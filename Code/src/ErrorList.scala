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
    "ERROR_CODE: " + code + "\nERROR_DESCRIPTION: " + description + "\n"
  }
}

//list of predefined errors
object ErrorList {
  def malformedPacket: Error = new Error(0, "Packet was malformed")
  def fileNotFound: Error = new Error(1, "No file exists at that path")
  def nodeNotFound: Error = new Error(2, "Node not found at ip/port specified")
  def nodeAlreadyExists: Error = new Error(3, "Node with ip/port specified already exists")
  def accessDenied: Error = new Error(6, "Authentication failed. Access Denied")
  def ticketTimeout: Error = new Error(7, "Ticket has expired. Please re-authenticate")
}
