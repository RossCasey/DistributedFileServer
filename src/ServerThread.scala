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
    out.println(inMessage.toUpperCase)
    out.flush()
    socket.close()
  }
}