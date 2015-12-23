import java.io.{BufferedOutputStream, FileOutputStream, ByteArrayOutputStream, PrintStream}
import java.net.{InetAddress, Socket}
import java.nio.charset.{CharsetDecoder, Charset, CodingErrorAction}

import scala.collection.mutable.ListBuffer
import scala.io.BufferedSource


/**
 * Created by Ross on 12/12/15.
 */
class DistributedFile(path: String) {
  val contents: ListBuffer[Byte] = ListBuffer()


  private def downloadFile(node: NodeAddress): Unit = {
    lazy val socket = new Socket(InetAddress.getByName(node.getIP), Integer.parseInt(node.getPort))
    val connection = new Connection(0, socket)

    var count = ServerMessageHandler.requestFile(connection, path)
    val baos = new ByteArrayOutputStream()

    while (count != 0) {
      baos.write(connection.readByte())
      count -= 1
    }

    for (f <- baos.toByteArray) {
      contents += f
    }
    socket.close()
  }


  private def initialiseFile(): Unit = {
    //contact directory server -> check if file exists
    //if file exists:
    val directoryIP = "localhost"
    val directoryPort = 8000


    val connection = new Connection(0, new Socket(InetAddress.getByName(directoryIP), directoryPort))
    val dict = ServerMessageHandler.getNode(connection, path, true)

    if(!dict.contains("error")) {
      val nodeAddress = dict("address").asInstanceOf[NodeAddress]
      downloadFile(nodeAddress)
    } else {
      //init a blank file
    }
  }


  def open():Unit = {
    initialiseFile()
  }


  def close(): Unit = {
    var asByteArray = contents.toArray

    lazy val socket = new Socket(InetAddress.getByName("localhost"),8004)
    val connection = new Connection(0, socket)

    ServerMessageHandler.sendUploadRequest(connection, "testFile2.png", asByteArray.length)
    connection.sendBytes(asByteArray)
    socket.close()
  }


  def read(startPosition: Int, length: Int): Unit = {
    //TO-DO


    /*
    val readArray = Array.ofDim[Byte](length)

    for( i <- 0 to length) {
      readArray(i) = contents(i + startPosition)
    }
    readArray
    */

  }


  def write(): Unit = {
    //TO-DO
  }


  def printContents(): Unit = {
    for(byte <- contents) {
      println(byte)
    }
  }
}
