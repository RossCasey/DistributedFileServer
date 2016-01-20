import ServerMessages.HeloReplyMessage

/**
 * Created by Ross on 10/11/15.
 */
class MessageHandler(connection: Connection, serverUtility: ServerUtility) {

  //enum of message types
  private object MessageType extends Enumeration {
    type MessageType = Value
    val  Error, Helo, Kill, LogonRequest = Value
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

      case MessageType.LogonRequest => handleLogonRequest(firstLine)

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
    if(firstLine.startsWith("USERNAME")) return MessageType.LogonRequest

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
    println("HANDLING HELO MESSAGE REQUEST FOR CONNECTION " + connection.getId)

    val id = "cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3"
    connection.sendMessage(new HeloReplyMessage(firstLine, serverUtility.getIP, serverUtility.getPort, id))
  }


  /**
   * Handles a Kill server message sent by user
   */
  private def handleKillMessage(): Unit = {
    println("HANDLING KILL SERVER REQUEST FOR CONNECTION " + connection.getId)

    serverUtility.killServer
  }



  private def handleLogonRequest(firstLine: String): Unit = {
    println("HANDLING LOGON REQUEST FOR CONNECTION " + connection.getId)

    val username = firstLine.split(":")(1).trim
    val encryptedPassphrase = connection.nextLine().split(":")(1).trim
    val serverIP = connection.nextLine().split(":")(1).trim
    val serverPort = connection.nextLine().split(":")(1).trim
    EncryptionHandler.replyWithToken(connection, username, encryptedPassphrase, serverIP, serverPort)
  }
}
