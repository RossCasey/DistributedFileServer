package ServerMessages

/**
 * Created by Ross on 22/12/15.
 */
class FileHashResponseMessage(hash: String) extends ServerMessage {
  override def toString: String = {
    "FILE_HASH: " + hash + "\n"
  }
}
