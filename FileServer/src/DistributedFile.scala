import java.io.{BufferedOutputStream, FileOutputStream, ByteArrayOutputStream, PrintStream}
import java.net.{InetAddress, Socket}
import java.nio.charset.{CharsetDecoder, Charset, CodingErrorAction}

import ServerMessages._

import scala.collection.mutable.ListBuffer
import scala.io.BufferedSource


/**
 * Created by Ross on 12/12/15.
 */
class DistributedFile(path: String, authIP: String, authPort: Int, directoryIP: String, directoryPort: Int, username: String, password: String) {
  var contents: Array[Byte] = Array[Byte]()
  val authAddress = new NodeAddress(authIP, authPort.toString)
  val directoryAddress = new NodeAddress(directoryIP, directoryPort.toString)


  /**
   * Parses the response from an authentication server and returns a ServerMessages.TokenMessage
   * which contains all relevant information for secure message transfer with the
   * requested node
   *
   * @param response - response received from authentication server (decrypted)
   * @return ServerMessages.TokenMessage containing details for secure communication with requested node
   */
  private def parseTokenFromAuthenticationServerResponse(response: String): TokenMessage = {
    try {
      val token = response.split("\n")

      val ticket = token(0).split(":")(1).trim
      val sessionKey = token(1).split(":")(1).trim
      val serverIP = token(2).split(":")(1).trim
      val serverPort = token(3).split(":")(1).trim
      val expirationTime = token(4).split(":")(1).trim

      new TokenMessage(ticket, sessionKey, serverIP, serverPort, expirationTime)
    } catch {
      case e: Exception => {
        null
      }
    }
  }


  /**
   * Contacts the authentication server to obtain a ticket from communication with the specified
   * node.
   *
   * @param node - node to request ticket for
   * @return ServerMessages.TokenMessage containing all relevant information for secure communication
   */
  private def getTicketForNodeFromAuthServer(node: NodeAddress): TokenMessage = {
    try {
      //contact authentication server and send logon message for specified server
      val authCon = new Connection(0, new Socket(authAddress.getIP, Integer.parseInt(authAddress.getPort)))
      val logonMessage = Encryptor.createLogonMessage(node, username, password)
      authCon.sendMessage(logonMessage)

      //receive response from authentication server
      val dataLine = authCon.nextLine().split(":")(1).trim
      val ticketLine = authCon.nextLine() //don't care, we initiated connection so no ticket
      authCon.close()

      //decrypt response from authentication server
      val decrypedToken = new String(Encryptor.decrypt(dataLine, password), "UTF-8")

      parseTokenFromAuthenticationServerResponse(decrypedToken)
    } catch {
      case e: Exception => {
        null
      }
    }
  }


  /**
   * Parses the response from the directory server to obtain all the information necessary to
   * contact the file server specified by the directory server and perform file actions.
   *
   * @param response - response received from directory server (decrypted)
   * @return ServerMessages.LookupResultMessage containing all relevant info to contact file node with file
   */
  private def parseLookupResultMessageFromDirectoryServerResponse(response: String): LookupResultMessage = {
    try {
      val messageLines = response.split("\n")

      val fileID = messageLines(0).split(":")(1).trim
      val ipAddress = messageLines(1).split(":")(1).trim
      val port = messageLines(2).split(":")(1).trim

      new LookupResultMessage(fileID, ipAddress, port)
    } catch {
      case e: Exception => {
        null
      }
    }
  }


  /**
   * Contacts the directory server and performs a file lookup for the specified file.
   *
   * @param ticket - ticket recieved from authentication server for communication with Directory server
   * @param sessionKey - session key recieved from authentication server for communication with Directory server
   * @param forReading - whether the file is to be read or written to (this affects the node the DS returns)
   * @return ServerMessages.LookupResultMessage containing all relevant info to contaict file node with file
   */
  private def lookupFileAddressFromDirectory(ticket: String, sessionKey: String, forReading: Boolean): LookupResultMessage= {
    try {
      val dirCon = new Connection(0, new Socket(directoryAddress.getIP, Integer.parseInt(directoryAddress.getPort)))

      val lookupMessage = new LookupMessage(path, forReading)
      val encLookupMessage = Encryptor.encryptMessage(lookupMessage, ticket, sessionKey)
      dirCon.sendMessage(encLookupMessage)
      println("got here 0")

      val dataLine = dirCon.nextLine().split(":")(1).trim
      val ticketLine = dirCon.nextLine()
      dirCon.close()

      val message = new String(Encryptor.decrypt(dataLine, sessionKey), "UTF-8")

      parseLookupResultMessageFromDirectoryServerResponse(message)
    } catch {
      case e: Exception => {
        null
      }
    }
  }


  /**
   * Parses the reading file message received from a file node and returns a ServerMessages.ReadingFileMessage
   * containing the id and length of the file.
   *
   * @param response - response received from file node
   * @return ServerMessages.ReadingFileMessage containing info relevant to file operations (id and length)
   */
  private def parseReadingFileMessageFromServerResponse(response: String): ReadingFileMessage = {
    try {
      val readingFile = response.split("\n")

      val fileID = readingFile(0).split(":")(1).trim
      val lengthOfEncryptedFile = Integer.parseInt(readingFile(1).split(":")(1).trim)

      new ReadingFileMessage(fileID, lengthOfEncryptedFile)
    } catch {
      case e: Exception => {
        null
      }
    }
  }


