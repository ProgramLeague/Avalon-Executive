package ray.eldath.avalon.executive.model

class ExecPair(execId: String, log: String) {
  def getExecId: String = execId

  def getLog: String = log

  override def toString = s"ExecPair(execId: $execId, log: $getLog)"
}
