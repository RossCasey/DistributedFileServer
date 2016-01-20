import java.net.{InetAddress, Socket}

/**
 * Created by Ross on 21/12/15.
 */
object LookupHandler {
  var lookupTable = new LookupTable()
  var nodeTable = new NodeTable()
  var serverUtility: ServerUtility = null


  /**
   * Performs a lookup for a node suitable for a read operation. If a suitable node exists
   * (ie file is in system) then it responds with node address, otherwise (file does not exist in system)
   * an error is returned.
   *
   * @param path - path of file to perform lookup on
   * @param connection - connection to send lookup result to
   * @param sessionKey - key with which to encrypt communication
   */
  def lookupForRead(path: String, connection: Connection, sessionKey: String): Unit = {
    val lookupResult = lookupTable.findEntryWithPath(path)
    if(lookupResult == null) {
      //send error if file not found
      val encPacket = Encryptor.encryptError(ErrorList.fileNotFound, "", sessionKey)
      connection.sendMessage(encPacket)
    } else {
      val readableNode = nodeTable.getReadableNode(lookupResult.getNodeId, serverUtility)

      val result = new LookupResultMessage(lookupResult.getFileId, readableNode.getIp, readableNode.getPort)
      val encPacket = Encryptor.encryptMessage(result, "", sessionKey)
      connection.sendMessage(encPacket)
    }
  }


  /**
   * Performs a lookup for a node suitable for a write operation. If a suitable node exists
   * (ie file is in system) then the primary that already has that file will be returned. Otherwise
   * (file is not in system) a primary will randomly be chosen to upload file to for first time.
   *
   * @param path - path of file to perform lookup on
   * @param connection - connection to send lookup result to
   * @param sessionKey - key with which to encrypt communication
   */
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


  /**
   * Handles the registration of a primary node.
   *
   * @param connection - connection to respond to (primary that is registering)
   * @param ip - IP of registering primary
   * @param port - port of registering primary
   * @param sessionKey - key with which to encrypt all communication
   */
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


  /**
   * Handles the registration of a replica node.
   *
   * @param connection - connection to respond to (primary that is registering)
   * @param primaryIP - IP of the primary to which registering node is a replica
   * @param primaryPort - port of the primary to which registering node is a replica
   * @param replicaIP - IP of registering replica
   * @param replicaPort - Port of registering replica
   * @param sessionKey - key with which to encrypt all communication
   */
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
