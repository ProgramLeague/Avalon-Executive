package ray.eldath.avalon.executive.pool

import java.util

import ray.eldath.avalon.executive.model.Language

object LanguagePool {
  private val idToLanguage = new util.HashMap[String, Language]

  idToLanguage.put("cpp11",
    new Language(
      "cpp11",
      "c++11",
      "frolvlad/alpine-gcc",
      "ray-eldath/avalon-executive",
      "g++ --static -O2 -Wall -std=c++11 -o _file _file.cc -lm",
      "./_file")
  )

  idToLanguage.put("py3",
    new Language(
      "py3",
      "python3",
      "python:alpine",
      "ray-eldath/avalon-executive:python",
      "python3 -m py_compile _file.py",
      "python3 _file.py")
  )

  def getById(id: String): Language = idToLanguage.get(id)

  def getMap: util.Map[String, Language] = idToLanguage

  def getAllLanguage = new util.ArrayList[Language](idToLanguage.values)
}