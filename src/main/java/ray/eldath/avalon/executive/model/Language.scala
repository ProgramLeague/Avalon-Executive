package ray.eldath.avalon.executive.model

class Language(id: String, name: String, compileCmd: String, runCmd: String) {
  def getId: String = id

  def getName: String = name

  def getCompileCmd: String = compileCmd

  def getRunCmd: String = runCmd
}
