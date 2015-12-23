import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.{IvParameterSpec, SecretKeySpec}

/**
 * Created by Ross on 23/12/15.
 */
object Encryptor {



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


  def getIvSpec(): IvParameterSpec = {
    val iv = new Array[Byte](16)
    for(i <- 0 to 15) {
      iv(i) = 0
    }

    val ivspec = new IvParameterSpec(iv)
    ivspec
  }



  def encrypt(data: Array[Byte], key: String): String = {
    val secretKey = new SecretKeySpec(padKeyIfNeeded(key).getBytes("UTF-8"), "AES")
    val encipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    encipher.init(Cipher.ENCRYPT_MODE, secretKey, getIvSpec)
    val encryptedBytes: Array[Byte] = encipher.doFinal(data)

    Base64.getEncoder.encodeToString(encryptedBytes)
  }



  def decrypt(base64EncryptedStr: String, key: String): Array[Byte] = {
    val encryptedBytes = Base64.getDecoder.decode(base64EncryptedStr)

    val secretKey = new SecretKeySpec(padKeyIfNeeded(key).getBytes("UTF-8"), "AES")
    val encipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    encipher.init(Cipher.DECRYPT_MODE, secretKey, getIvSpec)
    val plainBytes: Array[Byte] = encipher.doFinal(encryptedBytes)
    plainBytes
  }
}
