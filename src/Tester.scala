import java.io.PrintStream
import java.net.Socket

import scala.io.BufferedSource

/**
 * Created by Ross on 12/11/15.
 */
object Tester {




  def main(args: Array[String]): Unit = {
    val socket1 = new Socket("localhost", 8001)
    val inSocket1 = new BufferedSource(socket1.getInputStream()).getLines()
    val outSocket1 = new PrintStream(socket1.getOutputStream())

    outSocket1.println("HELO BASE_TEST")
    println(inSocket1.next())
    Thread.sleep(10)
    println(inSocket1.next())
    Thread.sleep(10)
    println(inSocket1.next())
    Thread.sleep(10)
    println(inSocket1.next())
    Thread.sleep(10)


  }
}
