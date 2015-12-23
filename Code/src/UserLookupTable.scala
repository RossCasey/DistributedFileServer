import java.io.{FileOutputStream, ObjectOutputStream, ObjectInputStream, FileInputStream}

import scala.collection.mutable.ListBuffer
/**
 * Created by Ross on 23/12/15.
 */
class UserLookupTable {
  var users: ListBuffer[UserLookupTableEntry] = null
  initialise()


  def initialise(): Unit = {
    try {
      var inputStream = new ObjectInputStream(new FileInputStream("./UserTable.lt"))
      var obj = inputStream.readObject()
      users = obj.asInstanceOf[ListBuffer[UserLookupTableEntry]]
      inputStream.close()
    } catch {
      case e: Exception => {
        //if file do not exist then create them
        users = ListBuffer[UserLookupTableEntry]()
        save()
      }
    }
  }


  private def save(): Unit = {
    var outputStream = new ObjectOutputStream(new FileOutputStream("./UserTable.lt"))
    outputStream.writeObject(users)
    outputStream.close()
  }


  def add(username: String, password: String): Unit = {
    val newEntry = new UserLookupTableEntry(username, password)
    users += newEntry
    save()
  }

  def findPasswordForUsername(username: String): String = {
    for(user <- users) {
      if(user.getUsername == username) {
        return user.getPassword
      }
    }
    null
  }
}
