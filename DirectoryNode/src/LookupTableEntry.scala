/**
 * Created by Ross on 21/12/15.
 */
class LookupTableEntry(fileId: String, nodeId: String, path: String) extends Serializable {
  override def toString = "LookupTableEntry($id, $ip, $port, $path)"

  def getFileId: String =  {
    fileId
  }

  def getNodeId: String = {
    nodeId
  }

  def getPath: String = {
    path
  }
}
