/**
 *  Echo Server Lab
 *  Ross Casey - ***REMOVED***
 */

import java.net._
import java.util.concurrent.Executors

object EchoServer {

  var serverSocket: ServerSocket = null

  def main(args: Array[String]) {
    val portNumber = args(0)
    serverSocket = new ServerSocket(Integer.parseInt(portNumber))
    val threadPool = Executors.newFixedThreadPool(4)
    println("Listening on port " + portNumber + ": ")

    var exit = false
    while(!exit) {
      try {
        val s = serverSocket.accept()
        println("Connection accepted")
        val serverThread = new ServerThread(s, killServer)
        threadPool.execute(serverThread)
      } catch {
        case e: Exception => {
          exit = true
          println("Killing server...")
        }
      }
    }
    System.exit(0)
  }

  def killServer(): Unit = {
    serverSocket.close()
  }
}
