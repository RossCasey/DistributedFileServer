import java.io.{FileOutputStream, ObjectOutputStream, ObjectInputStream, FileInputStream}

import scala.collection.mutable.ListBuffer
/**
 * Created by Ross on 23/12/15.
 */
class UserLookupTable {
  var users: ListBuffer[UserLookupTableEntry] = null
  initialise()

  /**
   * print state of user table to console
   */
  def printUserTable(): Unit = {
    for(user <- users) {
      println(user.getUsername + ": " + user.getPassword)
    }
  }


  /**
   * Initialises the lookup table. Reads the files that store the lookup table info
   * from disk to recreate the state of the authentication node when it was shut down. If
   * the files do not exist then create blank new ones.
   */
  def initialise(): Unit = {
    try {
      var inputStream = new ObjectInputStream(new FileInputStream("./UserTable.lt"))
      var obj = inputStream.readObject()
      users = obj.asInstanceOf[ListBuffer[UserLookupTableEntry]]
      inputStream.close()
      printUserTable()
    } catch {
      case e: Exception => {
        //if file do not exist then create them
        users = ListBuffer[UserLookupTableEntry]()
        save()
      }
    }
  }


  /**
   * Save the state of the lookup table to disk so that it can be restored when
   * authenication node is started again after shutdown.
   */
  private def save(): Unit = {
    var outputStream = new ObjectOutputStream(new FileOutputStream("./UserTable.lt"))
    outputStream.writeObject(users)
    outputStream.close()
  }


  /**
   * Determines whether a specified username/password pair exists
   *
   * @param username
   * @param password
   * @return true if pair exist, false otherwise
   */
  private def doesExist(username: String, password: String): Boolean = {
    for(user <- users) {
      if(user.getUsername == username && user.getPassword == password) {
        return true
      }
    }
    false
  }


  /**
   * Add username/password pair to list of users
   *
   * @param username - username for new entry
   * @param password - for new entry
   */
  def add(username: String, password: String): Unit = {
    if(!doesExist(username, password)) {
      val newEntry = new UserLookupTableEntry(username, password)
      users += newEntry
      save()
    }
  }


  /**
   * Returns the password for a specified username
   * @param username - username to find password for
   * @return password for specified username
   */
  def findPasswordForUsername(username: String): String = {
    for(user <- users) {
      if(user.getUsername == username) {
        return user.getPassword
      }
    }
    null
  }
}
