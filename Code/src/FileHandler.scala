import java.io.{BufferedOutputStream, FileOutputStream, ByteArrayOutputStream, File}
import java.nio.file.{Paths, Files}
import scala.collection.mutable.ArrayBuffer

/**
 * Created by Ross on 12/12/15.
 */
object FileHandler {
  val base = "../Files/"

  private def getFile(fileIdentifier: String): Array[Byte] = {
    val byteArray = Files.readAllBytes(Paths.get(base + fileIdentifier))
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
    val fos = new FileOutputStream(base + fileIdentifier)
    val bos = new BufferedOutputStream(fos)

    bos.write(baos.toByteArray)
    bos.flush()
    bos.close()
    fos.close()
  }

  def computeFileList(): Array[String] = {
    val directory = new File(base)
    val files = directory.listFiles  // this is File[]
    val fileNames = ArrayBuffer[String]()
    for (file <- files) {
      if (file.isFile) {
        fileNames += file.getName
      }
    }
    fileNames.toArray
  }


  def sendFileListToConnection(connection: Connection): Unit = {
    connection.sendMessage(new FileIDsResponseMessage(computeFileList()))
  }


  def computeHash(fileIdentifier: String): String = {
    val md = java.security.MessageDigest.getInstance("SHA-1")
    val hash = new sun.misc.BASE64Encoder().encode(md.digest(getFile(fileIdentifier)))
    hash.toString
  }


  def sendHashToConnection(connection: Connection, fileIdentifier: String): Unit = {
    val hash = computeHash(fileIdentifier)
    connection.sendMessage(new FileHashResponseMessage(hash))
  }

  def doesFileExist(fileIdentifier: String): Boolean = {
    Files.exists(Paths.get(base + fileIdentifier))
  }
}