import java.io.{BufferedOutputStream, FileOutputStream, ByteArrayOutputStream, File}
import java.nio.file.{Paths, Files}
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Ross on 12/12/15.
 */
object FileHandler {
  private def getFile(fileIdentifier: String): Array[Byte] = {
    val byteArray = Files.readAllBytes(Paths.get(fileIdentifier))
    byteArray
  }

  def sendFileToConnection(connection: Connection, fileIdentifier: String): Unit = {
    val file = getFile(fileIdentifier)

    connection.sendMessage(new ReadingFileMessage(fileIdentifier, file.length))
    connection.sendBytes(file)
  }

  def saveFile(connection: Connection, length: Int, fileIdentifier: String): Unit = {
    var count = length
    val baos = new ByteArrayOutputStream()

    while(count > 0) {
      baos.write(connection.readByte())
      count -= 1
    }
    val fos = new FileOutputStream(fileIdentifier)
    val bos = new BufferedOutputStream(fos)

    bos.write(baos.toByteArray)
    bos.flush()
    bos.close()
    fos.close()
  }
}
