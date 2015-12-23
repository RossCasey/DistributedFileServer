import javax.crypto.spec.SecretKeySpec
import javax.crypto.Cipher
import java.util.Base64
import scala.util.Random
import java.nio.charset.StandardCharsets

/**
 * Created by Ross on 23/12/15.
 */
object EncryptionHandler {
  var users = new UserLookupTable()


  private def findKeyForServer(ip: String, port: String): String = {
    "SuperSecretPassphrase"
  }


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

  private def currentUnixTime: Long = {
    val timestamp = System.currentTimeMillis / 1000
    timestamp
  }


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



  def replyWithToken(connection: Connection, username: String, encryptedPassphrase: String, serverIP: String, serverPort: String): Unit = {
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
  }


  def addUser(username: String, password: String): Unit = {
    users.add(username, password)
  }

}
