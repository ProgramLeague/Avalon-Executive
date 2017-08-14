package ray.eldath.avalon.executive.pool

import java.util

import ray.eldath.avalon.executive.model.Submission

object SubmissionPool {
  private val submissions = new util.HashMap[Integer, Submission]

  def put(submission: Submission): Unit = SubmissionPool.submissions.put(submission.getId, submission)

  def rm(id: Int): Unit = SubmissionPool.submissions.remove(id)

  def getById(id: Int): Submission = SubmissionPool.submissions.get(id)

  def has(id: Int): Boolean = SubmissionPool.submissions.containsKey(id)
}