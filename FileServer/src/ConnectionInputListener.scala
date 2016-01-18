import java.net.SocketException

/**
 * Created by Ross on 11/11/15.
 */
class ConnectionInputListener(connection: Connection, serverUtility: ChatServerUtility) extends Runnable {

  override def run(): Unit = {
    val messageHandler = new MessageHandler(connection, serverUtility)

    println("Listener created for: " + connection.getId)

    try {
      while(connection.isConnected) {

        if(connection.hasMessage) {
          println("Message received on: " + connection.getId)
          messageHandler.handleMessage()
          println("Finished handling new message on: " + connection.getId)
        }

        Thread.sleep(50)
      }
      println("Socket closing for " + connection.getId)
    } catch {
      case e: SocketException => {
        println("Socket closing due to error for " + connection.getId)
      }
    }
  }

}
