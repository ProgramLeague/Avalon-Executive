package ray.eldath.avalon.executive.model


class Language(id: String, name: String, dockerImageName: String, compileCmd: String, runCmd: String) {
  def getId: String = id

  def getName: String = name

  def getDockerImageName: String = dockerImageName

  def getCompileCmd: String = compileCmd

  def getRunCmd: String = runCmd
}
