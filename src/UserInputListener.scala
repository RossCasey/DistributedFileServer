import java.net.SocketException

/**
 * Created by Ross on 11/11/15.
 */
class UserInputListener(user: User, chatRoomHandler: ChatRoomHandler, serverUtility: ChatServerUtility) extends Runnable {

  override def run(): Unit = {
    val messageHandler = new MessageHandler(user, chatRoomHandler, serverUtility)

    println("LISTENER CREATED FOR: " + user.getId)

    try {
      while(user.getSocket.isConnected) {

        if(user.hasMessage) {
          println("Message received on: " + user.getId)
          messageHandler.handleMessage()
          println("Finished handling new message on: " + user.getId)
        }

        Thread.sleep(50)
      }
    } catch {
      case e: SocketException => {
        println("Socket closing for " + user.getId + "/" + user.getName)
      }
    }
  }

}
