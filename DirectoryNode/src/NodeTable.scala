import java.io.{FileOutputStream, ObjectOutputStream, ObjectInputStream, FileInputStream}
import java.net.{InetAddress, Socket}

import scala.collection.mutable.ListBuffer
import scala.util.Random

/**
 * Created by Ross on 22/12/15.
 */
class NodeTable {
  var entries: ListBuffer[NodeTableEntry] = null
  var nextAvailableId = -1
  initialise()


  /**
   * Initialises the lookup table. Reads the files that store the lookup table info
   * from disk to recreate the state of the directory node when it was shut down. If
   * the files do not exist then create blank new ones.
   */
  def initialise(): Unit = {
    try {
      var inputStream = new ObjectInputStream(new FileInputStream("./NodeTable.lt"))
      var obj = inputStream.readObject()
      entries = obj.asInstanceOf[ListBuffer[NodeTableEntry]]

      inputStream = new ObjectInputStream(new FileInputStream("./NodeTableId.lt"))
      obj = inputStream.readObject()
      nextAvailableId = obj.asInstanceOf[Int]

      inputStream.close()
    } catch {
      case e: Exception => {
        //if file do not exist then create them
        entries = ListBuffer[NodeTableEntry]()
        nextAvailableId = 0
        save()
      }
    }
  }


  /**
   * Save the state of the lookup table to disk so that it can be restored when
   * directory node is started again after shutdown.
   */
  private def save(): Unit = {
    var outputStream = new ObjectOutputStream(new FileOutputStream("./NodeTable.lt"))
    outputStream.writeObject(entries)

    outputStream = new ObjectOutputStream(new FileOutputStream("./NodeTableId.lt"))
    outputStream.writeObject(nextAvailableId)

    outputStream.close()
  }


  /**
   * Sends a ping message to the server at the specified ip/port in order to determine
   * whether it is up and whether it is responding to requests.
   *
   * @param ip - ip of node to send ping message to
   * @param port - port of node to send ping message to
   * @param serverUtility - for access to credentials needed for communication
   * @return true if server is up and responding to requests, false otherwise
   */
  private def pingServer(ip: String, port: String, serverUtility: ServerUtility): Boolean = {
    try {
      val authNode = serverUtility.getAuthenticationServer
      val authCon = new Connection(0, new Socket(authNode.getIP, Integer.parseInt(authNode.getPort)))

      val node = new NodeAddress(ip, port)
      val logonReq = Encryptor.createLogonMessage(node, serverUtility)
      authCon.sendMessage(logonReq)

      var dataLine = authCon.nextLine().split(":")(1).trim
      var ticketLine = authCon.nextLine() //dont care
      authCon.close()

      var decrypted = new String(Encryptor.decrypt(dataLine, serverUtility.getPassword), "UTF-8")
      var messageLines = decrypted.split("\n")

      val ticket = messageLines(0).split(":")(1).trim
      val sessionKey = messageLines(1).split(":")(1).trim

      val nodeSocket = new Socket(ip, Integer.parseInt(port))
      nodeSocket.setSoTimeout(5000)
      val nodeConnection = new Connection(1, nodeSocket)
      val encPing = Encryptor.encryptMessage(new PingMessage, ticket, sessionKey)
      nodeConnection.sendMessage(encPing)

      dataLine = nodeConnection.nextLine().split(":")(1).trim
      ticketLine = nodeConnection.nextLine() //dont care
      nodeConnection.close()

      decrypted = new String(Encryptor.decrypt(dataLine, sessionKey), "UTF-8")
      if(decrypted.contains("PONG")) {
        return true
      } else {
        return false
      }
    } catch {
      case e: Exception => {
        return false
      }
    }
  }


  /**
   * Adds an entry for a primary node to the table
   *
   * @param ip - of primary to add
   * @param port - of primary to add
   * @return true if node was added, false otherwise (collision)
   */
  def addPrimary(ip: String, port: String): Boolean = {
    if(findNode(ip,port) == null) {
      val nextId = nextAvailableId
      nextAvailableId += 1

      val newEntry = new NodeTableEntry(nextId.toString, ip, port, false)
      entries += newEntry

      save()
      true
    } else {
      false
    }
  }


  /**
   * Adds a replica for a replica node to the table
   *
   * @param id - id of primary to which this node is replica
   * @param ip - of replica to add
   * @param port - of replica to add
   * @return true if node was added, false otherwise (collision)
   */
  def addReplica(id: String, ip: String, port: String): Boolean = {
    if(findNode(ip, port) == null) {
      val newEntry = new NodeTableEntry(id, ip, port, true)
      entries += newEntry
      save()
      true
    } else {
      false
    }
  }


  /**
   * Returns a NodeTableEntry for a node with the specified ID that can be
   * read from.
   *
   * @param id - id node must have
   * @param serverUtility - used to ping node so only available nodes are returned
   * @return acceptable node for reading from
   */
  def getReadableNode(id: String, serverUtility: ServerUtility): NodeTableEntry = {
    //get all nodes with the correct id
    var nodes = ListBuffer[NodeTableEntry]()
    for(entry <- entries) {
      if(entry.getId == id) {
        nodes += entry
      }
    }

    //default to returning primary (it cannot go down)
    var primary: NodeTableEntry = null
    for(node <- nodes) {
      if(!node.getIsReplica) {
        primary = node
      }
    }

    //prefer not to read from primary so search for replicas
    var replicas = ListBuffer[NodeTableEntry]()
    for(node <- nodes) {
      if(node.getIsReplica) {
        replicas += node
      }
    }

    //return a random replica that responds to ping
    if(replicas.nonEmpty) {
      replicas = Random.shuffle(replicas)
      for(replica <- replicas) {
        if(pingServer(replica.getIp, replica.getPort, serverUtility)) {
          return replica
        }
      }
    }

    //if there are no alive replicas, return pirmary
    primary
  }


  /**
   * Returns a NodeTableEntry for a node with the specified ID that can be written to.
   *
   * @param id - id node must have
   * @return acceptable node for writing to
   */
  def getWritableNodeWithId(id: String): NodeTableEntry = {
    for(entry <- entries) {
      if(entry.getId == id && !entry.getIsReplica) {
        return entry
      }
    }
    return null
  }


  /**
   * Returns any primary to which a new file can be written (random)
   *
   * @return random primary suitable for writing new file to
   */
  def getAnyWritableNodeForNewFile(): NodeTableEntry = {
    var primaries =  ListBuffer[NodeTableEntry]()
    for(entry <- entries) {
      if(!entry.getIsReplica) {
        primaries += entry
      }
    }

    //return a random primary
    primaries(Random.nextInt(primaries.size))
  }


  /**
   * Find the entry for a node with the specified IP/port
   *
   * @param ip - of node to find
   * @param port - of node to find
   * @return NodeTableEntry with node that contains that IP/port combination
   */
  def findNode(ip: String, port: String): NodeTableEntry = {
    for(entry <- entries) {
      if(entry.getIp == ip && entry.getPort == port) {
        return entry
      }
    }
    return null
  }


  /**
   * Returns a string representation for the state of the lookup table
   * @return
   */
  override def toString: String = {
    var str = "Node Table: \n"
    for(entry <- entries) {
      if(!entry.getIsReplica) {
        str = str + "PRIMARY    ID: " + entry.getId + "    IP: " + entry.getIp + "    Port: " + entry.getPort + "\n"
      } else {
        str = str + "REPLICA    ID: " + entry.getId + "    IP: " + entry.getIp + "    Port: " + entry.getPort + "\n"
      }
    }
    str
  }
}
