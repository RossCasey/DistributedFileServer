/**
 * Created by Ross on 10/11/15.
 */
class MessageHandler(user: User, chatRoomHandler: ChatRoomHandler, serverUtility: ChatServerUtility) {

  object MessageType extends Enumeration {
    type MessageType = Value
    val Join, Leave, Disconnect, Chat, Error, Helo, Kill = Value
  }

  def handleMessage(): Unit = {
    println("Handling message for: " + user.getId + "/" + user.getName)
    val firstLine = user.getNextLine
    val messageType = getMessageType(firstLine)

    messageType match {
      case MessageType.Join => handleJoinMessage(firstLine)
      case MessageType.Chat => handleChatMessage(firstLine)
      case MessageType.Leave => handleLeaveMessage(firstLine)
      case MessageType.Disconnect => handleDisconnectMessage(firstLine)
      case MessageType.Helo => handleHeloMessage(firstLine)
      case MessageType.Kill => handleKillMessage()
      case MessageType.Error => handleError()
    }
  }

  import MessageType._
  def getMessageType(firstLine: String): MessageType = {
    if(firstLine.startsWith("JOIN_CHATROOM")) return MessageType.Join
    if(firstLine.startsWith("LEAVE_CHATROOM")) return MessageType.Leave
    if(firstLine.startsWith("DISCONNECT")) return MessageType.Disconnect
    if(firstLine.startsWith("CHAT")) return MessageType.Chat
    if(firstLine.startsWith("HELO")) return MessageType.Helo
    if(firstLine.startsWith("KILL_SERVICE")) return MessageType.Kill
    MessageType.Error
  }


  def handleHeloMessage(firstLine: String): Unit = {
    val ip = user.getSocket.getLocalAddress.toString.substring(1)
    val id = "cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3"
    user.sendMessage(new HeloReplyMessage(firstLine, ip, serverUtility.getPort, id))
  }

  def handleJoinMessage(firstLine: String): Unit = {
    try {
      val chatRoomName = firstLine.split(":")(1).trim
      val clientIP = 0//will be 0 as TCP
      user.getNextLine
      val clientPort = 0 //will be 0 as TCP
      user.getNextLine
      val username = user.getNextLine.split(":")(1).trim

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

  def handleLeaveMessage(firstLine: String): Unit = {
    try {
      val chatRoomId= Integer.parseInt(firstLine.split(":")(1).trim)
      val joinId = Integer.parseInt(user.getNextLine.split(":")(1).trim)
      val clientName = user.getNextLine.split(":")(1).trim
      chatRoomHandler.leaveChatRoom(user, chatRoomId)
    } catch {
      case e: Exception => {
        user.sendError(ErrorList.malformedPacket)
      }
    }
  }

  def handleKillMessage(): Unit = {
    serverUtility.killServer
  }

  def handleDisconnectMessage(firstLine: String): Unit = {
    try {
      val disconnect = firstLine.split(":")(1).trim
      val port = user.getNextLine.split(":")(1).trim
      val username = user.getNextLine.split(":")(1).trim

      if(username != user.getName) {
        user.sendError(ErrorList.usernameMismatch)
      } else {
        user.requestConnectionClose()
      }
    } catch {
      case e: Exception => {
        user.sendError(ErrorList.malformedPacket)
      }
    }
  }

  def handleChatMessage(firstLine: String): Unit = {
    try {
      val chatRoom = Integer.parseInt(firstLine.split(":")(1).trim)
      val joinId = Integer.parseInt(user.getNextLine.split(":")(1).trim)
      val username = user.getNextLine.split(":")(1).trim
      var message = user.getNextLine.split(":")(1)

      var nextMessagePart = user.getNextLine
      while(nextMessagePart.length != 0) {
        message = message + nextMessagePart
        nextMessagePart = user.getNextLine
      }


      chatRoomHandler.sendMessage(user, chatRoom, message)

    } catch {
      case e: Exception => {
        user.sendError(ErrorList.malformedPacket)
      }
    }
  }


  def handleError(): Unit = {
    user.sendError(ErrorList.malformedPacket)
  }

  def areIdentifiersValid(id: Int, username: String): Boolean = {
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


}
