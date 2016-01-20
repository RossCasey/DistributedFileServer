import java.io.{BufferedOutputStream, FileOutputStream, ByteArrayOutputStream, File}
import java.net.Socket
import java.nio.file.{Paths, Files}

import scala.collection.mutable.ArrayBuffer

/**
 * Created by Ross on 12/12/15.
 */
object FileHandler {
  val base = "../Files/"

  /**
   * Reads the data from a file on disk at the specified path
   * @param fileIdentifier - id of file to read data of
   * @return data of specified file
   */
  private def getFile(fileIdentifier: String): Array[Byte] = {
    val byteArray = Files.readAllBytes(Paths.get(base + fileIdentifier))
    byteArray
  }


  /**
   * Sends the file with the specified fileID across the passed connection,
   * encrypted with the specified session key
   * @param connection - connection to send encrypted file across
   * @param fileIdentifier - fileID of file to send
   * @param key - session key to encrypt file with
   */
  def sendFileToConnection(connection: Connection, fileIdentifier: String, key: String): Unit = {
    val file = getFile(fileIdentifier)
    val encryptedFile = Encryptor.encrypt(file, key)

    val readFileMessage = new ReadingFileMessage(fileIdentifier, encryptedFile.length)
    val encrypedReadFileMessage = Encryptor.encryptMessage(readFileMessage, "", key)
    connection.sendMessage(encrypedReadFileMessage)
    connection.sendBytes(encryptedFile.getBytes)
  }


  /**
   * Contacts the authentication server to get a ticket to communicate with the specified node
   *
   * @param node - node to request ticket for
   * @param serverUtility - allows function to request any credentials it needs from the server
   * @return decrypted response from authentication server
   */
  private def getToken(node: NodeAddress, serverUtility: ServerUtility): Array[String] = {
    try {
      val authServer = serverUtility.getAuthenticationServer
      val asCon = new Connection(0, new Socket(authServer.getIP, Integer.parseInt(authServer.getPort)))
      val logonMessage = Encryptor.createLogonMessage(node, serverUtility)
      asCon.sendMessage(logonMessage)

      val dataLine = asCon.nextLine().split(":")(1).trim
      val ticketLine = asCon.nextLine() //don't care, we initiated connection so no ticket
      asCon.close()

      val decrypedToken = new String(Encryptor.decrypt(dataLine, serverUtility.getPassword), "UTF-8")
      decrypedToken.split("\n")
    } catch {
      case e: Exception => {
        null
      }
    }
  }


  /**
   * Sends the file with the specified ID to a replica in order to sync state of both nodes.
   *
   * @param fileIdentifier - ID of file to send to replica
   * @param replica - address of replica to send file to
   * @param serverUtility - for access to credentials for encryption
   */
  private def copyFileToReplica(fileIdentifier: String, replica: NodeAddress, serverUtility: ServerUtility): Unit = {
    try {
      val token = getToken(replica, serverUtility)
      val sessionKey = token(1).split(":")(1).trim
      val ticket = token(0).split(":")(1).trim


      val repCon = new Connection(0, new Socket(replica.getIP, Integer.parseInt(replica.getPort)))
      val encryptedFile = Encryptor.encrypt(getFile(fileIdentifier), sessionKey)

      val writeFileMessage = new WriteFileMessage(fileIdentifier, encryptedFile.length)
      val encryptedWriteFileMessage = Encryptor.encryptMessage(writeFileMessage, ticket,sessionKey)

      repCon.sendMessage(encryptedWriteFileMessage)
      repCon.sendBytes(encryptedFile.getBytes("UTF-8"))
      repCon.close()
    } catch {
      case e: Exception => {
        println(s"FAILED TO COPY FILL TO REPLICA " + replica.getIP + "/" + replica.getPort)
      }
    }
  }


  /**
   * Send a file with the specified ID to all replicas registered to this machine
   *
   * @param fileIdentifier - id of file to send
   * @param replicas - replicas registered to this node
   * @param serverUtility - for access to credentials for encryption
   */
  private def copyFileToAllReplicas(fileIdentifier: String, replicas: Array[NodeAddress], serverUtility: ServerUtility): Unit = {
    for(replica <- replicas) {
      copyFileToReplica(fileIdentifier, replica, serverUtility)
    }
  }


