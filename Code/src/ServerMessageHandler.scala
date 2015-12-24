import java.io.{ByteArrayOutputStream, InputStream, PrintStream}
import java.net.Socket

import scala.io.BufferedSource

/**
 * Created by Ross on 12/12/15.
 */
object ServerMessageHandler {

  private def handleEncryptedMessage(connection: Connection, key: String): Array[String] = {
    val dataLine = connection.nextLine()
    val ticketLine = connection.nextLine()    //no ticket, we initiated connection

    if(dataLine.contains("ENCRYPTED_CONTENTS")) {
      val data = dataLine.split(":")(1).trim
      val decryptedData = new String(Encryptor.decrypt(data, key), "UTF-8")
      decryptedData.split("\n")
    } else {
      val returnArray = new Array[String](2)
      returnArray(0) = dataLine
      returnArray(1) = ticketLine
      returnArray
    }
  }


  def readFile(connection: Connection, fileID: String, ticket: String, sessionKey: String): Array[Byte] = {
    val reqMessage = new RequestReadFileMessage(fileID)
    val encReqMessage = Encryptor.encryptMessage(reqMessage, ticket, sessionKey)
    connection.sendMessage(encReqMessage)

    val response = handleEncryptedMessage(connection, sessionKey)
    if(!response(0).contains("ERROR")) {
      var count = Integer.parseInt(response(1).split(":")(1).trim)

      val baos = new ByteArrayOutputStream()
      while (count != 0) {
        baos.write(connection.readByte())
        count -= 1
      }

      val encryptedFile = new String(baos.toByteArray, "UTF-8")
      val decryptedFile = Encryptor.decrypt(encryptedFile, sessionKey)
      decryptedFile
    } else {
      println(response(1))
      null
    }
  }


  def getNode(connection: Connection,path: String, ticket: String, sessionKey: String, isRead: Boolean): Map[String, Object] = {
    var dict = Map[String, Object]()

    val reqMessage = new LookupMessage(path, isRead)
    val encMessage = Encryptor.encryptMessage(reqMessage, ticket, sessionKey)
    connection.sendMessage(encMessage)

    val response = handleEncryptedMessage(connection, sessionKey)

    val firstLine = response(0)
    if(firstLine.startsWith("LOOKUP_ID")) {

      val id = firstLine.split(":")(1).trim
      val ip = response(1).split(":")(1).trim
      val port = response(2).split(":")(1).trim

      dict += "id" -> id
      dict += "address" -> new NodeAddress(ip, port)

      dict
    } else {
      dict += "error" -> connection.nextLine().split(":")(1).trim
      dict
    }
  }


  def getTokenForServer(authNode: NodeAddress,node: NodeAddress, username: String, password: String): Array[String] =  {
    try {
      val authServer = new NodeAddress(authNode.getIP, authNode.getPort)
      val asCon = new Connection(0, new Socket(authServer.getIP, Integer.parseInt(authServer.getPort)))
      val logonMessage = Encryptor.createLogonMessage(node, username, password)
      asCon.sendMessage(logonMessage)

      val dataLine = asCon.nextLine().split(":")(1).trim
      val ticketLine = asCon.nextLine() //don't care, we initiated connection so no ticket

      val decrypedToken = new String(Encryptor.decrypt(dataLine, password), "UTF-8")
      decrypedToken.split("\n")
    } catch {
      case e: Exception => {
        null
      }
    }
  }


  def writeFile(connection: Connection, fileID: String, ticket: String, sessionKey: String, file: Array[Byte]): Unit = {
    val encryptedBytes = Encryptor.encrypt(file, sessionKey)

    val reqMessage = new WriteFileMessage(fileID, encryptedBytes.length)
    val encReqMessage = Encryptor.encryptMessage(reqMessage, ticket, sessionKey)
    connection.sendMessage(encReqMessage)
    connection.sendBytes(encryptedBytes.getBytes("UTF-8"))
  }
}
