/**
 * Created by Ross on 23/12/15.
 */
class UserLookupTableEntry(username: String, password: String) extends Serializable {
  override def toString = "UserLookupTableEntry($username, $password)"

  def getUsername: String = {
    username
  }

  def getPassword: String = {
    password
  }
}