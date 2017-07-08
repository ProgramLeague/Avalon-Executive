package ray.eldath.avalon.executive.model

import org.eclipse.jetty.util.UrlEncoded

class Submission(id: Int, language: Language, encodedCode: String) {
  private val decodedCode: String = UrlEncoded.decodeString(encodedCode)
  private val submitTime = System.currentTimeMillis()

  def getSubmitTime: Long = submitTime

  def getId: Int = id

  def getLanguage: Language = language

  def getEncodedCode: String = encodedCode

  def getDecodedCode: String = decodedCode
}
