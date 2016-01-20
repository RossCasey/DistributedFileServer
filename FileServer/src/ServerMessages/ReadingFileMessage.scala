package ServerMessages

/**
 * Created by Ross on 12/12/15.
 */
class ReadingFileMessage(fileIdentifier: String, length: Int) extends ServerMessage {
  override def toString: String = {
    "READING_FILE: " + fileIdentifier + "\n" +
    "LENGTH: " + length + "\n"
  }

  def getFileIdentifier: String = {
    fileIdentifier
  }

  def getLength: Int = {
    length
  }
}