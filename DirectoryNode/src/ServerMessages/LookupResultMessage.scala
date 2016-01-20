package ServerMessages

/**
 * Created by Ross on 21/12/15.
 */
class LookupResultMessage(id: String, ip: String, port: String) extends ServerMessage {
  override def toString: String = {
    "LOOKUP_ID: " + id + "\n" +
    "IP: " + ip + "\n" +
    "PORT: " + port + "\n"
  }
}
