import java.io.{BufferedOutputStream, FileOutputStream, ByteArrayOutputStream, PrintStream}
import java.net.{InetAddress, Socket}
import java.nio.charset.{CharsetDecoder, Charset, CodingErrorAction}

import scala.collection.mutable.ListBuffer
import scala.io.BufferedSource


/**
 * Created by Ross on 12/12/15.
 */
class DistributedFile(path: String, username: String, password: String) {
  val contents: ListBuffer[Byte] = ListBuffer()
  val authServer = new NodeAddress("localhost", "9000")
  val directoryServer = new NodeAddress("localhost", "8000")


  private def downloadFile(node: NodeAddress, fileID: String): Unit = {
    println("downloading file")

    val nodeToken = ServerMessageHandler.getTokenForServer(authServer, node, username, password)
    if(!nodeToken(0).contains("ERROR")) {
      val ticket = nodeToken(0).split(":")(1).trim
      val sessionKey = nodeToken(1).split(":")(1).trim

      //connect to node and download file
      val connection = new Connection(1, new Socket(node.getIP, Integer.parseInt(node.getPort)))

      val file = ServerMessageHandler.readFile(connection, fileID, ticket, sessionKey)

      println("File length in Dist: " + file.length)
      for(byte <- file) {
        contents += byte
      }

    } else {
      println(nodeToken(1))
    }
  }


  private def initialiseFile(): Unit = {
    println("initialising file")
    //contact directory server -> check if file exists
    //if file exists:
    val dirToken = ServerMessageHandler.getTokenForServer(authServer, directoryServer, username, password)
    if(!dirToken(0).contains("ERROR")) {
      val ticket = dirToken(0).split(":")(1).trim
      val sessionKey = dirToken(1).split(":")(1).trim

      //lookup file
      val dirCon = new Connection(0, new Socket(directoryServer.getIP, Integer.parseInt(directoryServer.getPort)))
      val fileDetails = ServerMessageHandler.getNode(dirCon, path, ticket, sessionKey, true)

      if(fileDetails.contains("error")) {
        println(fileDetails("error").asInstanceOf[String])
        //init blank file
      } else {
        val nodeAddress = fileDetails("address").asInstanceOf[NodeAddress]
        val fileID = fileDetails("id").asInstanceOf[String]
        downloadFile(nodeAddress,fileID)
      }

    }
  }


  def open():Unit = {
    initialiseFile()
  }



  private def uploadFile(node: NodeAddress, fileID: String): Unit = {
    val nodeToken = ServerMessageHandler.getTokenForServer(authServer, node, username, password)
    if(!nodeToken(0).contains("ERROR")) {
      val ticket = nodeToken(0).split(":")(1).trim
      val sessionKey = nodeToken(1).split(":")(1).trim

      //connect to node and download file
      val connection = new Connection(1, new Socket(node.getIP, Integer.parseInt(node.getPort)))

      ServerMessageHandler.writeFile(connection, fileID, ticket, sessionKey, contents.toArray )
    } else {
      println(nodeToken(1))
    }
  }



  private def prepareToWriteFile(): Unit = {
    val dirToken = ServerMessageHandler.getTokenForServer(authServer, directoryServer, username, password)
    if(!dirToken(0).contains("ERROR")) {
      val ticket = dirToken(0).split(":")(1).trim
      val sessionKey = dirToken(1).split(":")(1).trim

      //lookup file
      val dirCon = new Connection(0, new Socket(directoryServer.getIP, Integer.parseInt(directoryServer.getPort)))
      val fileDetails = ServerMessageHandler.getNode(dirCon, path, ticket, sessionKey, false)

      if(fileDetails.contains("error")) {
        println(fileDetails("error").asInstanceOf[String])
      } else {
        val nodeAddress = fileDetails("address").asInstanceOf[NodeAddress]
        val fileID = fileDetails("id").asInstanceOf[String]
        uploadFile(nodeAddress,fileID)
      }

    }
  }


  def close(): Unit = {
    prepareToWriteFile()
  }


  def read(startPosition: Int, length: Int): Unit = {
    //TO-DO
  }


  def write(): Unit = {
    //TO-DO
  }


  def printContents(): Unit = {
    for(byte <- contents) {
      println(byte)
    }
  }

  def getContents(): Array[Byte] = {
    contents.toArray
  }
}
