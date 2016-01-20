import java.io.{FileOutputStream, ObjectOutputStream, ObjectInputStream, FileInputStream}

import scala.collection.mutable.ListBuffer

/**
 * Created by Ross on 21/12/15.
 */
class LookupTable {
  var entries: ListBuffer[LookupTableEntry] = null
  var nextAvailableId = -1
  initialise()


  /**
   * Initialises the lookup table. Reads the files that store the lookup table info
   * from disk to recreate the state of the directory node when it was shut down. If
   * the files do not exist then create blank new ones.
   */
  def initialise(): Unit = {
    try {
      var inputStream = new ObjectInputStream(new FileInputStream("./LookupTable.lt"))
      var obj = inputStream.readObject()
      entries = obj.asInstanceOf[ListBuffer[LookupTableEntry]]

      inputStream = new ObjectInputStream(new FileInputStream("./LookupTableId.lt"))
      obj = inputStream.readObject()
      nextAvailableId = obj.asInstanceOf[Int]

      inputStream.close()
    } catch {
      case e: Exception => {
        //if file do not exist then create them
        entries = ListBuffer[LookupTableEntry]()
        nextAvailableId = 0
        save()
      }
    }
  }


  /**
   * Save the state of the lookup table to disk so that it can be restored when
   * directory node is started again after shutdown.
   */
  private def save(): Unit = {
    var outputStream = new ObjectOutputStream(new FileOutputStream("./LookupTable.lt"))
    outputStream.writeObject(entries)

    outputStream = new ObjectOutputStream(new FileOutputStream("./LookupTableId.lt"))
    outputStream.writeObject(nextAvailableId)

    outputStream.close()
  }


  /**
   * Add an association between a nodeID and a file path.
   *
   * @param nodeId
   * @param path
   */
  def add(nodeId: String, path: String): Unit = {
    val nextId = nextAvailableId
    nextAvailableId += 1

    val newEntry = new LookupTableEntry(nextId.toString, nodeId, path)
    entries += newEntry

    save()
  }


  /**
   * Remove all entries in the lookup table with the specified file id
   *
   * @param id - all entries with this file ID will be removed
   */
  def removeAllWithId(id: String): Unit = {
    for(entry <- entries) {
      if(entry.getFileId == id) {
        entries -= entry
      }
    }
  }


  /**
   * Removes all entries with the node ID specified
   *
   * @param nodeId - all entries with this node ID shall be removed
   */
  def removeAllWithNodeId(nodeId: String): Unit = {
    for(entry <- entries) {
      if(entry.getNodeId == nodeId) {
        entries -= entry
      }
    }
  }


  /**
   * Finds the entry with the specified file path
   *
   * @param path - path to find entry with
   * @return entry containing that path id
   */
  def findEntryWithPath(path: String): LookupTableEntry = {
    for(entry <- entries) {
      if(entry.getPath == path) {
        return entry
      }
    }
    return null
  }
}
