/**
 *  Echo Client Lab
 *  Ross Casey - cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3
 */

import java.net._
import java.io._
import java.nio.file.{Paths, Files}
import scala.io._
import scala.util.Random

object TestClient {



  def main(args: Array[String]) {
    /*
    val fileName = "test6.txt"

    //file that does not exist being added to system
    val distFile = new DistributedFile(fileName, "localhost", 10000, "localhost", 10100, "rcasey", "12345678")
    distFile.open()

    //val otherFile = Files.readAllBytes(Paths.get("./" + fileName))
    //distFile.write(0, otherFile.length, otherFile)

    //distFile.close()
    */


    for(i <- 0 to 10000) {
      val rand = Random.nextInt(6) + 1
      val fileToRequest = "test" + rand.toString + ".txt"

      val dist = new DistributedFile(fileToRequest, "localhost", 10000, "localhost", 10100, "rcasey", "12345678")
      dist.open()
      println(i)
      if(i % 5 == 0) {
        dist.close()
      }
    }
    println("Test over")

  }


}