package ray.eldath.avalon.executive.model


class Language(id: String, name: String, buildDockerImageName: String, runDockerImageName: String, compileCmd: String, runCmd: String) {
  def getId: String = id

  def getName: String = name

  def getCompileDockerImageName: String = buildDockerImageName

  def getRunDockerImageName: String = runDockerImageName

  def getCompileCmd: String = compileCmd

  def getRunCmd: String = runCmd
}
