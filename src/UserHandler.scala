import java.net.Socket

import scala.collection.mutable.ListBuffer

/**
 * Created by Ross on 10/11/15.
 */

trait UserHandler extends {
  def addUser(socket: Socket)
  def removeUser(user: User)
  def isUsernameTaken(username: String): Boolean
  def checkAllConnections(): Unit
}


class UserHandlerImplementation(serverUtility: ChatServerUtility) extends UserHandler {

  private val users: ListBuffer[User] = ListBuffer()
  private var userId = 0
  private val chatRoomHandler: ChatRoomHandler = new ChatRoomHandlerImplementation(serverUtility)

  private def computeNextId(): Int = {
    val newId = userId
    userId += 1
    newId
  }


  def addUser(socket: Socket): Unit = {
    val newUser = new User(this, computeNextId(), socket)
    users += newUser
    serverUtility.execute(new UserInputListener(newUser,chatRoomHandler, serverUtility))
  }


  def removeUser(user: User): Unit = {
    user.closeConnection()
    users -= user
    serverUtility.execute(new DisconnectHandler(user,chatRoomHandler))
  }

  def isUsernameTaken(username: String): Boolean = {
    for(user <- users) {
      if(user.getName == username) {
        return true
      }
    }
    false
  }

  def checkAllConnections(): Unit = {

  }
}
