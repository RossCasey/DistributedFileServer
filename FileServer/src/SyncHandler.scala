import java.net.Socket

import ServerMessages.{RequestReadFileMessage, RequestFileIDsMessage, RequestFileHashMessage, RegisterReplicaMessage}

/**
 * Created by Ross on 22/12/15.
 */
object SyncHandler {

  /**
   * Downloads a copy of the file with the specified file ID from the specifed connection
   *
   * @param fileIdentifier - id of file to download
   * @param connection - connection to download file from
   * @param ticket - ticket to accompany request to download file
   * @param sessionKey - key to use to encrypt communication
   */
  def loadCopyOfFileFromPrimary(fileIdentifier: String, connection: Connection, ticket: String, sessionKey: String): Unit = {
    println(s"Getting copy of file $fileIdentifier from primary...")

    val reqMessage = new RequestReadFileMessage(fileIdentifier)
    val encReqMessage = Encryptor.encryptMessage(reqMessage, ticket, sessionKey)
    connection.sendMessage(encReqMessage)

    val dataLine = connection.nextLine().split(":")(1).trim
    val ticketLine = connection.nextLine()    //don't care

    val message = new String(Encryptor.decrypt(dataLine, sessionKey), "UTF-8")
    val messageLines = message.split("\n")

    val length = Integer.parseInt(messageLines(1).split(":")(1).trim)
    FileHandler.saveFileWithoutSync(connection, length, fileIdentifier, sessionKey)
  }


  /**
   * Determines whether the file with the specified with id is up to date with the node at the other
   * end of the passed connection
   *
   * @param fileIdentifier - id of file to check is up to date
   * @param connection - connection with file to use as reference
   * @param ticket - ticket to accompany request message
   * @param sessionKey - key to use for encryption during process
   * @return true if file is up to date, false otherwise
   */
  def isFileUpToDate(fileIdentifier: String, connection: Connection, ticket: String, sessionKey: String): Boolean = {
    val hashOfLocalCopy = FileHandler.computeHash(fileIdentifier)

    val reqMessage = new RequestFileHashMessage(fileIdentifier)
    val encReqMessage = Encryptor.encryptMessage(reqMessage, ticket, sessionKey)
    connection.sendMessage(encReqMessage)

    val dataLine = connection.nextLine.split(":")(1).trim
    val ticketLine = connection.nextLine() //don't care

    val message = new String(Encryptor.decrypt(dataLine, sessionKey), "UTF-8")
    val messageLines = message.split("\n")

    val primaryCopyHash = messageLines(0).split(":")(1).trim

    hashOfLocalCopy == primaryCopyHash
  }


  /**
   * Synces a file with the passed ID with the primary. If the files does not exist then download it, if it does
   * exist but is out of date then download it, otherwise no need to download file.
   *
   * @param fileIdentifier - id of file to sync
   * @param connection - connection (to primary) to use for syncing file
   * @param ticket - ticket to accompany requests with primary
   * @param sessionKey - key to use to encrypt any communication with primary
   */
  def syncFile(fileIdentifier: String, connection: Connection, ticket: String, sessionKey: String): Unit = {
    if(!FileHandler.doesFileExist(fileIdentifier)) {
      loadCopyOfFileFromPrimary(fileIdentifier, connection, ticket,sessionKey)
    } else if(!isFileUpToDate(fileIdentifier, connection, ticket, sessionKey)) {
      loadCopyOfFileFromPrimary(fileIdentifier, connection, ticket, sessionKey)
    }
  }


  /**
   * Decrypts an encrypted message that was received
   * @param encryptedMessage - message to decrypted
   * @param key - key to use for decryption
   * @return decrypted message
   */
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


  /**
   * Sync current node with the specified primary
   *
   * @param primary - primary to sync with
   * @param serverUtility - for access to credentials and other info
   */
  def syncWithPrimary(primary: NodeAddress, serverUtility: ServerUtility): Unit = {
    val token = serverUtility.getToken(primary)
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
            syncFile(fileId, primaryCon, ticket, sessionKey)
          }
          println("SYNC COMPLETE, ALL FILES UP TO DATE")
        }
      } else {
        println("FAILED TO REGISTER AS REPLICA")
      }
    }
  }
}
