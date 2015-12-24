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
trait ChatServerUtility {
  def getIP: String
  def getPort: String
  def execute(x: Runnable): Unit
  def killServer(): Unit
  def getType: String
  def getDirectoryServer: NodeAddress
  def getPrimaryServer: NodeAddress
  def getAuthenticationServer: NodeAddress
  def getReplicaServers: Array[NodeAddress]
  def addReplicaServer(replica: NodeAddress): Unit
  def getKey: String
  def getUsername: String
  def getPassword: String
}






object ChatServer extends ChatServerUtility {

  var serverSocket: ServerSocket = null
  var threadPool: ExecutorService = null
  var portNumber: Int = -1

  var nodeType: String = ""
  var directoryServer: NodeAddress = null
  var primaryServer: NodeAddress = null
  var authenticationServer: NodeAddress = new NodeAddress("localhost", 9000.toString)

  var username: String = "rcasey"
  var password: String = "12345678"

  var replicaServers = ListBuffer[NodeAddress]()

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


      username = args(1)
      password = args(2)

      authenticationServer = new NodeAddress(args(3), args(4))
      directoryServer = new NodeAddress(args(5), args(6))

      nodeType = args(7)

      if(nodeType == "REPLICA") {
        primaryServer = new NodeAddress(args(8), args(9))
      }


      println("Listening on port " + portNumber + ": ")
    } catch {
      case e: Exception => {
        println("SERVER ERROR: " + e.getClass + ": " + e.getMessage)
        System.exit(0)
      }
    }
  }


  private def getToken(node: NodeAddress, serverUtility: ChatServerUtility): Array[String] = {
    try {
      val authServer = serverUtility.getAuthenticationServer
      val asCon = new Connection(0, new Socket(authServer.getIP, Integer.parseInt(authServer.getPort)))
      val logonMessage = Encryptor.createLogonMessage(node, serverUtility)
      asCon.sendMessage(logonMessage)

      val dataLine = asCon.nextLine().split(":")(1).trim
      val ticketLine = asCon.nextLine() //don't care, we initiated connection so no ticket

      val decrypedToken = new String(Encryptor.decrypt(dataLine, serverUtility.getPassword), "UTF-8")
      decrypedToken.split("\n")
    } catch {
      case e: Exception => {
        null
      }
    }
  }

  def notifyDirectory(): Unit = {
    val token = getToken(directoryServer, this)
    if(token != null) {
      val ticket = token(0).split(":")(1).trim
      val sessionKey = token(1).split(":")(1).trim
      val directoryConnection = new Connection(0, new Socket(directoryServer.getIP, Integer.parseInt(directoryServer.getPort)))

      if(nodeType == "PRIMARY") {
        val message = new RegisterPrimaryMessage(getIP, getPort)
        val encMessage = Encryptor.encryptMessage(message, ticket, sessionKey)
        directoryConnection.sendMessage(encMessage)
      } else {
        val message = new RegisterReplicaMessage(getIP, getPort, primaryServer.getIP, primaryServer.getPort)
        val encMessage = Encryptor.encryptMessage(message, ticket, sessionKey)
        directoryConnection.sendMessage(encMessage)
      }

      val dataLine = directoryConnection.nextLine().split(":")(1).trim
      val ticketLine = directoryConnection.nextLine() //dont care

      val response = new String(Encryptor.decrypt(dataLine, sessionKey), "UTF-8")
      val firstLine = response
      if(firstLine.startsWith("ERROR_CODE: 3") || firstLine.startsWith("REGISTRATION_STATUS: OK")) {
        println("Registration with directory server successful")
      } else {
        println("Error registering with directory server")
      }
    } else {
      println("Error authenticating for communication with directory server")
    }
  }


  def syncWithPrimary(): Unit = {
    SyncHandler.syncWithPrimary(primaryServer, this)
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


  def addReplicaServer(replica: NodeAddress): Unit = {
    var alreadyAdded = false
    for(existingReplica <- replicaServers) {
      if(existingReplica.getIP == replica.getIP && existingReplica.getPort == replica.getPort) {
        alreadyAdded = true
      }
    }

    if(!alreadyAdded) {
      replicaServers += replica
    }
  }

  def getReplicaServers: Array[NodeAddress] = {
    replicaServers.toArray
  }

  def getKey: String = {
    "SuperSecretPassphrase"
  }

  def getAuthenticationServer: NodeAddress = {
    authenticationServer
  }

  def getPassword: String = {
    password
  }

  def getUsername: String = {
    username
  }
}
