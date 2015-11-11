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
  private var userListener: UserListener = null


  def this(userListener: UserListener, id: Int, socket: Socket) {
    this()
    this.socket = socket
    this.userListener = userListener
    this.id = id

    in = new BufferedSource(socket.getInputStream()).getLines()
    out = new PrintStream(socket.getOutputStream())
  }


  def getNextLine: String = {
      in.next()
  }

  def sendMessage(message: ServerMessage): Unit = {
    out.print(message.toString)
    out.flush()
  }

  def sendError(error: Error): Unit = {
    out.print(error.toString)
    out.flush()
    userListener.removeUser(this)
  }

  def hasMessage: Boolean = {
    in.hasNext
  }

  def getId: Int = {
    id
  }

  def closeConnection(): Unit = {
    if(!socket.isClosed) {
      socket.close()
    }
    userListener.removeUser(this)
  }

  def attemptToSetName(name: String): Boolean = {
    if(userListener.isUsernameTaken(name)) {
      false
    } else {
      this.name = name
      true
    }
  }

  def getName: String = {
    name
  }
}
