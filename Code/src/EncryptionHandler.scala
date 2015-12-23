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
  val serverPassphrase = "SuperSecretPassphrase"


  /**
   * Returns encrypted data as BASE-64 encoded string!
   * @param data
   * @param key
   * @return
   */
  private def encrypt(data: Array[Byte], key: String): String = {
    val secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
    val encipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    encipher.init(Cipher.ENCRYPT_MODE, secretKey)
    val encryptedBytes: Array[Byte] = encipher.doFinal(data)

    Base64.getEncoder.encodeToString(encryptedBytes)
  }


  private def findKeyForServer(ip: String, port: String): String = {
    return serverPassphrase
  }


  private def decrypt(base64EncryptedStr: String, key: String): Array[Byte] = {
    val encryptedBytes = Base64.getDecoder.decode(base64EncryptedStr)

    val secretKey = new SecretKeySpec(key.getBytes("UTF-8"), "AES")
    val encipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    encipher.init(Cipher.DECRYPT_MODE, secretKey)
    val plainBytes: Array[Byte] = encipher.doFinal(encryptedBytes)
    plainBytes
  }


  private def isPasswordCorrect(password: String, encryptedPassphrase: String): Boolean = {
    val originalWordBytes = decrypt(encryptedPassphrase, password)
    val originalWord = new String(originalWordBytes, "UTF-8")
    if(originalWord == "PASSWORD") {
      true
    } else {
      false
    }
  }

  private def currentUnixTime: Long = {
    val timestamp = System.currentTimeMillis / 1000
    timestamp
  }


  private def generateSessionKey(): String = {
    val rand = new Random()
    val charStream = rand.alphanumeric

    val charArray = charStream.take(32)

    var sessionKey = ""
    for(char <- charArray) {
      sessionKey += char
    }
    return sessionKey
  }


  private def generateToken(serverIP: String, serverPort: String): String = {
    val sessionKey = generateSessionKey()
    val expirationTime = (currentUnixTime + 600).toString //valid for 10 minutes

    val serverKey = findKeyForServer(serverIP, serverPort)


    val ticketPlain = new Ticket(sessionKey, expirationTime)
    val ticketEncrypted = encrypt(ticketPlain.toString.getBytes(), serverKey)

    val tokenPlain = new TokenMessage(ticketEncrypted, sessionKey, serverIP, serverPort, expirationTime)
    tokenPlain.toString
  }



  def replyWithToken(connection: Connection, username: String, encryptedPassphrase: String, serverIP: String, serverPort: String): Unit = {
    val password = users.findPasswordForUsername(username)
    if(isPasswordCorrect(password, encryptedPassphrase)) {
      val plainToken = generateToken(serverIP, serverPort)
      val encryptedToken = encrypt(plainToken.getBytes(), password)
      val message = new EncryptedMessage(encryptedToken, "")  //no ticket necessary for client
      connection.sendMessage(message)
    } else {
      connection.sendError(ErrorList.accessDenied)
    }
  }

}
