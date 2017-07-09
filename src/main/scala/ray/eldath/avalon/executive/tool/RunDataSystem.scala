package ray.eldath.avalon.executive.tool

import java.io.{Closeable, File, FileReader, FileWriter}

import org.apache.commons.io.IOUtils
import org.json.{JSONObject, JSONTokener}
import ray.eldath.avalon.executive.model.Preservable
import ray.eldath.avalon.executive.pool.ConstantPool

class RunDataSystem {
}

object RunDataSystem extends Preservable with Closeable {
  private val dataFile = new File(ConstantPool.currentPath + "/data.json")
  private val writer = new FileWriter(dataFile, false)

  if (dataFile.length() == 0) {
    val obj = new JSONObject
    obj.put("created_image", false)
    writer.write(obj.toString)
    writer.flush()
  }

  private val valueObj = new JSONTokener(new FileReader(dataFile)).nextValue().asInstanceOf[JSONObject]

  def getString(key: String): String = valueObj.getString(key)

  def getBoolean(key: String): Boolean = valueObj.getBoolean(key)

  def putString(key: String, value: String): Unit = valueObj.put(key, value)

  def putBoolean(key: String, value: Boolean): Unit = valueObj.put(key, value)

  override def save(): Unit = {
    writer.write(valueObj.toString())
    writer.flush()
  }

  override def close(): Unit = IOUtils.closeQuietly(writer)
}