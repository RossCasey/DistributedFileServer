/**
 * Created by Ross on 23/12/15.
 */
class EncryptedMessage(encryptedContents: String, ticket: String) extends ServerMessage {
  override def toString: String = {
    "ENCRYPTED_CONTENTS: " + encryptedContents + "\n" +
      "TICKET: " + ticket + "\n"
  }
}