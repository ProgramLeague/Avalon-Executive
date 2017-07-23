package ray.eldath.avalon.executive.pool

import java.io.File

object ExecutableFilePool {
  private val executableFiles = new java.util.HashMap[Integer, File]

  def get(id: Int): File = executableFiles.get(id)

  def put(id: Int, executableFile: File): Unit = executableFiles.put(id, executableFile)

  def rm(id: Int): Unit = executableFiles.remove(id)
}
