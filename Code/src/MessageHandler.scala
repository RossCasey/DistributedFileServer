/**
 * Created by Ross on 10/11/15.
 */
class MessageHandler(connection: Connection, serverUtility: ChatServerUtility) {

  //enum of message types
  private object MessageType extends Enumeration {
    type MessageType = Value
    val ReadFileRequest, WriteFileRequest, Error, Helo, Kill, FileIDList, FileHash, RegisterReplica = Value
  }

  /**
   * Determines the message type of an incoming user message and performs
   * all necessary actions to handle it appropriately.
   */
  def handleMessage(): Unit = {
    val firstLine = connection.nextLine()
    val messageType = getMessageType(firstLine)

    messageType match {
      case MessageType.Helo => handleHeloMessage(firstLine)
      case MessageType.Kill => handleKillMessage()

      case MessageType.ReadFileRequest => handleReadFileMessage(firstLine)
      case MessageType.WriteFileRequest => handleWriteFileMessage(firstLine)

      case MessageType.FileIDList => handleFileIDListMessage(firstLine)
      case MessageType.FileHash => handleFileHashMessage(firstLine)
      case MessageType.RegisterReplica => handleReplicaRegistration(firstLine)


      case MessageType.Error => handleUnknownPacket(firstLine)

    }
  }


  import MessageType._
  /**
   * Determines the message type of an incoming user message
   * @param firstLine - first line of the user's message
   * @return the type of message it is
   */
  private def getMessageType(firstLine: String): MessageType = {
    if(firstLine.startsWith("HELO")) return MessageType.Helo
    if(firstLine.startsWith("KILL_SERVICE")) return MessageType.Kill
    if(firstLine.startsWith("READ_FILE")) return MessageType.ReadFileRequest
    if(firstLine.startsWith("WRITE_FILE")) return MessageType.WriteFileRequest
    if(firstLine.startsWith("REQUEST_FILE_IDS")) return MessageType.FileIDList
    if(firstLine.startsWith("REQUEST_FILE_HASH")) return MessageType.FileHash

    MessageType.Error
  }


  /**
   * Handles a line that cannot be identifed
   * @param firstLine - first line of unidentified message
   */
  private def handleUnknownPacket(firstLine: String):Unit = {
    print("CONTENTS OF UNIDENTIFIED PACKET:")
    println(firstLine)
  }


  /**
   * Handles a helo message from a user
   * @param firstLine - first line of helo message
   */
  private def handleHeloMessage(firstLine: String): Unit = {
    val id = "cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3"
    connection.sendMessage(new HeloReplyMessage(firstLine, serverUtility.getIP, serverUtility.getPort, id))
  }


  /**
   * Handles a Kill server message sent by user
   */
  private def handleKillMessage(): Unit = {
    serverUtility.killServer
  }


  /**
   * Handles requests from connections to read from a file
   * @param firstLine - firstLine of request message
   */
  private def handleReadFileMessage(firstLine: String): Unit = {
    val fileIdentifier = firstLine.split(":")(1).trim
    FileHandler.sendFileToConnection(connection, fileIdentifier)
  }


  /**
   * Handles request from connections to write to a file
   * @param firstLine - file line of the request message
   */
  private def handleWriteFileMessage(firstLine: String): Unit = {
    val fileIdentifier = firstLine.split(":")(1).trim
    val length = Integer.parseInt(connection.nextLine().split(":")(1).trim)
    FileHandler.saveFile(connection, length, fileIdentifier, serverUtility)
  }


  private def handleFileIDListMessage(firstLine: String): Unit = {
    FileHandler.sendFileListToConnection(connection)
  }


  private def handleFileHashMessage(firstLine: String): Unit = {
    val fileIdentifier = firstLine.split(":")(1).trim
    FileHandler.sendHashToConnection(connection, fileIdentifier)
  }

  private def handleReplicaRegistration(firstLine: String): Unit = {
    val replicaIP = firstLine.split(":")(1).trim
    val replicaPort = connection.nextLine().split(":")(1).trim
    val replicaAddress = new NodeAddress(replicaIP, replicaPort)
    serverUtility.addReplicaServer(replicaAddress)
    connection.sendMessage(new RegisterResponseMessage)
  }
}
