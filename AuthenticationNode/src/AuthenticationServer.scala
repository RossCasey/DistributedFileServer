import java.net.{Socket, InetAddress, ServerSocket}
import java.util.concurrent.{Executors, ExecutorService}
import java.nio.file.{Files, Paths}
import java.io.File
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.ListBuffer


/**
 * Created by Ross on 10/11/15.
 */

//functionality interface offered by server
trait ServerUtility {
  def getIP: String
  def getPort: String
  def execute(x: Runnable): Unit
  def killServer(): Unit
}


object AuthenticationServer extends ServerUtility {

  var serverSocket: ServerSocket = null
  var threadPool: ExecutorService = null
  var portNumber: Int = -1

  def main(args: Array[String]) {
    //attempt to create a server socket, exit otherwise
    startServer(args)

    EncryptionHandler.addUser("rcasey", "12345678")   //automatically add this user to table, all nodes use it

    var exit = false
    while(!exit) {
      try {
        val s = serverSocket.accept()
        println("Connection accepted")

        val newConnection = new Connection(computeNextId(), s)
        execute(new ConnectionInputListener(newConnection, this))

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
   * Attempts to start server with specified parameters
   *
   * @param args - arguments passed in to program at run time
   */
  def startServer(args: Array[String]): Unit = {
    try {
      portNumber = Integer.parseInt(args(0))
      serverSocket = new ServerSocket(portNumber)
      threadPool = Executors.newFixedThreadPool(32)

      println("Authentication Server listening on port " + portNumber + ": ")
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
    "localhost" //server cannot get its own IP due to NAT
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


  /**
   * Computes a unique ID for a new connection.
   */
  var connectionId = 0
  private def computeNextId(): Int = {
    val newId = connectionId
    connectionId += 1
    newId
  }
}
