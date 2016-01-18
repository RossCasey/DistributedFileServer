/**
 * Created by Ross on 22/12/15.
 */
class RegisterPrimaryMessage(ip: String, port: String) extends ServerMessage {
  override def toString: String = {
    "REGISTER_PRIMARY_IP: " + ip + "\n" +
    "PORT: " + port + "\n"
  }
}
