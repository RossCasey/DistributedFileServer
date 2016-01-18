/**
 * Created by Ross on 23/12/15.
 */
class LogonRequest(username: String, encryptedPassphrase: String, serverIP: String, serverPort: String) extends ServerMessage {
  override def toString: String = {
    "USERNAME: " + username + "\n" +
    "ENCRYPTED_PASSPHRASE: " + encryptedPassphrase + "\n" +
    "SERVER_IP: " + serverIP + "\n" +
    "SERVER_PORT: " + serverPort + "\n"
  }
}
