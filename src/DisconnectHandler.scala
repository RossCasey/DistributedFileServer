/**
 * Created by Ross on 11/11/15.
 */
class DisconnectHandler(user: User, chatRoomHandler: ChatRoomHandler) extends Runnable {
  override def run(): Unit = {
    chatRoomHandler.handleDisconnect(user)
  }
}
