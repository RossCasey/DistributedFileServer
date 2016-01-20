package ServerMessages

/**
 * Created by Ross on 12/12/15.
 */
class RequestReadFileMessage(fileIdentifier: String) extends ServerMessage {
  override def toString: String = {
    "READ_FILE: " + fileIdentifier + "\n"
  }
}