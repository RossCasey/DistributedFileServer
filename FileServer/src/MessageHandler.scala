/**
 * Created by Ross on 10/11/15.
 */
class MessageHandler(connection: Connection, serverUtility: ChatServerUtility) {

  //enum of message types
  private object MessageType extends Enumeration {
    type MessageType = Value
    val ReadFileRequest, WriteFileRequest, Error, Helo, Kill, FileIDList, FileHash, RegisterReplica, Ping = Value
  }


  def decryptTicket(encryptedTicket: String): Array[String] = {
    try {
      val decryptedTicket = Encryptor.decrypt(encryptedTicket, serverUtility.getKey)
      val decryptedStr = new String(decryptedTicket, "UTF-8")
      decryptedStr.split("\n")
    } catch {
      case e:Exception => {
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


  def isTicketExpired(expirationTime: String): Boolean = {
    val expTime = expirationTime.toLong
    val currentTime = System.currentTimeMillis() / 1000

    if(currentTime > expTime) {
      true
    } else {
      false
    }
  }


  def getDecryptedMessage(dataStr: String, ticket: Array[String]): Array[String] = {

    if(ticket != null) {
      val sessionKey = ticket(0).split(":")(1).trim
      val expirationTime = ticket(1).split(":")(1).trim
      if(!isTicketExpired(expirationTime)) {
        val message = decryptMessage(dataStr, sessionKey)
        if(message != null) {
          return message
        } else {
          println("MALFORMED PACKET")
          connection.sendError(ErrorList.malformedPacket)
          null
        }
      } else {
        println("TICKET HAS EXPIRED")
        connection.sendError(ErrorList.ticketTimeout)
        null
      }
    } else {
      println("UNABLE TO DECRYPT TICKET")
      connection.sendError(ErrorList.accessDenied)
      null
    }
  }


  private def encryptAndSendMessage(message: ServerMessage, key: String): Unit = {
    val encryptedMessage = Encryptor.encrypt(message.toString.getBytes("UTF-8"), key)
    val messageToSend = new EncryptedMessage(encryptedMessage, "")
    connection.sendMessage(messageToSend)
  }


  /**
   * Determines the message type of an incoming user message and performs
   * all necessary actions to handle it appropriately.
   */
  def handleMessage(): Unit = {
    val dataLine = connection.nextLine().split(":")(1).trim
    val ticketLine = connection.nextLine().split(":")(1).trim

    val ticket = decryptTicket(ticketLine)
    val message = getDecryptedMessage(dataLine, ticket)
    val sessionKey = ticket(0).split(":")(1).trim


    if(message != null) {
      val firstLine = message(0)
      val messageType = getMessageType(firstLine)

      messageType match {
        case MessageType.Helo => handleHeloMessage(message, sessionKey)
        case MessageType.Kill => handleKillMessage()

        case MessageType.ReadFileRequest => handleReadFileMessage(message, sessionKey)
        case MessageType.WriteFileRequest => handleWriteFileMessage(message, sessionKey)

        case MessageType.FileIDList => handleFileIDListMessage(message, sessionKey)
        case MessageType.FileHash => handleFileHashMessage(message, sessionKey)
        case MessageType.RegisterReplica => handleReplicaRegistration(message, sessionKey)
        case MessageType.Ping => handlePingMessage(message, sessionKey)

        case MessageType.Error => handleUnknownPacket(message)
      }
    }
  }


  import MessageType._
  /**
   * Determines the message type of an incoming user message
   * @param firstLine - first line of the user's message
   * @return the type of message it is
   */
  private def getMessageType(firstLine: String): MessageType = {
    if(firstLine.startsWith("HELO")) return MessageType.Helo
    if(firstLine.startsWith("KILL_SERVICE")) return MessageType.Kill
    if(firstLine.startsWith("READ_FILE")) return MessageType.ReadFileRequest
    if(firstLine.startsWith("WRITE_FILE")) return MessageType.WriteFileRequest
    if(firstLine.startsWith("REQUEST_FILE_IDS")) return MessageType.FileIDList
    if(firstLine.startsWith("REQUEST_FILE_HASH")) return MessageType.FileHash
    if(firstLine.startsWith("REGISTER_REPLICA_IP")) return MessageType.RegisterReplica
    if(firstLine.startsWith("PING")) return MessageType.Ping

    MessageType.Error
  }


  /**
   * Handles a line that cannot be identifed
   * @param message
   */
  private def handleUnknownPacket(message: Array[String]):Unit = {
    print("CONTENTS OF UNIDENTIFIED PACKET:")
    println(message.toString)
  }


  /**
   * Handles a helo message from a user
   * @param firstLine - first line of helo message
   */
  private def handleHeloMessage(message: Array[String], key: String): Unit = {
    val id = "cf6932cd853ecd5e1c39a62f639f5548cf2b4cbb567075697e9a7339bcbf4ee3"
    encryptAndSendMessage(new HeloReplyMessage(message(0), serverUtility.getIP, serverUtility.getPort, id), key)
  }


  /**
   * Handles a Kill server message sent by user
   */
  private def handleKillMessage(): Unit = {
    serverUtility.killServer
  }


  /**
   * Handles requests from connections to read from a file
   * @param firstLine - firstLine of request message
   */
  private def handleReadFileMessage(message: Array[String], sessionKey: String): Unit = {
    val fileIdentifier = message(0).split(":")(1).trim
    FileHandler.sendFileToConnection(connection, fileIdentifier, sessionKey)
  }


  /**
   * Handles request from connections to write to a file
   * @param firstLine - file line of the request message
   */
  private def handleWriteFileMessage(message: Array[String], sessionKey: String): Unit = {
    val fileIdentifier = message(0).split(":")(1).trim
    val length = Integer.parseInt(message(1).split(":")(1).trim)
    FileHandler.saveFile(connection, length, fileIdentifier, serverUtility, sessionKey)
  }


  private def handleFileIDListMessage(message: Array[String], sessionKey: String): Unit = {
    FileHandler.sendFileListToConnection(connection, sessionKey)
  }


  private def handleFileHashMessage(message: Array[String], sessionKey: String): Unit = {
    val fileIdentifier = message(0).split(":")(1).trim
    FileHandler.sendHashToConnection(connection, fileIdentifier, sessionKey)
  }

  private def handleReplicaRegistration(message: Array[String], sessionKey: String): Unit = {
    val replicaIP = message(0).split(":")(1).trim
    val replicaPort = message(1).split(":")(1).trim
    val replicaAddress = new NodeAddress(replicaIP, replicaPort)

    serverUtility.addReplicaServer(replicaAddress)
    encryptAndSendMessage(new RegisterResponseMessage, sessionKey)
  }


  private def handlePingMessage(message: Array[String], sessionKey: String): Unit = {
    encryptAndSendMessage(new PongMessage, sessionKey)
  }
}
