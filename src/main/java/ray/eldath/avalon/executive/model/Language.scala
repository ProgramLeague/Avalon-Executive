package ray.eldath.avalon.executive.model


class Language(id: String, name: String, buildDockerImageName: String, runDockerImageName: String, compileCmd: String, runCmd: String) {
  var compileNeeded = false

  def isCompileNeeded: Boolean = compileNeeded

  def setDoNotNeedCompile(): Language = {
    compileNeeded = true
    this
  }

  def getId: String = id

  def getName: String = name

  def getCompileDockerImageName: String = buildDockerImageName

  def getRunDockerImageName: String = runDockerImageName

  def getCompileCmd: String = compileCmd

  def getRunCmd: String = runCmd
}
