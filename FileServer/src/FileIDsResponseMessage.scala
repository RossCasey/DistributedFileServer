
/**
 * Created by Ross on 22/12/15.
 */
class FileIDsResponseMessage(ids: Array[String]) extends ServerMessage {

  private def convertStringArrayToString: String = {
    var str = ""
    var first = true
    for(id <- ids) {
      if(first) {
        str = str + id
        first = false
      } else {
        str = str + "," + id
      }
    }
    str
  }

  override def toString: String = {
    "FILE_IDS: " + convertStringArrayToString + "\n"
  }
}
