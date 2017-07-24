package ray.eldath.avalon.executive.pool;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.HostConfig;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.tool.DockerOperator;

public class RunContainerPool extends ContainerPool {

	private static RunContainerPool instance = new RunContainerPool();

	public static RunContainerPool instance() {
		return instance;
	}

	@Override
	String newContainer(Language language) throws DockerException, InterruptedException {
		String name = language.getRunDockerImageName();
		if (!DockerOperator.instance().isImageExist(name))
			DockerOperator.instance().pull(name);
		ContainerConfig config = ContainerConfig.builder()
				.networkDisabled(true)
				.workingDir("/sandbox")
				.image(name)
				.cmd("sh", "-c", "while :; do sleep 1; done")
				.hostConfig(
						HostConfig.builder().binds(HostConfig.Bind.builder()
								.from(Constants._WORK_DIR()).to("/sandbox").build())
								.oomKillDisable(false).memory(67108864L).memorySwap(67108864L).build()
				).build();
		String r = DockerOperator.instance().createContainer(config);
		map.put(language, r);
		return r;
	}
}
