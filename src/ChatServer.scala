import java.net.ServerSocket
import java.util.concurrent.{Executors, ExecutorService}

/**
 * Created by Ross on 10/11/15.
 */

trait ChatServerUtility {
  def getIP: String
  def getPort: String
  def execute(x: Runnable): Unit
  def killServer: Unit
}



object ChatServer extends ChatServerUtility {

  var serverSocket: ServerSocket = null
  var threadPool: ExecutorService = null
  var portNumber: Int = -1
  var userListener: UserListener = new UserListenerImplementation(this)

  def main(args: Array[String]) {
    //attempt to create a server socket, exit otherwise
    startServer(args(0))


    var exit = false
    while(!exit) {
      try {
        val s = serverSocket.accept()
        println("Connection accepted")
        userListener.addUser(s)
      } catch {
        case e: Exception => {
          exit = true
          threadPool.shutdown()
          println("Killing server...")
        }
      }
    }
    System.exit(0)
  }


  def startServer(port: String): Unit = {
    try {
      portNumber = Integer.parseInt(port)
      serverSocket = new ServerSocket(portNumber)
      threadPool = Executors.newFixedThreadPool(16)
      println("Listening on port " + portNumber + ": ")
    } catch {
      case e: Exception => {
        println("SERVER ERROR: " + e.getClass + ": " + e.getMessage)
        System.exit(0)
      }
    }
  }

  def getIP: String = {
    serverSocket.getInetAddress.getHostAddress
  }

  def getPort: String = {
    serverSocket.getLocalPort.toString
  }

  def execute(job: Runnable): Unit = {
    threadPool.execute(job)
  }

  def killServer: Unit = {
    serverSocket.close()
  }
}
