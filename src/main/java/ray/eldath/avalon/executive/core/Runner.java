package ray.eldath.avalon.executive.core;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerState;
import ray.eldath.avalon.executive.exception.RunErrorException;
import ray.eldath.avalon.executive.model.ExecInfoSimple;
import ray.eldath.avalon.executive.model.ExecPair;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.model.Submission;
import ray.eldath.avalon.executive.pool.RunContainerPool;

import java.io.IOException;

import static ray.eldath.avalon.executive.tool.DockerOperator.instance;

public class Runner {
	private static final int _TIME_LIMIT_MILLISECONDS = 1500;

	public static ExecPair run(Language language, Submission submission)
			throws DockerException, InterruptedException, RunErrorException, IOException {
		String containerId = RunContainerPool.instance().getContainerId(language);
		ExecPair state = instance().exec(containerId, language.getRunCmd().replace("_file", String.valueOf(submission.getSubmitTime())));

		Thread.sleep(_TIME_LIMIT_MILLISECONDS);

		ExecInfoSimple info = instance().inspectExec(state.getExecId());

		if (info.isRunning())
			throw new RunErrorException("<out of time>");

		ContainerState containerState = instance().inspectContainer(containerId);
		if (containerState == null)
			throw new RuntimeException();
		Boolean oomKilled = containerState.oomKilled();
		if (oomKilled != null && oomKilled)
			throw new RunErrorException("<out of resources>");
		if (!containerState.running())
			throw new RunErrorException("<unknown error>");
		return state;
	}
}
