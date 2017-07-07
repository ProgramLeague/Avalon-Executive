package ray.eldath.avalon.executive.pool

import ray.eldath.avalon.executive.model.Language
import java.util

object LanguagePool {
  private val idToLanguage = new util.HashMap[String, Language]

  idToLanguage.put("", new Language("cpp11", "c++11", "g++ -O2 -Wall -std=c++11 {file}.cc -lm", "./{file}"))
  idToLanguage.put("py", new Language("py", "python3", "python -m py_compile {file}.py", "python {file}.pyc"))

  def getById(id: String): Language = idToLanguage.get(id)

  def getMap: util.Map[String, Language] = idToLanguage

  def getAllLanguage = new util.ArrayList[Language](idToLanguage.values)
}