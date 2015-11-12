/**
 * Created by Ross on 11/11/15.
 */
class Delayer(user: User) extends Runnable{
  override def run(): Unit = {
    println("testing in 1 second")
    Thread.sleep(1000)
    //user.sendAny("test\n")

    user.sendAny("CHAT: 0\n")
    user.sendAny("JOIN_ID: 1\n")
    user.sendAny("CLIENT_NAME: client2\n")
    user.sendAny("MESSAGE: hello world from client 2\n\n")

  }
}
