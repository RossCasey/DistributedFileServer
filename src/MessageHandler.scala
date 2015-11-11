/**
 * Created by Ross on 10/11/15.
 */
class MessageHandler(user: User, chatRoomHandler: ChatRoomHandler) extends Runnable {

  object MessageType extends Enumeration {
    type MessageType = Value
    val Join, Leave, Disconnect, Chat, Error = Value
  }


  override def run(): Unit = {
    val firstLine = user.getNextLine
    val messageType = getMessageType(firstLine)

    messageType match {
      case MessageType.Join => handleJoinMessage(firstLine)
      case MessageType.Chat => handleChatMessage(firstLine)
      case MessageType.Leave => handleLeaveMessage(firstLine)
      case MessageType.Disconnect => handleDisconnectMessage(firstLine)
      case MessageType.Error => handleError()
    }
  }

  import MessageType._
  def getMessageType(firstLine: String): MessageType = {
    if(firstLine.startsWith("JOIN_CHATROOM")) return MessageType.Join
    if(firstLine.startsWith("LEAVE_CHATROOM")) return MessageType.Leave
    if(firstLine.startsWith("DISCONNECT")) return MessageType.Disconnect
    if(firstLine.startsWith("CHAT")) return MessageType.Chat
    MessageType.Error
  }

  def handleJoinMessage(firstLine: String): Unit = {
    try {
      val chatRoomName = firstLine.split(":")(1).trim
      val clientIP = user.getNextLine.split(":")(1).trim //will be 0 as TCP
      val clientPort = user.getNextLine.split(":")(1).trim //will be 0 as TCP
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

  def handleDisconnectMessage(firstLine: String): Unit = {
    try {
      val disconnect = firstLine.split(":")(1).trim
      val port = user.getNextLine.split(":")(1).trim
      val username = user.getNextLine.split(":")(1).trim

      if(username != user.getName) {
        user.sendError(ErrorList.usernameMismatch)
      } else {
        user.closeConnection()
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
      var message = ""
      if(areIdentifiersValid(joinId, username)) {
        while(user.getNextLine != "") {
          message = message + user.getNextLine + "\n"
        }
        chatRoomHandler.sendMessage(user, chatRoom, message)
      }
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
