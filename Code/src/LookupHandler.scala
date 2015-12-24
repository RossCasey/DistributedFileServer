import java.net.{InetAddress, Socket}

/**
 * Created by Ross on 21/12/15.
 */
object LookupHandler {
  var lookupTable = new LookupTable()
  var nodeTable = new NodeTable()



  def lookupForRead(path: String, connection: Connection, sessionKey: String): Unit = {
    val lookupResult = lookupTable.findEntryWithPath(path)
    if(lookupResult == null) {
      //send error if file not found
      val encPacket = Encryptor.encryptError(ErrorList.fileNotFound, "", sessionKey)
      connection.sendMessage(encPacket)
    } else {
      val readableNode = nodeTable.getReadableNode(lookupResult.getNodeId)

      val result = new LookupResultMessage(lookupResult.getFileId, readableNode.getIp, readableNode.getPort)
      val encPacket = Encryptor.encryptMessage(result, "", sessionKey)
      connection.sendMessage(encPacket)
    }
  }


  def lookupForWrite(path: String, connection: Connection, sessionKey: String): Unit = {
    var lookupResult = lookupTable.findEntryWithPath(path)
    if(lookupResult == null) {
      //if the file does not exist it will be written to one of the primaries
      val randomPrimary = nodeTable.getAnyWritableNodeForNewFile()

      lookupTable.add(randomPrimary.getId, path)
      lookupResult = lookupTable.findEntryWithPath(path)
    }

    val writableNode = nodeTable.getWritableNodeWithId(lookupResult.getNodeId)

    val result = new LookupResultMessage(lookupResult.getFileId, writableNode.getIp, writableNode.getPort)
    val encPacket = Encryptor.encryptMessage(result, "", sessionKey)
    connection.sendMessage(encPacket)
  }


  def registerPrimary(connection: Connection, ip: String, port: String, sessionKey: String): Unit = {
    if(!nodeTable.addPrimary(ip, port)) {
      val encPacket = Encryptor.encryptError(ErrorList.nodeAlreadyExists, "", sessionKey)
      connection.sendMessage(encPacket)
      println(nodeTable.toString)
    } else {
      val encPacket = Encryptor.encryptMessage(new RegisterResponseMessage, "", sessionKey)
      connection.sendMessage(encPacket)
    }
  }

  def registerReplica(connection: Connection, primaryIP: String, primaryPort: String, replicaIP: String, replicaPort: String, sessionKey: String): Unit = {
    val primary = nodeTable.findNode(primaryIP, primaryPort)
    if(primary == null) {
      val encPacket = Encryptor.encryptError(ErrorList.nodeNotFound, "", sessionKey)
      connection.sendMessage(encPacket)
    } else {
      if(!nodeTable.addReplica(primary.getId, replicaIP, replicaPort)) {
        val encPacket = Encryptor.encryptError(ErrorList.nodeAlreadyExists, "", sessionKey)
        connection.sendMessage(encPacket)
      } else {
        val encPacket = Encryptor.encryptMessage(new RegisterResponseMessage, "", sessionKey)
        connection.sendMessage(encPacket)
      }
    }
  }
}
