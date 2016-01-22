/**
 * Created by Ross on 12/12/15.
 */
class WriteFileMessage(fileIdentifier: String, length: Int) extends ServerMessage {
  override def toString: String = {
    "WRITE_FILE: " + fileIdentifier + "\n" +
      "LENGTH: " + length + "\n"
  }
}