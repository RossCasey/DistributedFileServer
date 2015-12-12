import java.io.File
import java.nio.file.{Paths, Files}
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Ross on 12/12/15.
 */
object FileHandler {
  def getFile(fileIdentifier: String): Array[Byte] = {
    val byteArray = Files.readAllBytes(Paths.get(fileIdentifier))
    byteArray
  }

  def sendFileToConnection(connection: Connection, fileIdentifier: String): Unit = {
    val file = getFile(fileIdentifier)

    var f: Byte = 0
    for( f <- file) {
      println(f)
    }


    connection.sendBytes(file)
  }


}
