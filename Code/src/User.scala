import java.io.PrintStream
import java.net._

import scala.io.BufferedSource

/**
 * Created by Ross on 10/11/15.
 */
class User() {

  private var in: Iterator[String] = null
  private var out: PrintStream = null
  private var id: Int = -1
  private var socket: Socket = null
  private var name: String = null
  private var userHandler: UserHandler = null


  def this(userHandler: UserHandler, id: Int, socket: Socket) {
    this()
    this.socket = socket
    this.userHandler = userHandler
    this.id = id

    in = new BufferedSource(socket.getInputStream()).getLines()
    out = new PrintStream(socket.getOutputStream())
  }

  /**
   * Gets the next line of input from the user
   * @return the next line of input sent by the user
   */
  def nextLine(): String = {
    val message = in.next()
    println("RECEIVED on "  + id + "/" + name + ": " + message)
    message
  }


  /**
   * Send a message to the user
   * @param message - message to send
   */
  def sendMessage(message: ServerMessage): Unit = {
    out.print(message.toString)
    out.flush()
    println("SENDING on " + id + "/" + name + ": " + message)
  }


  /**
   * Send error to user, remove from its from server records and close connection.
   * @param error - error to send
   */
  def sendError(error: Error): Unit = {
    out.print(error.toString)
    out.flush()
    userHandler.removeUser(this)
  }


  /**
   * Determines whether it is possible to read a message from the user
   * @return whether or not a message exists
   */
  def hasMessage: Boolean = {
    //socket.getInputStream.available() != 0
    in.hasNext
  }


  /**
   * @return user's ID
   */
  def getId: Int = {
    id
  }


  /**
   * Close a user's socket connection
   */
  def closeConnection(): Unit = {
    if(!socket.isClosed) {
      socket.close()
    }
  }


  /**
   * Request the user to remove themselves from the server records and close their connection
   */
  def requestConnectionClose(): Unit = {
    userHandler.removeUser(this)
  }


  /**
   * Attempt to acquire specified username
   * @param name - name to acquire
   * @return whether it was able to secure that name
   */
  def attemptToSetName(name: String): Boolean = {
    if(this.name == null) {
      if(userHandler.isUsernameTaken(name)) {
        return false
      } else {
        this.name = name
        return true
      }
    } else {
      true
    }
  }


  /**
   * @return user's username
   */
  def getName: String = {
    name
  }


  /**
   * @return user's socket
   */
  def getSocket: Socket = {
    socket
  }
}
