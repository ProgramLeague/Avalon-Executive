package ray.eldath.avalon.executive.model

import com.spotify.docker.client.messages.ExecState

class ExecInfoSimple(running: Boolean, exitCode: Int, full: ExecState) {
  def isRunning: Boolean = running

  def getExitCode: Int = exitCode

  def getFull: ExecState = full

  override def toString = s"ExecInfoSimple(running=$isRunning, exitCode=$getExitCode)"
}
