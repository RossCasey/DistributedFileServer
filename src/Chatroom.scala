import scala.collection.mutable.ListBuffer

/**
 * Created by Ross on 10/11/15.
 */
class ChatRoom(id: Int, name: String, serverUtility: ChatServerUtility) {

  val users: ListBuffer[User] = null

  def addUser(user: User): Unit = {
    if(isUserInGroup(user)) {
      user.sendError(ErrorList.userAlreadyInChatRoom)
    } else {
      users += user
      user.sendMessage(createJoinMessage(user))
    }
  }

  def userLeaving(user: User): Unit = {
    if(!isUserInGroup(user)) {
      user.sendError(ErrorList.userNotInChatRoom)
    } else {
      user.sendMessage(createLeavingMessage(user))
      removeUser(user)
    }
  }

  private def removeUser(user: User): Unit = {
    for(currentUser <- users) {
      if(currentUser.getId == user.getId) {
        users -= currentUser
      }
    }
  }


  def handleUserDisconnect(user: User): Unit = {
    if(isUserInGroup(user)) {
      removeUser(user)
    }
  }

  def isUserInGroup(user: User): Boolean = {
    var exists = false
    for(currentUser <- users) {
      if(currentUser.getId == user.getId) {
        exists = true
      }
    }
    exists
  }


  def sendMessage(user: User, message: String):Unit = {
    val chatMessage = createChatMessage(user, message)
    for(user <- users) {
      user.sendMessage(chatMessage)
    }
  }

  def getName: String = {
    name
  }

  def getId: Int = {
    id
  }

  def createJoinMessage(newUser: User): ServerMessage = {
    val chatRoomName = name
    val serverIP = serverUtility.getIP
    val port = serverUtility.getPort
    val roomRef = id
    val joinId = newUser.getId
    new JoinReplyMessage(chatRoomName, serverIP, port, roomRef, joinId)
  }

  def createChatMessage(user: User, message: String): ChatReplyMessage = {
    val roomRef = id
    val username = user.getName
    new ChatReplyMessage(id, username, message)
  }

  def createLeavingMessage(user: User): LeaveReplyMessage = {
    new LeaveReplyMessage(this.id, user.getId)
  }


}
