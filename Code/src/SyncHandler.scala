import java.net.Socket

/**
 * Created by Ross on 22/12/15.
 */
object SyncHandler {

  def loadCopyOfFileFromPrimary(fileIdentifier: String, connection: Connection, sessionKey: String): Unit = {
    println(s"Getting copy of file $fileIdentifier from primary...")
    connection.sendMessage(new RequestReadFileMessage(fileIdentifier))

    connection.nextLine() //READING_LINE: [file id]
    val length = Integer.parseInt(connection.nextLine().split(":")(1).trim)
    FileHandler.saveFileWithoutSync(connection, length, fileIdentifier, sessionKey)
  }


  def isFileUpToDate(fileIdentifier: String, connection: Connection): Boolean = {
    val hashOfLocalCopy = FileHandler.computeHash(fileIdentifier)
    connection.sendMessage(new RequestFileHashMessage(fileIdentifier))
    val primaryCopyHash = connection.nextLine().split(":")(1).trim

    hashOfLocalCopy == primaryCopyHash
  }


  def syncFile(fileIdentifier: String, connection: Connection, sessionKey: String): Unit = {
    if(!FileHandler.doesFileExist(fileIdentifier)) {
      loadCopyOfFileFromPrimary(fileIdentifier, connection, sessionKey)
    } else if(!isFileUpToDate(fileIdentifier, connection)) {
      loadCopyOfFileFromPrimary(fileIdentifier, connection, sessionKey)
    }
  }

  private def getToken(node: NodeAddress, serverUtility: ChatServerUtility): Array[String] = {
    try {
      val authServer = serverUtility.getAuthenticationServer
      val asCon = new Connection(0, new Socket(authServer.getIP, Integer.parseInt(authServer.getPort)))
      val logonMessage = Encryptor.createLogonMessage(node, serverUtility)
      asCon.sendMessage(logonMessage)

      val dataLine = asCon.nextLine().split(":")(1).trim
      val ticketLine = asCon.nextLine() //don't care, we initiated connection so no ticket

      val decrypedToken = new String(Encryptor.decrypt(dataLine, serverUtility.getPassword), "UTF-8")
      decrypedToken.split("\n")
    } catch {
      case e: Exception => {
        null
      }
    }
  }

  def decryptMessage(encryptedMessage: String, key: String): Array[String] = {
    try {
      val decryptedMessage = Encryptor.decrypt(encryptedMessage, key)
      val decryptedStr = new String(decryptedMessage, "UTF-8")
      decryptedStr.split("\n")
    } catch {
      case e:Exception => {
        null
      }
    }
  }

  def syncWithPrimary(primary: NodeAddress, serverUtility: ChatServerUtility): Unit = {
    val token = getToken(primary, serverUtility)
    if(token != null) {
      val ticket = token(0).split(":")(1).trim
      val sessionKey = token(1).split(":")(1).trim


      val primaryCon = new Connection(0, new Socket(primary.getIP, Integer.parseInt(primary.getPort)))
      val regRepMessage = new RegisterReplicaMessage(serverUtility.getIP, serverUtility.getPort, "","")
      val encryptedMessage = Encryptor.encryptMessage(regRepMessage, ticket, sessionKey)
      primaryCon.sendMessage(encryptedMessage)

      var dataLine = primaryCon.nextLine().split(":")(1).trim
      var ticketLine = primaryCon.nextLine()
      var response = decryptMessage(dataLine, sessionKey)


      if(response(0) == "REGISTRATION_STATUS: OK") {
        println("SUCCESSFULLY REGISTERED AS REPLICA")

        val reqList = new RequestFileIDsMessage
        val encReqList = Encryptor.encryptMessage(reqList, ticket, sessionKey)
        primaryCon.sendMessage(encReqList)

        dataLine = primaryCon.nextLine().split(":")(1).trim
        ticketLine = primaryCon.nextLine()
        response = decryptMessage(dataLine, sessionKey)


        val list = response(0).split(":")(1).trim
        if(list.size > 0) {
          val fileIds = list.split(",")

          for(fileId <- fileIds) {
            syncFile(fileId, primaryCon, sessionKey)
          }
          println("SYNC COMPLETE, ALL FILES UP TO DATE")
        }
      } else {
        println("FAILED TO REGISTER AS REPLICA")
      }
    }
  }
}