  /**
   * Attempts to download the file at the specified path (AFS) by communicating with the
   * authentication server, directory server, and if file present a file server.
   *
   * @return - true if download of file was possible (file exists), false otherwise
   */
  private def downloadFile(): Boolean = {
    try {
      val tokenForDir = getTicketForNodeFromAuthServer(directoryAddress)
      val readFile = true
      val lookupResult = lookupFileAddressFromDirectory(tokenForDir.getTicket, tokenForDir.getSessionKey, readFile)

      if(lookupResult != null) {
        val fileNodeAddress = new NodeAddress(lookupResult.getIP, lookupResult.getPort)
        val tokenForFileNode = getTicketForNodeFromAuthServer(fileNodeAddress)

        val nodeCon = new Connection(0, new Socket(fileNodeAddress.getIP, Integer.parseInt(fileNodeAddress.getPort)))
        val fileRequestMessage = Encryptor.encryptMessage(new RequestReadFileMessage(lookupResult.getID), tokenForFileNode.getTicket, tokenForFileNode.getSessionKey)
        nodeCon.sendMessage(fileRequestMessage)

        //receive response from authentication server
        val dataLine = nodeCon.nextLine().split(":")(1).trim
        val ticketLine = nodeCon.nextLine() //don't care, we initiated connection so no ticket

        val message = new String(Encryptor.decrypt(dataLine, tokenForFileNode.getSessionKey), "UTF-8")
        val length = parseReadingFileMessageFromServerResponse(message).getLength

        var count = length
        val baos = new ByteArrayOutputStream()

        //write encrypted bytes to baos
        while(count > 0) {
          baos.write(nodeCon.readByte())
          count -= 1
        }
        nodeCon.close()

        //decrypt and store decrypted bytes of file locally
        val encryptedFile = baos.toString("UTF-8")
        val decryptedFile = Encryptor.decrypt(encryptedFile, tokenForFileNode.getSessionKey)

        contents = Array.ofDim[Byte](decryptedFile.length)
        for(i <- 0 to decryptedFile.length - 1) {
          contents(i) = decryptedFile(i)
        }

        return true
      } else {
        return false
      }
    } catch {
      case e: Exception => {
        return false
      }
    }
  }


  /**
   * A replacement for the standard file open operation. Rather than attempting to get the
   * file bytes from the disk (as is the case in a normal file API) it attempts to download
   * the file from the distributed file server. If this is not possible (because the file specified
   * does not exist) the file is initialised locally.
   */
  def open():Unit = {
    //try to download file
    if(!downloadFile()) {
      println("file does not exist in file system, initing blank file locally")
    }
  }


  /**
   * Uploads the contents of the locally cached file to a primary node specified by the
   * directory server.
   */
  def close(): Unit = {
    println("Starting file close")
    try {
      val tokenForDir = getTicketForNodeFromAuthServer(directoryAddress)
      val readFile = false
      val lookupResult = lookupFileAddressFromDirectory(tokenForDir.getTicket, tokenForDir.getSessionKey, readFile)

      //don't need to check as lookup result for a write will never fail (otherwise how could new files be added)
      val fileNodeAddress = new NodeAddress(lookupResult.getIP, lookupResult.getPort)
      val tokenForFileNode = getTicketForNodeFromAuthServer(fileNodeAddress)

      //connect to primary specified by directory server
      val nodeCon = new Connection(0, new Socket(fileNodeAddress.getIP, Integer.parseInt(fileNodeAddress.getPort)))

      //upload encrypted file to primary
      val encryptedFile = Encryptor.encrypt(contents.toArray, tokenForFileNode.getSessionKey)
      val encryptedMeesage = Encryptor.encryptMessage(new WriteFileMessage(lookupResult.getID, encryptedFile.length),tokenForFileNode.getTicket , tokenForFileNode.getSessionKey)
      nodeCon.sendMessage(encryptedMeesage)
      nodeCon.sendBytes(encryptedFile.getBytes)
      println("Encrypted file uploaded")

    } catch {
      case e: Exception => {
        //
      }
    }

  }


  /**
   * Reads the specified part of the file and returns the bytes that it contains.
   * @param startPosition - index into byte array where read operation starts
   * @param length - how many bytes to read
   * @return Array[Byte] containing the bytes read from the file
   */
  def read(startPosition: Int, length: Int): Array[Byte] = {
    val readArray = Array.ofDim[Byte](length)

    for( i <- 0 to length) {
      readArray(i) = contents(i + startPosition)
    }
    readArray
  }


  /**
    * Writes the passed data to the specified portion of the file, increasing the
    * length of the file if necessary.
    * @param startPosition - index into byte array where write should start
    * @param length - how many bytes to write
    * @param data - the bytes to write
    */
  def write(startPosition: Int, length: Int, data: Array[Byte]): Unit = {
    println("Beginning write")
    if(startPosition + length < contents.length) {
      for(i <- 0 to length) {
        contents(i + startPosition) = data(i)
      }
    } else {
      //contents array is not long enough so we need to extend it before we write
      val newEndPosition = startPosition + length
      val extraBytesNeeded = newEndPosition - contents.length

      var newContents = Array.ofDim[Byte](newEndPosition)
      for(i <- 0 to contents.length - 1) {
        newContents(i) = contents(i)
      }

      //now have enough space for write
      println("Writing new bytes")
      for(i <- 0 to length - 1) {
        newContents(i + startPosition) = data(i)
      }
      contents = newContents
    }
    println("Finished write")
  }


  /**
   * Prints the contents of the file to console
   */
  def printContents(): Unit = {
    for(byte <- contents) {
      println(byte)
    }
  }
}
