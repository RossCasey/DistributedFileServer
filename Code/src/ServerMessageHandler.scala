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
}
