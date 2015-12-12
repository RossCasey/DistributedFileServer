//import java.net.Socket
//
//import scala.collection.mutable.ListBuffer
//
//
//
////user handler implementation
//class UserHandlerImplementation(serverUtility: ChatServerUtility) extends UserHandler {
//
//  private val users: ListBuffer[Connection] = ListBuffer()
//  private var userId = 0
//  private val chatRoomHandler: ChatRoomHandler = new ChatRoomHandlerImplementation(serverUtility)
//
//
//  /**
//   * Gets the next available unique ID for a user
//   * @return unique ID for user
//   */
//  private def computeNextId(): Int = {
//    val newId = userId
//    userId += 1
//    newId
//  }
//
//
//  /**
//   * Adds a user to the servers records and creates and executes a new thread to
//   * handle all communications with that user
//   * @param socket - socket user connected on
//   */
//  def addUser(socket: Socket): Unit = {
//    val newUser = new Connection(this, computeNextId(), socket)
//    users += newUser
//    println("Creating worker thread for user: " + newUser.getId)
//    serverUtility.execute(new ConnectionInputListener(newUser,chatRoomHandler, serverUtility))
//  }
//
//
//  /**
//   * Remove a user that has disconnected from the records
//   * @param user - user to remove
//   */
//  def removeUser(user: Connection): Unit = {
//    user.closeConnection()
//    users -= user
//  }
//
//
//  /**
//   * Determines whether a username is taken by an existing user
//   * @param username - username to check the availability of
//   * @return whether username is taken
//   */
//  def isUsernameTaken(username: String): Boolean = {
//    for(user <- users) {
//      if(user.getName == username) {
//        return true
//      }
//    }
//    false
//  }
//}
