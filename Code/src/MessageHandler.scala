/**
 * Created by Ross on 10/11/15.
 */
class MessageHandler(connection: Connection, serverUtility: ChatServerUtility) {

  //enum of message types
  private object MessageType extends Enumeration {
    type MessageType = Value
    val Join, Leave, Disconnect, Chat, Error, Helo, Kill, FileRequest = Value
  }

  /**
   * Determines the message type of an incoming user message and performs
   * all necessary actions to handle it appropriately.
   */
  def handleMessage(): Unit = {
    val firstLine = connection.nextLine
    val messageType = getMessageType(firstLine)

    messageType match {
      //case MessageType.Join => handleJoinMessage(firstLine)
      //case MessageType.Chat => handleChatMessage(firstLine)
      //case MessageType.Leave => handleLeaveMessage(firstLine)
      //case MessageType.Disconnect => handleDisconnectMessage(firstLine)
      case MessageType.Helo => handleHeloMessage(firstLine)
      case MessageType.Kill => handleKillMessage()

      case MessageType.FileRequest => handleReadFileMessage(firstLine)

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
    //if(firstLine.startsWith("JOIN_CHATROOM")) return MessageType.Join
    //if(firstLine.startsWith("LEAVE_CHATROOM")) return MessageType.Leave
    //if(firstLine.startsWith("DISCONNECT")) return MessageType.Disconnect
    //if(firstLine.startsWith("CHAT")) return MessageType.Chat
    if(firstLine.startsWith("HELO")) return MessageType.Helo
    if(firstLine.startsWith("KILL_SERVICE")) return MessageType.Kill
    if(firstLine.startsWith("READ_FILE")) return MessageType.FileRequest

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


  /*
  /**
   * Handles a join message from a user that wishes to join a particular server
   * @param firstLine - first line of the join message
   */
  private def handleJoinMessage(firstLine: String): Unit = {
    try {
      val chatRoomName = firstLine.split(":")(1).trim
      val clientIP = 0//will be 0 as TCP
      user.nextLine
      val clientPort = 0 //will be 0 as TCP
      user.nextLine
      val username = user.nextLine.split(":")(1).trim

      if(user.attemptToSetName(username)) {
        chatRoomHandler.joinOrCreateChatRoom(user, chatRoomName)
      } else {
        user.sendError(ErrorList.usernameAlreadyInUse)
      }
    } catch {
      case e: Exception => {
        user.sendError(ErrorList.malformedPacket)
      }
    }
  }


  /**
   * Handles leave message destined for a particular chat room from a user
   * @param firstLine - the first line of the leave message
   */
  private def handleLeaveMessage(firstLine: String): Unit = {
    try {
      val chatRoomId= Integer.parseInt(firstLine.split(":")(1).trim)
      val joinId = Integer.parseInt(user.nextLine.split(":")(1).trim)
      val clientName = user.nextLine.split(":")(1).trim
      chatRoomHandler.leaveChatRoom(user, chatRoomId)
    } catch {
      case e: Exception => {
        user.sendError(ErrorList.malformedPacket)
      }
    }
  }




  /**
   * Handles disconnect message sent by user to server
   * @param firstLine - firstLine of message
   */
  private def handleDisconnectMessage(firstLine: String): Unit = {
    try {
      val disconnect = firstLine.split(":")(1).trim
      val port = user.nextLine.split(":")(1).trim
      val username = user.nextLine.split(":")(1).trim

      if(username != user.getName) {
        user.sendError(ErrorList.usernameMismatch)
      } else {

        chatRoomHandler.handleDisconnect(user)
        user.requestConnectionClose()
      }
    } catch {
      case e: Exception => {
        user.sendError(ErrorList.malformedPacket)
      }
    }
  }


  /**
   * Handler the case where a user send a chat message destined for a chat room to the server
   * @param firstLine - the firstline of the message
   */
  private def handleChatMessage(firstLine: String): Unit = {
    try {
      val chatRoom = Integer.parseInt(firstLine.split(":")(1).trim)
      val joinId = Integer.parseInt(user.nextLine.split(":")(1).trim)
      val username = user.nextLine.split(":")(1).trim
      var message = user.nextLine.split(":")(1) +"\n"

      var nextMessagePart = user.nextLine + "\n"
      while(nextMessagePart.length > 1) {
        message = message + nextMessagePart
        nextMessagePart = user.nextLine + "\n"
      }


      chatRoomHandler.sendMessage(user, chatRoom, message)

    } catch {
      case e: Exception => {
        user.sendError(ErrorList.malformedPacket)
      }
    }
  }


  /**
   * Sends malformed packet error to user
   */
  private def handleError(): Unit = {
    user.sendError(ErrorList.malformedPacket)
  }


  /**
   * Determines whether id and username match
   * @param id - id to check
   * @param username - username to check
   * @return whether id and username match
   */
  private def areIdentifiersValid(id: Int, username: String): Boolean = {
    if(id != user.getId) {
      user.sendError(ErrorList.joinIdMismatch)
      return false
    }
    if(username != user.getName) {
      user.sendError(ErrorList.usernameMismatch)
      return false
    }
    true
  }

  */



  /**
   * Handles a Kill server message sent by user
   */
  private def handleKillMessage(): Unit = {
    serverUtility.killServer
  }

  private def handleReadFileMessage(firstLine: String): Unit = {
    val fileIdentifier = firstLine.split(":")(1).trim
    FileHandler.sendFileToConnection(connection, fileIdentifier)
  }
}
