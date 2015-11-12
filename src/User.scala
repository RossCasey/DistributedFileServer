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


  def getNextLine: String = {
    val message = in.next()
    println("RECEIVED on " + name + ": " + message)
    message
  }

  def sendMessage(message: ServerMessage): Unit = {
    out.print(message.toString)
    out.flush()
    println("SENDING on " + name + ": " + message)

    if(message.toString.contains("hello world from client 1") && name == "client3") {
      userHandler.checkAllConnections()
    }
  }

  def sendError(error: Error): Unit = {
    out.print(error.toString)
    out.flush()
    userHandler.removeUser(this)
  }

  def hasMessage: Boolean = {
    //socket.getInputStream.available() != 0
    in.hasNext
  }

  def getId: Int = {
    id
  }

  def closeConnection(): Unit = {
    if(!socket.isClosed) {
      socket.close()
    }
  }

  def requestConnectionClose(): Unit = {
    userHandler.removeUser(this)
  }

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

  def getName: String = {
    name
  }

  def getSocket: Socket = {
    socket
  }

  def sendAny(m: String): Unit = {
    out.print(m)
    out.flush()
  }

}
