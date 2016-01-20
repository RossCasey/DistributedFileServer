import ServerMessages.{ServerMessage, HeloReplyMessage, EncryptedMessage}

/**
 * Created by Ross on 10/11/15.
 */
class MessageHandler(connection: Connection, serverUtility: ServerUtility) {

  //enum of message types
  private object MessageType extends Enumeration {
    type MessageType = Value
    val Error, Helo, Kill, Lookup, RegisterPrimary, RegisterReplica = Value
  }


  /**
   * Decrypts a received ticket that is encrypted with this node's key
   *
   * @param encryptedTicket - ticket to be decrypted
   * @return
   */
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


  /**
   * Decrypts a message received by node
   *
   * @param encryptedMessage - encrypted message to decrypt
   * @param key - key to decrypt message with
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
   * Determies whether a ticket has expired based on the expiration time specified in
   * the ticket.
   *
   * @param expirationTime - expiration time contained in ticket
   * @return true if ticket has expired, false otherwise
   */
  def isTicketExpired(expirationTime: String): Boolean = {
    val expTime = expirationTime.toLong
    val currentTime = System.currentTimeMillis() / 1000

    if(currentTime > expTime) {
      true
    } else {
      false
    }
  }


  /**
   * Decrypts a message received
   *
   * @param dataStr - encrypted data which contains the message
   * @param ticket - ticket accompanying the message
   * @return decrypted message that was received
   */
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


  /**
   * Encrypts the passed message and sends it to the connection associated with this handler.
   *
   * @param message - message to send
   * @param key - key to encrypt message with
   */
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
        case MessageType.Lookup => handleLookup(message, sessionKey)
        case MessageType.RegisterPrimary => handlePrimaryRegistration(message, sessionKey)
        case MessageType.RegisterReplica => handleReplicaRegistration(message, sessionKey)

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
    if(firstLine.startsWith("LOOKUP")) return MessageType.Lookup
    if(firstLine.startsWith("REGISTER_PRIMARY"))  return MessageType.RegisterPrimary
    if(firstLine.startsWith("REGISTER_REPLICA"))  return MessageType.RegisterReplica

    MessageType.Error
  }


  /**
   * Handles a message that cannot be identified
   * @param message - unidentifiable message
   */
  private def handleUnknownPacket(message: Array[String]):Unit = {
    print("CONTENTS OF UNIDENTIFIED PACKET:")
    println(message.toString)
  }


  /**
   * Handles a helo message from a user
   * @param message - message received by node
   * @param key - key to encrypt reply with
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
   * Handles the reception of a lookup message.
   *
   * @param message - message received
   * @param sessionKey - key with which communications should be encrypted
   */
  private def handleLookup(message: Array[String], sessionKey: String): Unit = {
    val path = message(0).split(":")(1).trim
    val lookupType = message(1).split(":")(1).trim

    if(lookupType == "READ") {
      LookupHandler.lookupForRead(path, connection, sessionKey)
    } else if (lookupType == "WRITE") {
      LookupHandler.lookupForWrite(path, connection, sessionKey)
    } else {
      connection.sendError(ErrorList.malformedPacket)
    }
  }


  /**
   * Handles the registration of a primary node
   *
   * @param message - message received
   * @param sessionKey - key with which communications should be encrypted
   */
  private def handlePrimaryRegistration(message: Array[String], sessionKey: String): Unit = {
    val ip = message(0).split(":")(1).trim
    val port = message(1).split(":")(1).trim
    LookupHandler.registerPrimary(connection, ip, port, sessionKey)
  }


  /**
   * Handles the registration of a replica node
   *
   * @param message - message received
   * @param sessionKey - key with which communications should be encrypted
   */
  private def handleReplicaRegistration(message: Array[String], sessionKey: String): Unit = {
    val replicaIP = message(0).split(":")(1).trim
    val replicaPort = message(1).split(":")(1).trim
    val primaryIP = message(2).split(":")(1).trim
    val primaryPort = message(3).split(":")(1).trim
    LookupHandler.registerReplica(connection, primaryIP, primaryPort, replicaIP, replicaPort, sessionKey)
  }
}
