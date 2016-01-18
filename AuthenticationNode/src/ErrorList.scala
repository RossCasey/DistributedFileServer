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
  def accessDenied: Error = new Error(6, "Authentication failed. Access Denied")
}
