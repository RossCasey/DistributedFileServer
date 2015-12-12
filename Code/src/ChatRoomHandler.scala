import scala.collection.mutable.ListBuffer

/**
 * Created by Ross on 11/11/15.
 */


//interface for chat room handler
trait ChatRoomHandler {
  def joinOrCreateChatRoom(user: User, chatRoomName: String): Unit
  def leaveChatRoom(user: User, chatRoomId: Int): Unit
  def handleDisconnect(user: User): Unit
  def sendMessage(user: User, chatRoomRef: Int, message: String): Unit
}


//implementation of chat room handler
class ChatRoomHandlerImplementation(serverUtility: ChatServerUtility) extends ChatRoomHandler {

  private val chatRooms: ListBuffer[ChatRoom] = ListBuffer()


  /**
   * Computetes the next available unique chat room id
   * @return id for next chat room
   */
  private var chatRoomId = 0
  private def getNextId(): Int = {
    val newId = chatRoomId
    chatRoomId += 1
    newId
  }


  /**
   * Handles user joining chat room. If a chatroom with the specified name does not exist then a new
   * chat room with that name is created.
   *
   * @param user - user joining chat room
   * @param chatRoomName - name of chat room to join or create
   */
  def joinOrCreateChatRoom(user: User, chatRoomName: String): Unit = {
    if(doesChatRoomExist(chatRoomName)) {
      joinChatRoom(user, chatRoomName)
    } else {
      createChatRoom(user, chatRoomName)
    }
  }


  /**
   * Handler a user leaving a particular chat room
   * @param user - user leaving
   * @param chatRoomId - id of chat room user is leaving
   */
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


  /**
   * Handles a user disconnecting from the server by removing them
   * from all chat room they are currently in
   *
   * @param user - user that is disconnecting
   */
  def handleDisconnect(user: User): Unit = {
    for(chatRoom <- chatRooms) {
      chatRoom.handleUserDisconnect(user)
    }
  }


  /**
   * Determines whether or not chat room exists based on the chat room name
   * @param chatRoomName - name of chat room to find
   * @return whether chat room exists
   */
  private def doesChatRoomExist(chatRoomName: String): Boolean = {
    for(chatRoom <- chatRooms) {
      if(chatRoom.getName == chatRoomName) {
        return true
      }
    }
    false
  }


  /**
   * Determines whether chat room exists based on chat room reference number
   * @param chatRoomRef - reference number of chat room to find
   * @return whether or not chat room exists
   */
  private def doesChatRoomExist(chatRoomRef: Int): Boolean = {
    for(chatRoom <- chatRooms) {
      if(chatRoom.getId == chatRoomRef) {
        return true
      }
    }
    false
  }


  /**
   * Join a user to an existing chat room
   * @param user - user trying to join chat room
   * @param chatRoomName - name of the chat room user is joining
   */
  private def joinChatRoom(user: User, chatRoomName: String): Unit = {
    var chatRoomToJoin: ChatRoom = null
    for(chatRoom <- chatRooms) {
      if(chatRoom.getName == chatRoomName) {
        chatRoomToJoin = chatRoom
      }
    }
    chatRoomToJoin.addUser(user)
  }


  /**
   * Creates a new chat room
   * @param user - user requesting to join new chat room
   * @param chatRoomName - the name of the new chat room
   */
  private def createChatRoom(user: User, chatRoomName: String): Unit = {
    var newChatRoom = new ChatRoom(getNextId(), chatRoomName, serverUtility)
    chatRooms += newChatRoom
    newChatRoom.addUser(user)
  }


  /**
   * Returns a chat room based on the reference number for a chat room
   * @param chatRoomRef - chat room to find
   * @return chat room with passed reference number
   */
  private def getChatRoom(chatRoomRef: Int): ChatRoom = {
    for(chatRoom <- chatRooms) {
      if(chatRoom.getId == chatRoomRef) {
        return chatRoom
      }
    }
    null
  }


  /**
   * Sends a message from a user to a particular chat room
   * @param user - send of the message
   * @param chatRoomRef - chat room to send message to
   * @param message - message contents
   */
  def sendMessage(user: User, chatRoomRef: Int, message: String): Unit = {
    if(doesChatRoomExist(chatRoomRef)) {
      getChatRoom(chatRoomRef).sendMessage(user, message: String)
    }
  }
}
