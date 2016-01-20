import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

/**
 * Created by Ross on 23/12/15.
 */
object Encryptor {


  /**
   * Key is required to be exactly 16 bytes, if the key is less than that
   * the key is padded. If it is greater than that a substring of key is used
   * @param key - key to be modified if necessary
   * @return - key suitable for 128 bit AES
   */
  def padKeyIfNeeded(key: String): String = {
    if(key.length < 16) {
      var paddingLengthNeeded = 16 - key.length
      var paddedKey = key
      for(i <- 1 to paddingLengthNeeded) {
        paddedKey += "A"
      }
      return paddedKey
    } else if(key.length > 16) {
      val paddedKey = key.substring(0,16)
      return paddedKey
    } else {
      key
    }
  }


  /**
   * Gets the initialisation vector spec for the ecryption
   * @return - IV spec for enccyrption
   */
  def getIvSpec(): IvParameterSpec = {
    val iv = new Array[Byte](16)
    for(i <- 0 to 15) {
      iv(i) = 0
    }

    val ivspec = new IvParameterSpec(iv)
    ivspec
  }


  /**
   * Encrypts the passed data using the passed key and returns
   * a base64 encoded string of encrypted data.
   *
   * @param data - data to encrypt
   * @param key - key to use to encrypt data
   * @return Base64 encoded string with encrypted data
   */
  def encrypt(data: Array[Byte], key: String): String = {
    val secretKey = new SecretKeySpec(padKeyIfNeeded(key).getBytes("UTF-8"), "AES")
    val encipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    encipher.init(Cipher.ENCRYPT_MODE, secretKey, getIvSpec)
    val encryptedBytes: Array[Byte] = encipher.doFinal(data)

    Base64.getEncoder.encodeToString(encryptedBytes)
  }


  /**
   * Takes a base64 encoded string of encrypted data and returns the
   * decrypted bytes contained within.
   *
   * @param base64EncryptedStr - base64 encoded string to decrypt
   * @param key - key to use for decryption
   * @return decrypted data contained within encrypted string
   */
  def decrypt(base64EncryptedStr: String, key: String): Array[Byte] = {
    val encryptedBytes = Base64.getDecoder.decode(base64EncryptedStr)

    val secretKey = new SecretKeySpec(padKeyIfNeeded(key).getBytes("UTF-8"), "AES")
    val encipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    encipher.init(Cipher.DECRYPT_MODE, secretKey, getIvSpec)
    val plainBytes: Array[Byte] = encipher.doFinal(encryptedBytes)
    plainBytes
  }
}
