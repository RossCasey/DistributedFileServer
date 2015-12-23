/**
 *  Echo Client Lab
 *  Ross Casey - cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3
 */

import java.net._
import java.io._
import java.nio.file.{Paths, Files}
import scala.io._

object EchoClient {



  def main(args: Array[String]) {

    //var test = new DistributedFile("testImage.png")
    //test.open()
    //test.printContents()
    //test.close()

    /*
    val connection = new Connection(0, new Socket(InetAddress.getByName("localhost"), 8000))
    val dict = ServerMessageHandler.getNode(connection, "rattle.mp3", false)

    if(!dict.contains("error")) {
      val nodeAddress = dict("address").asInstanceOf[NodeAddress]
      val fileId = dict("id").asInstanceOf[String]
      val ipAddress = nodeAddress.getIP
      val port = nodeAddress.getPort

      val priCon = new Connection(1, new Socket(InetAddress.getByName(ipAddress), Integer.parseInt(port)))


      val byteArray = Files.readAllBytes(Paths.get("./rattle.mp3"))
      ServerMessageHandler.sendUploadRequest(priCon, fileId, byteArray.length)
      priCon.sendBytes(byteArray)


      println(ipAddress)
      println(port)
    } else {
      println(dict("error"))
    }
  */

    val connection = new Connection(0, new Socket(InetAddress.getByName("localhost"), 8000))
    val dict = ServerMessageHandler.getNode(connection, "rattle.mp3", true)

    val node = dict("address").asInstanceOf[NodeAddress]
    val id= dict("id").asInstanceOf[String]


    lazy val socket = new Socket(InetAddress.getByName(node.getIP), Integer.parseInt(node.getPort))
    val readCon = new Connection(0, socket)

    var count = ServerMessageHandler.requestFile(readCon, id)
    val baos = new ByteArrayOutputStream()

    while (count != 0) {
      baos.write(readCon.readByte())
      count -= 1
    }

    val fos = new FileOutputStream("./Files/rattle.mp3")
    val bos = new BufferedOutputStream(fos)

    bos.write(baos.toByteArray)
    bos.flush()
    bos.close()
    fos.close()

    socket.close()
  }


}