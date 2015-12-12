/**
 *  Echo Client Lab
 *  Ross Casey - cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3
 */

import java.net._
import java.io._
import scala.io._

object EchoClient {



  def main(args: Array[String]) {

    var test = new DistributedFile("testImage.png")
    test.open()
    //test.printContents()
    test.close()

  }


}