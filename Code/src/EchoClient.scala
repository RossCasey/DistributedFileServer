/**
 *  Echo Client Lab
 *  Ross Casey - cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3
 */

import java.net._
import java.io._
import scala.io._

object EchoClient {

  def readFile(numberOfBytes: Int, path: String): Unit = {

  }




  def main(args: Array[String]) {

    //create socket, buffered input/output streams
    lazy val socket = new Socket(InetAddress.getByName("localhost"),8004)
    lazy val fromServer = new BufferedSource(socket.getInputStream).getLines()
    lazy val toServer = new PrintStream(socket.getOutputStream)

    //send request to server
    toServer.println("READ_FILE: testImage.png")
    toServer.flush()




    /*
    var file = ""
    //print response to console
    while(fromServer.hasNext) {
      file += fromServer.next()
    }

    println("got here")
    println(file)
    socket.close()
    */


    println("Starting file read")

    var count = 3464 - 1


    val baos = new ByteArrayOutputStream()
    val is = socket.getInputStream
    val aByte = Array[Byte](1)

    if (is != null) {

      var fos: FileOutputStream = null
      var bos: BufferedOutputStream = null
      try {
        fos = new FileOutputStream( "./testImage2.png")
        bos = new BufferedOutputStream(fos)
        var bytesRead = is.read(aByte, 0, aByte.length)

        do {
          baos.write(aByte)
          bytesRead = is.read(aByte)
          count -= 1
        } while (count != 0)


        println("got here 0 ")

        var byteArray = baos.toByteArray
        bos.write(byteArray)
        var f: Byte = 0


        for(f <- byteArray) {
          println(f)
        }

        println("got here")

        bos.flush()
        bos.close()
        fos.close()

      } catch {
        case e: Exception => {
          println("whoops!")
        }
      }
    }
  }

}