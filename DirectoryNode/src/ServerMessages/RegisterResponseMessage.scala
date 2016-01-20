package ServerMessages

/**
 * Created by Ross on 22/12/15.
 */
class RegisterResponseMessage extends ServerMessage {
  override def toString: String = {
    "REGISTRATION_STATUS: OK\n"
  }
}
