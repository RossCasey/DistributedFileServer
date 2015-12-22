import java.net.{Socket, InetAddress, ServerSocket}
import java.util.concurrent.{Executors, ExecutorService}
import java.nio.file.{Files, Paths}
import java.io.File
import javax.imageio.spi.RegisterableService
import scala.collection.mutable.ArrayBuffer


/**
 * Created by Ross on 10/11/15.
 */

//functionality interface offered by server
trait ChatServerUtility {
  def getIP: String
  def getPort: String
  def execute(x: Runnable): Unit
  def killServer(): Unit
  def getType: String
  def getDirectoryServer: NodeAddress
  def getPrimaryServer: NodeAddress
}






object ChatServer extends ChatServerUtility {

  var serverSocket: ServerSocket = null
  var threadPool: ExecutorService = null
  var portNumber: Int = -1

  var nodeType: String = ""
  var directoryServer: NodeAddress = null
  var primaryServer: NodeAddress = null

  def main(args: Array[String]) {
    //attempt to create a server socket, exit otherwise
    startServer(args)
    setupFileDirectory()

    if(nodeType == "REPLICA") {
      syncWithPrimary()
    }
    notifyDirectory()


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
   * Attempts to start server on specified port
   * @param args - arguments passed in to program at run time
   */
  def startServer(args: Array[String]): Unit = {
    try {
      portNumber = Integer.parseInt(args(0))
      serverSocket = new ServerSocket(portNumber)
      threadPool = Executors.newFixedThreadPool(32)

      directoryServer = new NodeAddress(args(1), args(2))
      nodeType = args(3)

      if(nodeType == "REPLICA") {
        primaryServer = new NodeAddress(args(4), args(5))
      }


      println("Listening on port " + portNumber + ": ")
    } catch {
      case e: Exception => {
        println("SERVER ERROR: " + e.getClass + ": " + e.getMessage)
        System.exit(0)
      }
    }
  }

  def notifyDirectory(): Unit = {
    return

    val directoryConnection = new Connection(0, new Socket(directoryServer.getIP, Integer.parseInt(directoryServer.getPort)))
    directoryConnection.sendMessage(new RegisterPrimaryMessage(getIP, getPort))

    val firstLine = directoryConnection.nextLine()
    if(firstLine.startsWith("ERROR_CODE: 3") || firstLine.startsWith("REGISTRATION_STATUS: OK")) {
      println("Registration with directory server successful")
    }
  }


  def syncWithPrimary(): Unit = {
    SyncHandler.syncWithPrimary(primaryServer)
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
   * Opens file
   */
  def openFile(): Unit = {
    val byteArray = Files.readAllBytes(Paths.get("./start.sh"))

    println(byteArray.length)
    //val path = Paths.get("./")

    val directory = new File("./")
    val files = directory.listFiles  // this is File[]
    val dirNames = ArrayBuffer[String]()
    for (file <- files) {
      if (file.isFile) {
        dirNames += file.getName
        println(file.getName)
      }
    }

  }


  def setupFileDirectory(): Unit = {
    val dir = new File("../Files");
    if(!dir.exists()) {
      println("Files directory does not exist. Creating one now...")
      dir.mkdir()
    } else {
      println("Files directory already exists.")
    }
  }

  var connectionId = 0
  private def computeNextId(): Int = {
    val newId = connectionId
    connectionId += 1
    newId
  }


  def getType: String = {
    nodeType
  }


  def getPrimaryServer: NodeAddress = {
    primaryServer
  }

  def getDirectoryServer: NodeAddress = {
    directoryServer
  }
}
