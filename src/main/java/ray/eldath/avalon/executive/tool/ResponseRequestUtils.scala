package ray.eldath.avalon.executive.tool

import javax.servlet.http.HttpServletResponse

import org.json.JSONObject

object ResponseRequestUtils {
  def response(resp: HttpServletResponse, code: Int, message: String): Unit = {
    val error = new JSONObject()
    error.put("error", true)
    error.put("message", "发生错误：" + message)
    resp.sendError(code, error.toString())
  }

  def responseDockerException(resp: HttpServletResponse, exception: Exception): Unit = {
    val error = new JSONObject()
    error.put("error", true)
    error.put("message", "发生错误：与Docker交互异常：" + exception.toString)
    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, error.toString())
  }
}
