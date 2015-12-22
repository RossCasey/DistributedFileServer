import java.io.ByteArrayOutputStream
import java.net.{InetAddress, Socket}

/**
 * Created by Ross on 22/12/15.
 */
object DirectoryServerTester {



  def main(args: Array[String]) {

    lazy val socket = new Socket(InetAddress.getByName("localhost"), 8004)
    val connection = new Connection(0, socket)

    //val path = "usr/var/www/test.txt"
    //ServerMessageHandler.performWriteLookup(connection, path)

    //println(connection.nextLine())
    //println(connection.nextLine())
    //println(connection.nextLine())


    //register some nodes
    /*
    val prim = new RegisterPrimaryMessage("0.0.0.0", "1000")
    connection.sendMessage(prim)
    println(connection.nextLine())

    val rep1 = new RegisterReplicaMessage("1.1.1.1", "1000","0.0.0.0", "1000")
    connection.sendMessage(rep1)
    println(connection.nextLine())

    val rep2 = new RegisterReplicaMessage("2.2.2.2", "1000","0.0.0.0", "1000")
    connection.sendMessage(rep2)
    println(connection.nextLine())
    */

    val path = "usr/var/www/test2.txt"
    ServerMessageHandler.performWriteLookup(connection, path)

    println(connection.nextLine())
    println(connection.nextLine())
    println(connection.nextLine())



  }

}
