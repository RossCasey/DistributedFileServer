import scala.collection.mutable.ListBuffer

/**
 * Created by Ross on 10/11/15.
 */
class ChatRoom(id: Int, name: String, serverUtility: ChatServerUtility) {

  private val users: ListBuffer[User] = ListBuffer()

  /**
   * Handlers a user being added to the chat room
   * @param user - user to be added to chat room
   */
  def addUser(user: User): Unit = {
    if(isUserInGroup(user)) {
      user.sendError(ErrorList.userAlreadyInChatRoom)
    } else {
      users += user
      user.sendMessage(createJoinMessage(user))
      sendMessage(user, user.getName + " has joined this chatroom.\n")
    }
  }


  /**
   * Handles a user leaving this specific chat room
   * @param user - user leaving the chat room
   */
  def userLeaving(user: User): Unit = {
    if(!isUserInGroup(user)) {
      user.sendError(ErrorList.userNotInChatRoom)
    } else {
      user.sendMessage(createLeavingMessage(user))
      sendMessage(user, user.getName + " has left this chatroom.\n")
      removeUser(user)
    }
  }


  /**
   * Removes a user from the chat room records
   * @param user - user to remove
   */
  private def removeUser(user: User): Unit = {
    for(currentUser <- users) {
      if(currentUser.getId == user.getId) {
        users -= currentUser
      }
    }
  }


  /**
    * Handlers a disconnect request for this chatroom
    * @param user - user that is disconnecting
    */
  def handleUserDisconnect(user: User): Unit = {
    if(isUserInGroup(user)) {
      sendMessage(user, user.getName + " has left this chatroom.\n")
      removeUser(user)
    }
  }


  /**
   * Determines whether a user is part of the group
   * @param user - user to check
   * @return whether user is in group
   */
  def isUserInGroup(user: User): Boolean = {
    var exists = false
    for(currentUser <- users) {
      if(currentUser.getId == user.getId) {
        exists = true
      }
    }
    exists
  }


  /**
   * Broadcasts a message to every user in the chat room
   * @param user - the sender of the message
   * @param message - the message contents
   */
  def sendMessage(user: User, message: String):Unit = {
    val chatMessage = createChatMessage(user, message)
    for(user <- users) {
      user.sendMessage(chatMessage)
    }
  }


  /**
   * @return name of chat room
   */
  def getName: String = {
    name
  }


  /**
   * @return unique ID of chatroom
   */
  def getId: Int = {
    id
  }


  /**
   * Creates a personalised join message for a user that just joined
   * @param newUser - the user that just joined the chat room
   * @return a join message that can be sent to the new user
   */
  private def createJoinMessage(newUser: User): ServerMessage = {
    val chatRoomName = name
    val serverIP = serverUtility.getIP
    val port = serverUtility.getPort
    val roomRef = id
    val joinId = newUser.getId
    new JoinReplyMessage(chatRoomName, serverIP, port, roomRef, joinId)
  }


  /**
   * Creates a chat message that can be broadcast to every user in the chatroom
   * @param user - the user that sent the message
   * @param message - the message the user sent
   * @return chat message that can be sent to all users
   */
  private def createChatMessage(user: User, message: String): ChatReplyMessage = {
    val roomRef = id
    val username = user.getName
    new ChatReplyMessage(id, username, message)
  }


  /**
   * Returns a personalised leave message for the specified user
   * @param user - the user that is leaving the group
   * @return a message that can be sent to the user to indicate that they have left the chat room
   */
  private def createLeavingMessage(user: User): LeaveReplyMessage = {
    new LeaveReplyMessage(this.id, user.getId)
  }
}
