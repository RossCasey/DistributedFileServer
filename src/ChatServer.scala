import java.net.{InetAddress, ServerSocket}
import java.util.concurrent.{Executors, ExecutorService}

/**
 * Created by Ross on 10/11/15.
 */

//functionality interface offered by server
trait ChatServerUtility {
  def getIP: String
  def getPort: String
  def execute(x: Runnable): Unit
  def killServer(): Unit
}


object ChatServer extends ChatServerUtility {

  var serverSocket: ServerSocket = null
  var threadPool: ExecutorService = null
  var portNumber: Int = -1
  var userHandler: UserHandler = new UserHandlerImplementation(this)

  def main(args: Array[String]) {
    //attempt to create a server socket, exit otherwise
    startServer(args(0))


    var exit = false
    while(!exit) {
      try {
        val s = serverSocket.accept()
        println("Connection accepted")
        userHandler.addUser(s)

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


  /**
   * Attempts to start server on specified port
   * @param port - port to run server on
   */
  def startServer(port: String): Unit = {
    try {
      portNumber = Integer.parseInt(port)
      serverSocket = new ServerSocket(portNumber)
      threadPool = Executors.newFixedThreadPool(32)
      println("Listening on port " + portNumber + ": ")
    } catch {
      case e: Exception => {
        println("SERVER ERROR: " + e.getClass + ": " + e.getMessage)
        System.exit(0)
      }
    }
  }


  /**
   * @return IP address of server
   */
  def getIP: String = {
    "178.62.123.87" //server cannot get its own IP due to NAT
  }


  /**
   * @return Port server is running on
   */
  def getPort: String = {
    serverSocket.getLocalPort.toString
  }


  /**
   * Executes a thread on server's thread pool
   * @param job - thread to execute
   */
  def execute(job: Runnable): Unit = {
    threadPool.execute(job)
  }


  /**
   * Kills the server
   */
  def killServer(): Unit = {
    serverSocket.close()
  }
}
