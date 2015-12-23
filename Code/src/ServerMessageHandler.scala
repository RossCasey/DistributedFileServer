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


  def sendUploadRequest(connection: Connection, path: String, length: Int): Unit = {
    connection.sendMessage(new WriteFileMessage(path, length))
  }


  def getNode(connection: Connection, path: String, isRead: Boolean): NodeAddress = {
    connection.sendMessage(new LookupMessage(path, isRead))
    val firstLine = connection.nextLine()
    if(firstLine.startsWith("LOOKUP_RESULT")) {
      val ip = firstLine.split(":")(1).trim
      val port = connection.nextLine().split(":")(1).trim
      return new NodeAddress(ip, port)
    } else {
      return null
    }
  }
}
