import java.io.{FileOutputStream, ObjectOutputStream, ObjectInputStream, FileInputStream}

import scala.collection.mutable.ListBuffer

/**
 * Created by Ross on 21/12/15.
 */
class LookupTable {
  var entries: ListBuffer[LookupTableEntry] = null
  var nextAvailableId = -1
  initialise()

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


  private def save(): Unit = {
    var outputStream = new ObjectOutputStream(new FileOutputStream("./LookupTable.lt"))
    outputStream.writeObject(entries)

    outputStream = new ObjectOutputStream(new FileOutputStream("./LookupTableId.lt"))
    outputStream.writeObject(nextAvailableId)

    outputStream.close()
  }


  def add(nodeId: String, path: String): Unit = {
    val nextId = nextAvailableId
    nextAvailableId += 1

    val newEntry = new LookupTableEntry(nextId.toString, nodeId, path)
    entries += newEntry

    save()
  }


  def removeAllWithId(id: String): Unit = {
    for(entry <- entries) {
      if(entry.getFileId == id) {
        entries -= entry
      }
    }
  }


  def removeAllWithNodeId(nodeId: String): Unit = {
    for(entry <- entries) {
      if(entry.getNodeId == nodeId) {
        entries -= entry
      }
    }
  }


  def findEntryWithPath(path: String): LookupTableEntry = {
    for(entry <- entries) {
      if(entry.getPath == path) {
        return entry
      }
    }
    return null
  }
}
