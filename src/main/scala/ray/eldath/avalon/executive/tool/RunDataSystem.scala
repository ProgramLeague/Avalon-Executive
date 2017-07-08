package ray.eldath.avalon.executive.tool

import java.io.{Closeable, File, FileWriter}

import com.typesafe.config.{Config, ConfigFactory}
import org.apache.commons.io.IOUtils
import org.json.JSONObject
import org.slf4j.LoggerFactory
import ray.eldath.avalon.executive.model.Preservable
import ray.eldath.avalon.executive.pool.ConstantPool

class RunDataSystem {
}

object RunDataSystem extends Preservable with Closeable {
  private val LOGGER = LoggerFactory.getLogger(classOf[RunDataSystem])
  private val dataFile = new File(ConstantPool.currentPath + "/data.json")
  private val valueObj = new JSONObject
  private val writer = new FileWriter(dataFile)

  private val config: Config = {
    valueObj.put("created_image", false)
    writer.write(valueObj.toString())
    writer.flush()
    ConfigFactory.parseFile(dataFile)
  }

  def getString(key: String): String = config.getString(key)

  def getBoolean(key: String): Boolean = config.getBoolean(key)

  def putString(key: String, value: String): Unit = valueObj.put(key, value)

  def putBoolean(key: String, value: Boolean): Unit = valueObj.put(key, value)

  override def save(): Unit = IOUtils.write(valueObj.toString, writer)

  override def close(): Unit = IOUtils.closeQuietly(writer)
}