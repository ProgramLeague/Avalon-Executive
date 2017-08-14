package ray.eldath.avalon.executive.pool;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.nio.file.Paths;

public class CompilerContainerPool extends ContainerPool {
	private static CompilerContainerPool instance = new CompilerContainerPool();

	public static CompilerContainerPool instance() {
		return instance;
	}

	@Override
	String newContainer(Language language) throws DockerException, InterruptedException {
		String name = language.getCompileDockerImageName();
		if (!DockerOperator.instance().isImageExist(name))
			DockerOperator.instance().pull(name);
		String r = DockerOperator.instance().createContainer(name, Paths.get(Constants._WORK_DIR()));
		map.put(language, r);
		return r;
	}
}
