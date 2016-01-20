
/**
 * Created by Ross on 22/12/15.
 */
class RegisterReplicaMessage(replicaIP: String, replicaPort: String, primaryIP: String, primaryPort: String) extends ServerMessage {
  override def toString: String = {
    "REGISTER_REPLICA_IP: " + replicaIP + "\n" +
    "PORT: " + replicaPort + "\n" +
    "PRIMARY_IP: " + primaryIP + "\n" +
    "PRIMARY_PORT: " + primaryPort + "\n"
  }
}