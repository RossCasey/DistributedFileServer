/**
 * Created by Ross on 11/11/15.
 */

/**
 * All messages that can be sent to a user implement this interface
 */
trait ServerMessage {
  def toString: String
}
