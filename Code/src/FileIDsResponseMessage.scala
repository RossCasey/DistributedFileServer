/**
 * Created by Ross on 22/12/15.
 */
class FileIDsResponseMessage(ids: Array[String]) extends ServerMessage {

  private def convertStringArrayToString: String = {
    var str = ""
    for(id <- ids) {
      str = str + "," + id
    }
    str
  }

  override def toString: String = {
    "FILE_IDS: " + convertStringArrayToString + "\n"
  }
}
