import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import java.util.Base64
import ServerMessages.{TokenMessage, Ticket, EncryptedMessage}

import scala.util.Random
import java.nio.charset.StandardCharsets

/**
 * Created by Ross on 23/12/15.
 */
object EncryptionHandler {
  var users = new UserLookupTable()


  /**
   * Returns the key for a given server, for simiplity all servers use the same key
   *
   * @param ip - ip of server to get key for
   * @param port - port of server to get key for
   * @return key for the specified server
   */
  private def findKeyForServer(ip: String, port: String): String = {
    "SuperSecretPassphrase"
  }


  /**
   * Determines whether the user trying to authenticate has the correct passphrase
   *
   * @param password - password on file for user
   * @param encryptedPassphrase - passphrase encrypted with what is supposedly user's password
   * @return true if password ok, false otherwise
   */
  private def isPasswordCorrect(password: String, encryptedPassphrase: String): Boolean = {
    try {
      val originalWordBytes = Encryptor.decrypt(encryptedPassphrase, password)

      val originalWord = new String(originalWordBytes, "UTF-8")
      if (originalWord == "PASSPHRASE") {
        true
      } else {
        false
      }
    } catch {
      case e: Exception => {
        false
      }
    }
  }


  /**
   * Returns the current unix time for calculating expiration time of ticket
   *
   * @return current unix time
   */
  private def currentUnixTime: Long = {
    val timestamp = System.currentTimeMillis / 1000
    timestamp
  }


  /**
   * Generate a random key that is suitable for use as a session key
   *
   * @return randomly generated session key
   */
  private def generateSessionKey(): String = {
    val rand = new Random()
    val charStream = rand.alphanumeric

    val charArray = charStream.take(16)

    var sessionKey = ""
    for(char <- charArray) {
      sessionKey += char
    }
    return sessionKey
  }


  /**
   * Generate a token for the server with the specified IP/port
   *
   * @param serverIP - IP of server to generate token for
   * @param serverPort - Port of server to generate token for
   * @return Token for specified server as a string
   */
  private def generateToken(serverIP: String, serverPort: String): String = {
    val sessionKey = generateSessionKey()
    println(sessionKey.length)
    val expirationTime = (currentUnixTime + 600).toString //valid for 10 minutes

    val serverKey = findKeyForServer(serverIP, serverPort)


    val ticketPlain = new Ticket(sessionKey, expirationTime)
    val ticketEncrypted = Encryptor.encrypt(ticketPlain.toString.getBytes(), serverKey)

    val tokenPlain = new TokenMessage(ticketEncrypted, sessionKey, serverIP, serverPort, expirationTime)
    tokenPlain.toString
  }


  /**
   * Reply to a request for a token
   *
   * @param connection - connection to send response to
   * @param username - username of authenticating user
   * @param encryptedPassphrase - encrypted passphrase of user attempting to authenticate
   * @param serverIP - IP for server they wish to get ticket for
   * @param serverPort - Port for server they wish to get ticket for
   */
  def replyWithToken(connection: Connection, username: String, encryptedPassphrase: String, serverIP: String, serverPort: String): Unit = {
    this.synchronized {
      val password = users.findPasswordForUsername(username)
      if(isPasswordCorrect(password, encryptedPassphrase)) {
        val plainToken = generateToken(serverIP, serverPort)
        println(plainToken)
        val encryptedToken = Encryptor.encrypt(plainToken.getBytes(), password)
        println(encryptedToken)
        val message = new EncryptedMessage(encryptedToken, "")  //no ticket necessary for client
        connection.sendMessage(message)
      } else {
        connection.sendError(ErrorList.accessDenied)
      }
      connection.close()
    }
  }


  /**
   * Add a user to table of users that can authenticate
   * @param username
   * @param password
   */
  def addUser(username: String, password: String): Unit = {
    this.synchronized {
      users.add(username, password)
    }
  }
}
