/**
 * Created by Ross on 22/12/15.
 */
class RequestFileHashMessage(fileIdentifier: String) extends ServerMessage {
  override def toString: String = {
    "REQUEST_FILE_HASH: " + fileIdentifier + "\n"
  }
}
