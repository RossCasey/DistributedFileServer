/**
 *  Echo Client Lab
 *  Ross Casey - cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3
 */

import java.net._
import java.io._
import scala.io._

object EchoClient {

  def main(args: Array[String]) {

    //create socket, buffered input/output streams
    lazy val socket = new Socket(InetAddress.getByName("178.62.123.87"),8000)
    lazy val fromServer = new BufferedSource(socket.getInputStream).getLines()
    lazy val toServer = new PrintStream(socket.getOutputStream)

    //get user input from console
    val input = readLine("prompt>")


    //send request to server
    toServer.println(input)
    toServer.flush()

    //print response to console
    while(fromServer.hasNext) {
      println(fromServer.next())
    }
    socket.close()
  }

}