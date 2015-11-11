/**
 * Created by Ross on 11/11/15.
 */
class LeaveReplyMessage(chatRoomRef: Int, id: Int) extends ServerMessage {
  override def toString: String = {
    "LEFT_CHATROOM: " + chatRoomRef.toString + "\n" +
    "JOIN_ID: " + id.toString + "\n"
  }
}