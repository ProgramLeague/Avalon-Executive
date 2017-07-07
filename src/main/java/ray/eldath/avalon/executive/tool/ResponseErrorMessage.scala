package ray.eldath.avalon.executive.tool

import javax.servlet.http.HttpServletResponse

import org.json.JSONObject

object ResponseErrorMessage {
  def response(resp: HttpServletResponse, code: Int, message: String): Unit = {
    val error = new JSONObject()
    error.put("error", true)
    error.put("message", "发生错误：" + message)
    resp.sendError(code, error.toString())
  }
}
