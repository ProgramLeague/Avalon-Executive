package ray.eldath.avalon.executive.model

class ExecState(exitCode: Int, log: String) {
  def getExitCode: Int = exitCode

  def getLog: String = log
}
