/**
 * Created by Ross on 22/12/15.
 */
class NodeTableEntry(id: String, ip: String, port: String, isReplica: Boolean) extends Serializable {
  override def toString = "NodeTableEntry($id, $ip, $port, $isReplica)"

  def getId: String = {
    id
  }

  def getIp: String = {
    ip
  }

  def getPort: String = {
    port
  }

  def getIsReplica: Boolean = {
    isReplica
  }
}
