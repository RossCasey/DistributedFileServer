import java.net.Socket

/**
 * Created by Ross on 22/12/15.
 */
object SyncHandler {

  def loadCopyOfFileFromPrimary(fileIdentifier: String, connection: Connection): Unit = {
    println(s"Getting copy of file $fileIdentifier from primary...")
    connection.sendMessage(new RequestReadFileMessage(fileIdentifier))

    connection.nextLine() //READING_LINE: [file id]
    val length = Integer.parseInt(connection.nextLine().split(":")(1).trim)
    FileHandler.saveFileWithoutSync(connection, length, fileIdentifier)
  }


  def isFileUpToDate(fileIdentifier: String, connection: Connection): Boolean = {
    val hashOfLocalCopy = FileHandler.computeHash(fileIdentifier)
    connection.sendMessage(new RequestFileHashMessage(fileIdentifier))
    val primaryCopyHash = connection.nextLine().split(":")(1).trim

    hashOfLocalCopy == primaryCopyHash
  }


  def syncFile(fileIdentifier: String, connection: Connection): Unit = {
    if(!FileHandler.doesFileExist(fileIdentifier)) {
      loadCopyOfFileFromPrimary(fileIdentifier, connection)
    } else if(!isFileUpToDate(fileIdentifier, connection)) {
      loadCopyOfFileFromPrimary(fileIdentifier, connection)
    }
  }


  def syncWithPrimary(primary: NodeAddress): Unit = {
    val primaryCon = new Connection(0, new Socket(primary.getIP, Integer.parseInt(primary.getPort)))
    primaryCon.sendMessage(new RequestFileIDsMessage)

    val list = primaryCon.nextLine().split(":")(1).trim
    val fileIds = list.split(",")

    for(fileId <- fileIds) {
      syncFile(fileId, primaryCon)
    }
    println("SYNC COMPLETE, ALL FILES UP TO DATE")
  }
}
