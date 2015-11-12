/**
 * Created by Ross on 11/11/15.
 */
class UserInputListener(user: User, chatRoomHandler: ChatRoomHandler, serverUtility: ChatServerUtility) extends Runnable {

  override def run(): Unit = {
    val messageHandler = new MessageHandler(user, chatRoomHandler, serverUtility)

    println("LISTENER CREATED FOR: " + user.getId)

    while(!user.getSocket.isClosed) {
      //if(user.hasMessage) {
        println("Message received on: " + user.getId)
        messageHandler.handleMessage()
        println("finish handling new message")
      //}
    }
  }

}
