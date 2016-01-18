import java.io.{BufferedOutputStream, FileOutputStream, ByteArrayOutputStream, PrintStream}
import java.net.{InetAddress, Socket}
import java.nio.charset.{CharsetDecoder, Charset, CodingErrorAction}

import scala.collection.mutable.ListBuffer
import scala.io.BufferedSource


/**
 * Created by Ross on 12/12/15.
 */
class DistributedFile(path: String, authIP: String, authPort: Int, directoryIP: String, directoryPort: Int, username: String, password: String) {
  val contents: ListBuffer[Byte] = ListBuffer()
  val authAddress = new NodeAddress(authIP, authPort.toString)
  val directoryAddress = new NodeAddress(directoryIP, directoryPort.toString)


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


  private def lookupFileAddressFromDirectory(ticket: String, sessionKey: String, forWrite: Boolean): LookupResultMessage= {
    try {
      val dirCon = new Connection(0, new Socket(directoryAddress.getIP, Integer.parseInt(directoryAddress.getPort)))

      val lookupMessage = new LookupMessage(path, forWrite)
      val encLookupMessage = Encryptor.encryptMessage(lookupMessage, ticket, sessionKey)
      dirCon.sendMessage(encLookupMessage)

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


  private def downloadFile(): Unit = {
    try {
      val tokenForDir = getTicketForNodeFromAuthServer(directoryAddress)
      val lookupResult = lookupFileAddressFromDirectory(tokenForDir.getTicket, tokenForDir.getSessionKey, false)

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

      while(count > 0) {
        baos.write(nodeCon.readByte())
        count -= 1
      }
      nodeCon.close()

      val encryptedFile = baos.toString("UTF-8")
      val decryptedFile = Encryptor.decrypt(encryptedFile, tokenForFileNode.getSessionKey)

      for (f <- decryptedFile) {
        contents += f
      }
    } catch {
      case e: Exception => {
        null
      }
    }
  }






  /*
  * val authCon = new Connection(2, new Socket(InetAddress.getByName("localhost"),8000))
    val token = ServerMessageHandler.getTokenForServer(authCon, new NodeAddress("localhost", 8100.toString), "rcasey", "12345678")

    if(token != null) {
      println("got token")
      val ticket = token(0).split(":")(1).trim
      val sessionKey = token(1).split(":")(1).trim

      val dirCon = new Connection(3, new Socket(InetAddress.getByName("localhost"), 8100))

      val lookupMessage = new LookupMessage("testFile.txt", false)
      val encLookupMessage = Encryptor.encryptMessage(lookupMessage, ticket, sessionKey)
      dirCon.sendMessage(encLookupMessage)

      val dataLine = dirCon.nextLine().split(":")(1).trim
      val ticketLine = dirCon.nextLine()
      println("got stuff from dir server")


      val message = new String(Encryptor.decrypt(dataLine, sessionKey), "UTF-8")
      val messageLines = message.split("\n")

      val fileID = messageLines(0).split(":")(1).trim
      val ipAddress = messageLines(1).split(":")(1).trim
      val port = messageLines(2).split(":")(1).trim

      val priCon = new Connection(4, new Socket(InetAddress.getByName(ipAddress), Integer.parseInt(port)))


      val encryptedFile = Encryptor.encrypt(Files.readAllBytes(Paths.get("./testFile.txt")), sessionKey)
      val encryptedMeesage = Encryptor.encryptMessage(new WriteFileMessage(fileID, encryptedFile.length), ticket, sessionKey)
      priCon.sendMessage(encryptedMeesage)
      priCon.sendBytes(encryptedFile.getBytes)
      println("send encrypted bytes")
  * */

  /*
  private def downloadFile(node: NodeAddress): Unit = {
    lazy val socket = new Socket(InetAddress.getByName(node.getIP), Integer.parseInt(node.getPort))
    val connection = new Connection(0, socket)

    var count = ServerMessageHandler.requestFile(connection, path)
    val baos = new ByteArrayOutputStream()

    while (count != 0) {
      baos.write(connection.readByte())
      count -= 1
    }

    for (f <- baos.toByteArray) {
      contents += f
    }
    socket.close()
  }
  */


  private def initialiseFile(): Unit = {
    //contact directory server -> check if file exists
    //if file exists:
    val directoryIP = "localhost"
    val directoryPort = 8000


    val connection = new Connection(0, new Socket(InetAddress.getByName(directoryIP), directoryPort))
    val dict = ServerMessageHandler.getNode(connection, path, true)

    if(!dict.contains("error")) {
      val nodeAddress = dict("address").asInstanceOf[NodeAddress]
      //downloadFile(nodeAddress)
    } else {
      //init a blank file
    }
  }


  def open():Unit = {
    //initialiseFile()
    downloadFile()
    println(contents.length)
  }


  def close(): Unit = {
    var asByteArray = contents.toArray

    lazy val socket = new Socket(InetAddress.getByName("localhost"),8004)
    val connection = new Connection(0, socket)

    ServerMessageHandler.sendUploadRequest(connection, "testFile2.png", asByteArray.length)
    connection.sendBytes(asByteArray)
    socket.close()
  }


  def read(startPosition: Int, length: Int): Unit = {
    //TO-DO


    /*
    val readArray = Array.ofDim[Byte](length)

    for( i <- 0 to length) {
      readArray(i) = contents(i + startPosition)
    }
    readArray
    */

  }


  def write(): Unit = {
    //TO-DO
  }


  def printContents(): Unit = {
    for(byte <- contents) {
      println(byte)
    }
  }
}
