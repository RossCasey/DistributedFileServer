/**
 * Created by Ross on 10/11/15.
 */
class MessageHandler(connection: Connection, serverUtility: ChatServerUtility) {

  //enum of message types
  private object MessageType extends Enumeration {
    type MessageType = Value
    val Error, Helo, Kill, Lookup, RegisterPrimary, RegisterReplica = Value
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
      case MessageType.Lookup => handleLookup(firstLine)
      case MessageType.RegisterPrimary => handlePrimaryRegistration(firstLine)
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
    if(firstLine.startsWith("LOOKUP")) return MessageType.Lookup
    if(firstLine.startsWith("REGISTER_PRIMARY"))  return MessageType.RegisterPrimary
    if(firstLine.startsWith("REGISTER_REPLICA"))  return MessageType.RegisterReplica

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


  private def handleLookup(firstLine: String): Unit = {
    val path = firstLine.split(":")(1).trim
    val lookupType = connection.nextLine().split(":")(1).trim

    if(lookupType == "READ") {
      LookupHandler.lookupForRead(path, connection)
    } else if (lookupType == "WRITE") {
      LookupHandler.lookupForWrite(path, connection)
    } else {
      connection.sendError(ErrorList.malformedPacket)
    }
  }



  private def handlePrimaryRegistration(firstLine: String): Unit = {
    val ip = firstLine.split(":")(1).trim
    val port = connection.nextLine().split(":")(1).trim
    LookupHandler.registerPrimary(connection, ip, port)
  }

  private def handleReplicaRegistration(firstLine: String): Unit = {
    val replicaIP = firstLine.split(":")(1).trim
    val replicaPort = connection.nextLine().split(":")(1).trim
    val primaryIP = connection.nextLine().split(":")(1).trim
    val primaryPort = connection.nextLine().split(":")(1).trim
    LookupHandler.registerReplica(connection, primaryIP, primaryPort, replicaIP, replicaPort)
  }
}
