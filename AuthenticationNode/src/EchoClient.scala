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



    val connection = new Connection(0, new Socket(InetAddress.getByName("localhost"), 8004))

    val encryptedPassword = Encryptor.encrypt("PASSPHRASE".getBytes("UTF-8"), "1234567")
    val logonRequest = new LogonRequest("rcasey", encryptedPassword, "test", "test")
    connection.sendMessage(logonRequest)




    val token = connection.nextLine().split(":")(1).trim
    connection.nextLine() //remove redundant Ticket section
    val decToken = new String(Encryptor.decrypt(token, "12345678"), "UTF-8")

    val lines = decToken.split("\n")
    val ticket = lines(0).split(":")(1).trim
    val sessionKey = lines(1)
    val serverIP = lines(2)
    val serverPort = lines(3)
    val exp = lines(4)

    val decTicket = new String(Encryptor.decrypt(ticket, "SuperSecretPassphrase"), "UTF-8")
    println(sessionKey)
    println(decTicket)









    /*
    val test = "Hello my honey, hello my baby, hello my ragtime girl"

    val encrypted = Encryptor.encrypt(test.getBytes("UTF-8"), "12345678")
    val decrypted = Encryptor.decrypt(encrypted, "12345678")

    println(new String(decrypted, "UTF-8"))
    */

    /*
    val encodeAndEncrypted = "fDVjF2FX220xqcVJg39yIcBCQmJB52cb8gd7Yjbi2PhUor5zCfhhMOpgASUhsvINuczkZyOiddj/XVEB4hF21Mw56Ss0P56hZER4dwyhgN2eT4X+/Kquk9mURFNk3jX1Bdy4oW2NGmhibOHPIOARAeM+u82p2lwyf7zmiLhHgzr75IoYtlGVlLD2RZUyZJYngBUHXoxTzmqarlucPUEKtSwVPNEHWf7sIHFP20jjzKdowQs3FylSlVL5Xh6XP4XG"
    val decrypted = Encryptor.decrypt(encodeAndEncrypted, "12345678")
    println(new String(decrypted, "UTF-8"))


    val received = "ENCRYPTED_CONTENTS: fDVjF2FX220xqcVJg39yIcBCQmJB52cb8gd7Yjbi2PhUor5zCfhhMOpgASUhsvINuczkZyOiddj/XVEB4hF21Mw56Ss0P56hZER4dwyhgN2eT4X+/Kquk9mURFNk3jX1Bdy4oW2NGmhibOHPIOARAeM+u82p2lwyf7zmiLhHgzr75IoYtlGVlLD2RZUyZJYngBUHXoxTzmqarlucPUEKtSwVPNEHWf7sIHFP20jjzKdowQs3FylSlVL5Xh6XP4XG"
    val encOnly = received.split(":")(1).trim
    if(encodeAndEncrypted == encOnly) {
      println("match")
    } else {
      println("does not match")
    }
    */

  }


}