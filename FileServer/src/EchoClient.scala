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

    //DistributedFile(path: String, authIP: String, authPort: Int, directoryIP: String, directoryPort: Int, username: String, password: String)
    val distFile = new DistributedFile("testFile.txt", "localhost", 8000, "localhost", 8100, "rcasey", "12345678")
    distFile.open()






    //var test = new DistributedFile("testImage.png")
    //test.open()
    //test.printContents()
    //test.close()


    /*
    val authCon = new Connection(2, new Socket(InetAddress.getByName("localhost"),8000))
    val token = ServerMessageHandler.getTokenForServer(authCon, new NodeAddress("localhost", 8100.toString), "rcasey", "12345678")

    if(token != null) {
      println("got token")
      val ticket = token(0).split(":")(1).trim
      val sessionKey = token(1).split(":")(1).trim

      val dirCon = new Connection(3, new Socket(InetAddress.getByName("localhost"), 8100))

      val lookupMessage = new LookupMessage("testFile.txt", false)
      val encLookupMessage = Encryptor.encryptMessage(lookupMessage, ticket, sessionKey)
      dirCon.sendMessage(encLookupMessage)

      val dataLine = dirCon.nextLine().split(":")(1).trim
      val ticketLine = dirCon.nextLine()
      println("got stuff from dir server")


      val message = new String(Encryptor.decrypt(dataLine, sessionKey), "UTF-8")
      val messageLines = message.split("\n")

      val fileID = messageLines(0).split(":")(1).trim
      val ipAddress = messageLines(1).split(":")(1).trim
      val port = messageLines(2).split(":")(1).trim

      val priCon = new Connection(4, new Socket(InetAddress.getByName(ipAddress), Integer.parseInt(port)))


      val encryptedFile = Encryptor.encrypt(Files.readAllBytes(Paths.get("./testFile.txt")), sessionKey)
      val encryptedMeesage = Encryptor.encryptMessage(new WriteFileMessage(fileID, encryptedFile.length), ticket, sessionKey)
      priCon.sendMessage(encryptedMeesage)
      priCon.sendBytes(encryptedFile.getBytes)
      println("send encrypted bytes")
    }
    */







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


    /*
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
    */
  }


}