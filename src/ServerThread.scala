/**
 *  Echo Server Lab
 *  Ross Casey - ***REMOVED***
 */

import java.io._
import scala.io._
import java.net._

class ServerThread(socket: Socket, killServerCallback: () => Unit) extends Runnable {

  private var in: Iterator[String] = null
  private var out: PrintStream = null


  /**
   * Main
   */
  override def run() {
    println("Using thread: " + Thread.currentThread().getId)

    in = new BufferedSource(socket.getInputStream()).getLines()
    out = new PrintStream(socket.getOutputStream())

    val message = in.next()

    //process message and perform suitable operation
    if (isKillServiceCommand(message)) handleKillServerCommand()
    else if (isHeloMessage(message)) handleHeloMessage(message)
    else handleDefault(message)

    socket.close()
  }


  /**
   * Sends any passed data to the client.
   *
   * @param toSend: String - data to send to client
   */
  private def sendToClient(toSend: String): Unit = {
    out.print(toSend)
    out.flush()
  }


  /**
   * Determines whether the message received from the user is a valid
   * command to kill the server.
   *
   * @param input: String - message received from client
   * @return Boolean - whether message is valid kill command
   */
  private def isKillServiceCommand(input: String): Boolean = {
    if (input.equals("KILL_SERVICE")) {
      true
    } else {
      false
    }
  }


  /**
   * Handle kill command from client by killing the server
   * via callback to main server.
   */
  private def handleKillServerCommand(): Unit = {
    killServerCallback()
  }

  /**
   * Determines whether input received is a "helo message",
   * which is of the form: "HELO text\n"
   *
   * @param input: String - the message received from the client
   * @return Boolean - whether message from client is "helo message"
   */
  private def isHeloMessage(input: String): Boolean = {
    if (input.startsWith("HELO ")) {
      true
    } else {
      false
    }
  }


  /**
   * For incoming messages starting with "HELO x" returns:
   *    HELO x
   *    IP: [IP ADDRESS OF CLIENT]
   *    Port: [PORT NUMBER]
   *    StudentID: [STUDENT ID]
   *
   *
   * @param input - the message received from the client
   */
  private def handleHeloMessage(input: String): Unit = {
    val ip = socket.getInetAddress.toString.substring(1)
    val port = socket.getPort.toString
    val id = "***REMOVED***"
    val output = input + "\nIP:" + ip + "\nPort:" + port + "\nStudentID:" + id +"\n"
    sendToClient(output)
  }


  /**
   * TO-DO
   * @param input
   */
  private def handleDefault(input: String) = {
    //do nothing for now
  }
}