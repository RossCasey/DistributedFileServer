
/**
 * Created by Ross on 22/12/15.
 */
class LookupMessage(path: String, isRead: Boolean) extends ServerMessage {
  override def toString: String = {
    if(isRead) {
      "LOOKUP: " + path + "\n" +
        "TYPE: READ\n"
    } else {
      "LOOKUP: " + path + "\n" +
        "TYPE: WRITE\n"
    }
  }
}