
/**
 * Created by Ross on 11/11/15.
 */
class HeloReplyMessage(original: String, ip: String, port: String, studentID: String) extends ServerMessage {
  override def toString: String = {
    original + "\n" +
    "IP:" + ip + "\n" +
    "Port:" + port + "\n" +
    "StudentID:" + studentID + "\n"
  }
}
