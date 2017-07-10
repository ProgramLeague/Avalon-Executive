package ray.eldath.avalon.executive.pool

import java.io.File

object ConstantPool {

  val CURRENT_PATH: String = new File("").getCanonicalPath

  val MAX_OUTPUT_STREAM_LENGTH: Int = 255
}

