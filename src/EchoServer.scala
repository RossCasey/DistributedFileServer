/**
 *  Echo Server Lab
 *  Ross Casey - 12301716
 */

import scala.io._
import java.io._
import java.net._
import java.util.concurrent.Executors

object EchoServer {

  var serverSocket: ServerSocket = null

  def main(args: Array[String]) {
    serverSocket = new ServerSocket(8000)
    val threadPool = Executors.newFixedThreadPool(2)

    println("Listening on port 8000:")

    var exit = false
    while(!exit) {
      try {
        val s = serverSocket.accept()
        println("Connection accepted")
        val serverThread = new ServerThread(s, killServer)
        threadPool.execute(serverThread)
      } catch {
        case Exception => {
          exit = true
          println("Killing server...")
        }
      }
    }
  }

  def killServer(): Unit = {
    serverSocket.close()
  }
}
