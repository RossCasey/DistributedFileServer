import scala.collection.mutable.ListBuffer

/**
 * Created by Ross on 11/11/15.
 */

trait ChatRoomHandler {
  def joinOrCreateChatRoom(user: User, chatRoomName: String): Unit
  def leaveChatRoom(user: User, chatRoomId: Int): Unit
  def handleDisconnect(user: User): Unit
  def sendMessage(user: User, chatRoomRef: Int, message: String): Unit
}



class ChatRoomHandlerImplementation(serverUtility: ChatServerUtility) extends ChatRoomHandler {
  private val chatRooms: ListBuffer[ChatRoom] = ListBuffer()

  private var chatRoomId = 0
  private def getNextId(): Int = {
    val newId = chatRoomId
    chatRoomId += 1
    newId
  }

  def joinOrCreateChatRoom(user: User, chatRoomName: String): Unit = {
    if(doesChatRoomExist(chatRoomName)) {
      joinChatRoom(user, chatRoomName)
    } else {
      createChatRoom(user, chatRoomName)
    }
  }

  def leaveChatRoom(user: User, chatRoomId: Int): Unit = {
    var chatRoomToLeave: ChatRoom = null
    for(chatRoom <- chatRooms) {
      if(chatRoom.getId == chatRoomId) {
        chatRoomToLeave = chatRoom
      }
    }

    if(chatRoomToLeave == null) {
      user.sendError(ErrorList.chatRoomDoesNotExist)
    } else {
      chatRoomToLeave.userLeaving(user)
    }
  }

  def handleDisconnect(user: User): Unit = {
    for(chatRoom <- chatRooms) {
      chatRoom.handleUserDisconnect(user)
    }
  }

  private def doesChatRoomExist(chatRoomName: String): Boolean = {
    for(chatRoom <- chatRooms) {
      if(chatRoom.getName == chatRoomName) {
        true
      }
    }
    false
  }

  private def doesChatRoomExist(chatRoomRef: Int): Boolean = {
    for(chatRoom <- chatRooms) {
      if(chatRoom.getId == chatRoomRef) {
        true
      }
    }
    false
  }

  private def joinChatRoom(user: User, chatRoomName: String): Unit = {
    var chatRoomToJoin: ChatRoom = null
    for(chatRoom <- chatRooms) {
      if(chatRoom.getName == chatRoomName) {
        chatRoomToJoin = chatRoom
      }
    }
    chatRoomToJoin.addUser(user)
  }

  private def createChatRoom(user: User, chatRoomName: String): Unit = {
    var newChatRoom = new ChatRoom(getNextId(), chatRoomName, serverUtility)
    chatRooms += newChatRoom
    newChatRoom.addUser(user)
  }

  private def getChatRoom(chatRoomRef: Int): ChatRoom = {
    for(chatRoom <- chatRooms) {
      if(chatRoom.getId == chatRoomRef) {
        return chatRoom
      }
    }
    null
  }

  def sendMessage(user: User, chatRoomRef: Int, message: String): Unit = {
    if(doesChatRoomExist(chatRoomRef)) {
      getChatRoom(chatRoomRef).sendMessage(user, message: String)
    }
  }


}
