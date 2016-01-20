import java.io.{InputStream, PrintStream}
import java.net._

import ServerMessages.ServerMessage

import scala.io.BufferedSource

/**
 * Created by Ross on 10/11/15.
 */
class Connection() {

  private var out: PrintStream = null
  private var id: Int = -1
  private var socket: Socket = null
  private var in: InputStream = null


  def this(id: Int, socket: Socket) {
    this()
    this.socket = socket
    this.id = id

    out = new PrintStream(socket.getOutputStream())
    in = socket.getInputStream()
  }

  /**
   * Gets the next line of input from the user
   * @return the next line of input sent by the user
   */
  def nextLine(): String = {
    var line = ""

    var nextChar = in.read()
    while(nextChar != 10) {
      line += nextChar.toChar
      nextChar = in.read()
    }
    println("RECEIVED on " + id + ": " + line)
    line
  }


  /**
   * Send a message to the user
   * @param message - message to send
   */
  def sendMessage(message: ServerMessage): Unit = {
    out.print(message.toString)
    out.flush()
    println("SENDING on " + id + ": " + message)
  }


  /**
   * Send error to user, remove from its from server records and close connection.
   * @param error - error to send
   */
  def sendError(error: Error): Unit = {
    out.print(error.toString)
    out.flush()
  }


  /**
   * Determines whether it is possible to read a message from the user
   * @return whether or not a message exists
   */
  def hasMessage: Boolean = {
    in.available() > 0
  }

  /**
   * Returns the ID of the connection
   */
  def getId: Int = {
    id
  }


  /**
   * Returns whether the TCP connection is still active
   */
  def isConnected: Boolean = {
    socket.isConnected
  }


  /**
   * Sends bytes to client at other end of connection
   * @param bytes - array of bytes to send
   */
  def sendBytes(bytes: Array[Byte]): Unit = {
    out.write(bytes)
    out.flush()
  }


  /**
   * Reads a single byte from connection
   * @return next byte from connection
   */
  def readByte(): Byte = {
    val aByte = Array[Byte](1)
    in.read(aByte)
    aByte(0)
  }


  /**
   * Ends communication over socket
   */
  def close(): Unit = {
    socket.close()
  }
}
