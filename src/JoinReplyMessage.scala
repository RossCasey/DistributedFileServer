/**
 * Created by Ross on 11/11/15.
 */
class JoinReplyMessage(chatRoomName: String, serverIP: String,
                       port: String, roomId: Int, joinId: Int) extends ServerMessage {

  override def toString: String = {
    "JOINED_CHATROOM: " + chatRoomName + "\n" +
    "SERVER_IP: " + serverIP + "\n" +
    "PORT: " + port + "\n"  +
    "ROOM_REF: " + roomId + "\n" +
    "JOIN_ID: " + joinId + "\n"
  }
}