  /**
   * Writes the encrypted file being received from the specified connection to disk and if node is a primary
   * then send it to all replicas
   *
   * @param connection - connection receiving file from
   * @param length - length of encrypted file being received
   * @param fileIdentifier - id of file being received
   * @param serverUtility - for access to credentials for encryption
   * @param sessionKey - key with which received contents are encrypted
   */
  def saveFile(connection: Connection, length: Int, fileIdentifier: String, serverUtility: ServerUtility, sessionKey: String): Unit = {
    var count = length
    val baos = new ByteArrayOutputStream()

    while(count > 0) {
      baos.write(connection.readByte())
      count -= 1
    }
    val fos = new FileOutputStream(base + fileIdentifier)
    val bos = new BufferedOutputStream(fos)


    val encryptedFile = baos.toString("UTF-8")
    val decryptedFile = Encryptor.decrypt(encryptedFile, sessionKey)

    bos.write(decryptedFile)
    bos.flush()
    bos.close()
    fos.close()

    if(serverUtility.getType == "PRIMARY") {
      copyFileToAllReplicas(fileIdentifier, serverUtility.getReplicaServers, serverUtility)
    }
  }


  /**
   * Writes the encrypted file being received from the specified connection to disk
   *
   * @param connection - connection receiving file from
   * @param length - length fo encrypted file being received
   * @param fileIdentifier - id of file being received
   * @param sessionKey - key with which received contents are encrypted
   */
  def saveFileWithoutSync(connection: Connection, length: Int, fileIdentifier: String, sessionKey: String): Unit = {
    var count = length
    val baos = new ByteArrayOutputStream()

    while(count > 0) {
      baos.write(connection.readByte())
      count -= 1
    }
    val fos = new FileOutputStream(base + fileIdentifier)
    val bos = new BufferedOutputStream(fos)

    val encryptedFile = baos.toString("UTF-8")
    val decryptedFile = Encryptor.decrypt(encryptedFile, sessionKey)

    bos.write(decryptedFile)
    bos.flush()
    bos.close()
    fos.close()
  }


  /**
   * Create a list of the ids of all files stored on this node.
   * @return - array of all file ids on node
   */
  def computeFileList(): Array[String] = {
    val directory = new File(base)
    val files = directory.listFiles  // this is File[]
    val fileNames = ArrayBuffer[String]()
    for (file <- files) {
      if (file.isFile) {
        fileNames += file.getName
      }
    }
    fileNames.toArray
  }


  /**
   * Sends the list of file ids to the specified connection. This is used when a replica first
   * registers with this node if it is a primary. The replica is then aware of all the files the
   * primary has and can request them one by one.
   *
   * @param connection - connection to send file list to
   * @param sessionKey - key to encrypt message with
   */
  def sendFileListToConnection(connection: Connection, sessionKey: String): Unit = {
    val fileIdMessage = new FileIDsResponseMessage(computeFileList())
    val encryptedMessage = Encryptor.encryptMessage(fileIdMessage, "", sessionKey)

    connection.sendMessage(encryptedMessage)
  }


  /**
   * Computes the hash of a given file's contents
   * @param fileIdentifier - id of file to compute hash of
   * @return string with hash of file contents
   */
  def computeHash(fileIdentifier: String): String = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val hash = new sun.misc.BASE64Encoder().encode(md.digest(getFile(fileIdentifier)))
    hash.toString
  }


  /**
   * Sends the hash of a specified file to the specified connection. This is used when a replica
   * that was offline is coming back online. The hash allows the replica to determine if the
   * files it has are up to date. If the hashes of the file on the replica and primary match then
   * the replica's file is up to date and it does not need to download it. If the hashes do not
   * match then the replica can request the primary's copy to bring its file up to date.
   *
   * @param connection - connection to send hash message to
   * @param fileIdentifier - id of file to compute hash of
   * @param sessionKey - session key to encrypt communication with
   */
  def sendHashToConnection(connection: Connection, fileIdentifier: String, sessionKey: String): Unit = {
    val hash = computeHash(fileIdentifier)
    val hashMessage = new FileHashResponseMessage(hash)
    val encrypedMessage = Encryptor.encryptMessage(hashMessage, "", sessionKey)

    connection.sendMessage(encrypedMessage)
  }


  /**
   * Determines whether a file with the given ID exists on this node.
   *
   * @param fileIdentifier - file to determine the existence of
   * @return true if file exists, false otherwise
   */
  def doesFileExist(fileIdentifier: String): Boolean = {
    Files.exists(Paths.get(base + fileIdentifier))
  }
}
