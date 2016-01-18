import java.io.{InputStream, PrintStream}
import java.net.Socket

import scala.io.BufferedSource

/**
 * Created by Ross on 12/12/15.
 */
object ServerMessageHandler {


  private def handleReadingFileMessage(firstLine: String, connection: Connection): Int = {
    val length = Integer.parseInt(connection.nextLine().split(":")(1).trim)
    length
  }


  def requestFile(connection: Connection, path: String): Int = {
    connection.sendMessage(new RequestReadFileMessage(path))
    val firstLine = connection.nextLine()
    val length = handleReadingFileMessage(firstLine, connection)
    length
  }


  def sendUploadRequest(connection: Connection, fileId: String, length: Int): Unit = {
    connection.sendMessage(new WriteFileMessage(fileId, length))
  }


  def getNode(connection: Connection, path: String, isRead: Boolean): Map[String, Object] = {
    var dict = Map[String, Object]()
    connection.sendMessage(new LookupMessage(path, isRead))
    val firstLine = connection.nextLine()
    if(firstLine.startsWith("LOOKUP_ID")) {

      val id = firstLine.split(":")(1).trim
      val ip = connection.nextLine().split(":")(1).trim
      val port = connection.nextLine().split(":")(1).trim

      dict += "id" -> id
      dict += "address" -> new NodeAddress(ip, port)

      dict
    } else {
      dict += "error" -> connection.nextLine().split(":")(1).trim
      dict
    }
  }

  //def getTokenForDirectoryServer(connection: Connection)


  def getTokenForServer(connection: Connection, node: NodeAddress, username: String, password: String): Array[String] =  {
    try {
      val authServer = new NodeAddress("localhost", 8000.toString)
      val asCon = new Connection(0, new Socket(authServer.getIP, Integer.parseInt(authServer.getPort)))
      val logonMessage = Encryptor.createLogonMessage(node, username, password)
      asCon.sendMessage(logonMessage)

      val dataLine = asCon.nextLine().split(":")(1).trim
      val ticketLine = asCon.nextLine() //don't care, we initiated connection so no ticket

      val decrypedToken = new String(Encryptor.decrypt(dataLine, password), "UTF-8")
      asCon.close()
      decrypedToken.split("\n")
    } catch {
      case e: Exception => {
        null
      }
    }
  }
}
