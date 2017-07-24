package ray.eldath.avalon.executive.pool

import java.io.File

object Constants {

	val _DEBUG = true

	val _CURRENT_PATH: String = new File("").getCanonicalPath

	val _MAX_OUTPUT_STREAM_LENGTH: Int = 255

	val _WORK_DIR: String = _CURRENT_PATH + File.separator + "temp"
}

