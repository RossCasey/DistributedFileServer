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

    val connection = new Connection(0, new Socket(InetAddress.getByName("localhost"), 8000))
    val dict = ServerMessageHandler.getNode(connection, "hello.txt", false)

    if(!dict.contains("error")) {
      val nodeAddress = dict("address").asInstanceOf[NodeAddress]
      val fileId = dict("id").asInstanceOf[String]
      val ipAddress = nodeAddress.getIP
      val port = nodeAddress.getPort

      val priCon = new Connection(1, new Socket(InetAddress.getByName(ipAddress), Integer.parseInt(port)))


      val byteArray = Files.readAllBytes(Paths.get("./start.sh"))
      ServerMessageHandler.sendUploadRequest(priCon, fileId, byteArray.length)
      priCon.sendBytes(byteArray)


      println(ipAddress)
      println(port)
    } else {
      println(dict("error"))
    }

  }


}