/**
 *  Echo Client Lab
 *  Ross Casey - 12301716
 */

import java.net._
import java.io._
import scala.io.StdIn.readLine
import scala.io._

object EchoClient {

  def main(args: Array[String]) {

    //create socket, buffered input/output streams
    lazy val socket = new Socket(InetAddress.getByName("0.0.0.0"),8000)
    lazy val fromServer = new BufferedSource(socket.getInputStream).getLines()
    lazy val toServer = new PrintStream(socket.getOutputStream)

    //get user input from console
    val input = readLine("prompt>")

    //make user input URL safe
    val urlSafeInput = URLEncoder.encode(input, "UTF-8")

    //send request to server
    toServer.print("GET /echo.php?message=" + urlSafeInput + " HTTP/1.0\r\n\r\n")
    toServer.flush()

    //print response to console
    while(fromServer.hasNext) {
      println(fromServer.next())
    }
    socket.close()
  }

}