/**
 *  Echo Client Lab
 *  Ross Casey - cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3
 */

import java.net._
import java.io._
import java.nio.file.{Paths, Files}
import scala.io._

object TestClient {



  def main(args: Array[String]) {

    val fileName = "test5.txt"

    //file that does not exist being added to system
    val distFile = new DistributedFile(fileName, "localhost", 10000, "localhost", 10100, "rcasey", "12345678")
    distFile.open()

    //val otherFile = Files.readAllBytes(Paths.get("./" + fileName))
    //distFile.write(0, otherFile.length, otherFile)

    //distFile.close()
  }


}