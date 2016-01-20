import java.net.{Socket, InetAddress, ServerSocket}
import java.util.concurrent.{Executors, ExecutorService}
import java.nio.file.{Files, Paths}
import java.io.File
import ServerMessages.{RegisterReplicaMessage, RegisterPrimaryMessage}

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
  def getType: String
  def getDirectoryServer: NodeAddress
  def getPrimaryServer: NodeAddress
  def getAuthenticationServer: NodeAddress
  def getReplicaServers: Array[NodeAddress]
  def addReplicaServer(replica: NodeAddress): Unit
  def getKey: String
  def getUsername: String
  def getPassword: String
  def getToken(node: NodeAddress): Array[String]
}




object FileServer extends ServerUtility {

  var serverSocket: ServerSocket = null
  var threadPool: ExecutorService = null
  var portNumber: Int = -1

  var nodeType: String = ""
  var directoryServer: NodeAddress = null
  var primaryServer: NodeAddress = null
  var authenticationServer: NodeAddress = null

  var username: String = null
  var password: String = null

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
   * Attempts to start server with specified paramters
   * @param args - arguments passed in to program at run time
   */
  def startServer(args: Array[String]): Unit = {
    try {
      portNumber = Integer.parseInt(args(0))
      serverSocket = new ServerSocket(portNumber)
      threadPool = Executors.newFixedThreadPool(256)

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


  /**
   * Gets the token from the authentication server for computing with a given node
   * @param node - node to request ticket for
   * @return decrypted response from authentication server
   */
  private def getToken(node: NodeAddress): Array[String] = {
    try {
      val authServer = getAuthenticationServer
      val asCon = new Connection(0, new Socket(authServer.getIP, Integer.parseInt(authServer.getPort)))
      val logonMessage = Encryptor.createLogonMessage(node, this)
      asCon.sendMessage(logonMessage)

      val dataLine = asCon.nextLine().split(":")(1).trim
      val ticketLine = asCon.nextLine() //don't care, we initiated connection so no ticket

      val decrypedToken = new String(Encryptor.decrypt(dataLine, getPassword), "UTF-8")
      decrypedToken.split("\n")
    } catch {
      case e: Exception => {
        null
      }
    }
  }


  /**
   * Notifies the directory of the existence of this node. If this node is a primary
   * then new files may be assigned to it that it must store. If this node is a replica
   * then it must occassionally return data for a file that has been requested.
   */
  def notifyDirectory(): Unit = {
    val token = getToken(directoryServer)
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


  /**
   * If this node is a replica then on start up it must sync with the primary
   * and download any new/modified files.
   */
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


  /**
   * Sets up a directory to store all files in if one does
   * not exist
   */
  def setupFileDirectory(): Unit = {
    val dir = new File("../Files");
    if(!dir.exists()) {
      println("Files directory does not exist. Creating one now...")
      dir.mkdir()
    } else {
      println("Files directory already exists.")
    }
  }


  /**
   * Returns a unique ID for each new connection
   */
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


  /**
   * When a new replica server is assigned to this node (if it is a primary) it
   * is added to this list so that updates can be pushed to it.
   *
   * @param replica - address of new replica to add
   */
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
