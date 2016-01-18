/**
 * Created by Ross on 11/11/15.
 */
class ChatReplyMessage(chatRoomRef: Int, username: String, message: String) extends ServerMessage{

  override def toString: String = {
    "CHAT: " + chatRoomRef.toString + "\n" +
    "CLIENT_NAME: " + username + "\n" +
    "MESSAGE: " + message + "\n"
  }
}
