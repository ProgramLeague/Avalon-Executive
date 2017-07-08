package ray.eldath.avalon.executive.test.tool

import java.io.File

import ray.eldath.avalon.executive.pool.ConstantPool
import ray.eldath.avalon.executive.tool.RunDataSystem

object RunDataSystemTest {
  private val dataFile = new File(ConstantPool.currentPath + "/data.json")

  def main(args: Array[String]): Unit = {
    RunDataSystem.getClass
  }
}
