/**
 * Created by Ross on 11/11/15.
 */


class Error(code: Int, description: String) {
  def getCode: Int = {
    code
  }
  def getDescription: String = {
    description
  }
  override def toString: String = {
    "ERROR_CODE: " + code + "\nERROR_DESCRIPTION: " + description
  }
}

//list of predefined errors
object ErrorList {
  def malformedPacket: Error = new Error(0, "Packet was malformed")
  def userAlreadyInChatRoom: Error = new Error(1, "User is already a member of this chat room")
  def usernameAlreadyInUse: Error = new Error(2, "Username has been used by someone else in this chat room")
  def chatRoomDoesNotExist: Error = new Error(3, "The referenced chat room does not exist")
  def userNotInChatRoom: Error = new Error(4, "User is not a member of this chat room")
  def usernameMismatch: Error = new Error(5, "Username used does not match username used earlier in connection")
  def joinIdMismatch: Error = new Error(6, "Join id on connection does not match join id issued to connection")
}
