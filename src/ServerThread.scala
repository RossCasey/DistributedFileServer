/**
 *  Echo Server Lab
 *  Ross Casey - 12301716
 */

import java.io._
import scala.io._
import java.net._

class ServerThread(socket: Socket, killServerCallback: () => Unit) extends Runnable {

  override def run() {
    val in = new BufferedSource(socket.getInputStream()).getLines()
    val out = new PrintStream(socket.getOutputStream())

    val inMessage = in.next()
    inMessage match {
      case "KILL_SERVER" => {

      }

      case ""
    }



    val inMessage = in.next()
    out.println(inMessage.toUpperCase)
    out.flush()
    socket.close()



  }

  def isKillService(input: String): Boolean = {
    if (input.equals("KILL_SERVER")) {
      true
    } else {
      false
    }
  }

  def handleKillServer(): Unit = {
    killServerCallback()
  }

  def isHeloMessage(input: String): Boolean = {
    if (input.startsWith("HELO ")) {
      true
    } else {
      false
    }
  }

  def handleHeloMessage(input: String): String = {
    val ip = socket.getInetAddress.toString
    val port = socket.getPort.toString
    val id = "12301716"
    input + "IP:" + ip + "\nPort:" + port + "\nStudentID:" + id + "\n"
  }
}