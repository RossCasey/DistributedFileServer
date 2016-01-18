import java.io.{BufferedOutputStream, FileOutputStream, ByteArrayOutputStream, File}
import java.net.Socket
import java.nio.file.{Paths, Files}
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Ross on 12/12/15.
 */
object FileHandler {
  val base = "../Files/"

  private def getFile(fileIdentifier: String): Array[Byte] = {
    val byteArray = Files.readAllBytes(Paths.get(base + fileIdentifier))
    byteArray
  }

  def sendFileToConnection(connection: Connection, fileIdentifier: String, key: String): Unit = {
    val file = getFile(fileIdentifier)
    val encryptedFile = Encryptor.encrypt(file, key)

    val readFileMessage = new ReadingFileMessage(fileIdentifier, encryptedFile.length)
    val encrypedReadFileMessage = Encryptor.encryptMessage(readFileMessage, "", key)
    connection.sendMessage(encrypedReadFileMessage)
    connection.sendBytes(encryptedFile.getBytes)
  }


  private def getToken(node: NodeAddress, serverUtility: ChatServerUtility): Array[String] = {
    try {
      val authServer = serverUtility.getAuthenticationServer
      val asCon = new Connection(0, new Socket(authServer.getIP, Integer.parseInt(authServer.getPort)))
      val logonMessage = Encryptor.createLogonMessage(node, serverUtility)
      asCon.sendMessage(logonMessage)

      val dataLine = asCon.nextLine().split(":")(1).trim
      val ticketLine = asCon.nextLine() //don't care, we initiated connection so no ticket

      val decrypedToken = new String(Encryptor.decrypt(dataLine, serverUtility.getPassword), "UTF-8")
      decrypedToken.split("\n")
    } catch {
      case e: Exception => {
        null
      }
    }
  }





  private def copyFileToReplica(fileIdentifier: String, replica: NodeAddress, serverUtility: ChatServerUtility): Unit = {
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
    } catch {
      case e: Exception => {
        println(s"FAILED TO COPY FILL TO REPLICA " + replica.getIP + "/" + replica.getPort)
      }
    }
  }


  private def copyFileToAllReplicas(fileIdentifier: String, replicas: Array[NodeAddress], serverUtility: ChatServerUtility): Unit = {
    for(replica <- replicas) {
      copyFileToReplica(fileIdentifier, replica, serverUtility)
    }
  }


  def saveFile(connection: Connection, length: Int, fileIdentifier: String, serverUtility: ChatServerUtility, sessionKey: String): Unit = {
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


  def sendFileListToConnection(connection: Connection, sessionKey: String): Unit = {
    val fileIdMessage = new FileIDsResponseMessage(computeFileList())
    val encryptedMessage = Encryptor.encryptMessage(fileIdMessage, "", sessionKey)

    connection.sendMessage(encryptedMessage)
  }


  def computeHash(fileIdentifier: String): String = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val hash = new sun.misc.BASE64Encoder().encode(md.digest(getFile(fileIdentifier)))
    hash.toString
  }


  def sendHashToConnection(connection: Connection, fileIdentifier: String, sessionKey: String): Unit = {
    val hash = computeHash(fileIdentifier)
    val hashMessage = new FileHashResponseMessage(hash)
    val encrypedMessage = Encryptor.encryptMessage(hashMessage, "", sessionKey)

    connection.sendMessage(encrypedMessage)
  }

  def doesFileExist(fileIdentifier: String): Boolean = {
    Files.exists(Paths.get(base + fileIdentifier))
  }
}
