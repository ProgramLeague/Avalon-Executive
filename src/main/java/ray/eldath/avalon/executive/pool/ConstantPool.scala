package ray.eldath.avalon.executive.pool

import java.io.File

object ConstantPool {
  val currentPath: String = new File("").getCanonicalPath
}

