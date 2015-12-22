import java.net.{InetAddress, Socket}

/**
 * Created by Ross on 21/12/15.
 */
object LookupHandler {
  var lookupTable = new LookupTable()
  var nodeTable = new NodeTable()



  def lookupForRead(path: String, connection: Connection): Unit = {
    val lookupResult = lookupTable.findEntryWithPath(path)
    if(lookupResult == null) {
      //send error if file not found
      connection.sendError(ErrorList.fileNotFound)
    } else {
      val readableNode = nodeTable.getReadableNode(lookupResult.getNodeId)
      connection.sendMessage(new LookupResultMessage(lookupResult.getFileId, readableNode.getIp, readableNode.getPort))
    }
  }


  def lookupForWrite(path: String, connection: Connection): Unit = {
    var lookupResult = lookupTable.findEntryWithPath(path)
    if(lookupResult == null) {
      //if the file does not exist it will be written to one of the primaries
      val randomPrimary = nodeTable.getAnyWritableNodeForNewFile()

      lookupTable.add(randomPrimary.getId, path)
      lookupResult = lookupTable.findEntryWithPath(path)
    }

    val writableNode = nodeTable.getWritableNodeWithId(lookupResult.getNodeId)


    connection.sendMessage(new LookupResultMessage(lookupResult.getFileId, writableNode.getIp, writableNode.getPort))
  }


  def registerPrimary(connection: Connection, ip: String, port: String): Unit = {
    if(!nodeTable.addPrimary(ip, port)) {
      connection.sendError(ErrorList.nodeAlreadyExists)
      println(nodeTable.toString)
    } else {
      connection.sendMessage(new RegisterResponseMessage)
    }
  }

  def registerReplica(connection: Connection, primaryIP: String, primaryPort: String, replicaIP: String, replicaPort: String): Unit = {
    val primary = nodeTable.findNode(primaryIP, primaryPort)
    if(primary == null) {
      connection.sendError(ErrorList.nodeNotFound)
    } else {
      if(!nodeTable.addReplica(primary.getId, replicaIP, replicaPort)) {
        connection.sendError(ErrorList.nodeAlreadyExists)
      } else {
        connection.sendMessage(new RegisterResponseMessage)
      }
    }

  }

}
