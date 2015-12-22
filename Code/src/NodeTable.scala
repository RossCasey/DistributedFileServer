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


  private def save(): Unit = {
    var outputStream = new ObjectOutputStream(new FileOutputStream("./NodeTable.lt"))
    outputStream.writeObject(entries)

    outputStream = new ObjectOutputStream(new FileOutputStream("./NodeTableId.lt"))
    outputStream.writeObject(nextAvailableId)

    outputStream.close()
  }



  private def pingServer(ip: String, port: String): Boolean = {
    try {
      lazy val socket = new Socket(InetAddress.getByName(ip), Integer.parseInt(port))
      val connection = new Connection(0, socket)
      socket.setSoTimeout(5000)
      connection.sendMessage(new PingMessage)

      val response = connection.nextLine()
      if(response.startsWith("PONG")) {
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


  def getReadableNode(id: String): NodeTableEntry = {
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
      Random.shuffle(replicas)
      for(replica <- replicas) {
        if(pingServer(replica.getIp, replica.getPort)) {
          return replica
        }
      }
    }

    //if there are no alive replicas, return pirmary
    primary
  }


  def getWritableNodeWithId(id: String): NodeTableEntry = {
    for(entry <- entries) {
      if(entry.getId == id && !entry.getIsReplica) {
        return entry
      }
    }
    return null
  }


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

  def findNode(ip: String, port: String): NodeTableEntry = {
    for(entry <- entries) {
      if(entry.getIp == ip && entry.getPort == port) {
        return entry
      }
    }
    return null
  }

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
