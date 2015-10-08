/**
 *  Echo Server Lab
 *  Ross Casey - 12301716
 */

import scala.io._
import java.io._
import java.net._

object EchoServer {
  def main(args: Array[String]) {
    val serverSocket = new ServerSocket(8000)

    println("Listening on port 8000:")
    while(true) {
      val s = serverSocket.accept()
      println("connection accepted")
      val serverThread = new ServerThread(s)
      serverThread.start()
    }
  }
}
