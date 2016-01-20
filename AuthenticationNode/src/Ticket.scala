/**
 * Created by Ross on 23/12/15.
 */
class Ticket(sessionKey: String, expirationTime: String) extends ServerMessage {
  override def toString: String = {
    "SESSION_KEY: " + sessionKey + "\n" +
    "EXPIRATION_TIME: " + expirationTime + "\n"
  }
}
