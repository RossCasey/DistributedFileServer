import java.io.PrintStream
import java.net.Socket

import scala.io.BufferedSource

/**
 * Created by Ross on 11/11/15.
 */
class TestThread(socket: Socket) extends Runnable{

  override def run(): Unit = {
    var in = new BufferedSource(socket.getInputStream()).getLines()
    var out = new PrintStream(socket.getOutputStream())


    println(in.next())
    println(in.next())
    println(in.next())
    println(in.next())

    out.print("JOINED_CHATROOM: room1\n")
    out.print("SERVER_IP: 0.0.0.0\n")
    out.print("PORT: 8001\n")
    out.print("ROOM_REF: 0\n")
    out.print("JOIN_ID: 1\n")
    out.println("CHAT: 0")
    out.println("CLIENT_NAME: client1")
    out.println("MESSAGE: client1 has joined this chatroom.\n")

    println(in.next())


  }

}
