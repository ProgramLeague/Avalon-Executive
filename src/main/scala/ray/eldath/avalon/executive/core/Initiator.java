package ray.eldath.avalon.executive.core;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.pool.LanguagePool;

import java.util.List;

import static ray.eldath.avalon.executive.tool.DockerOperator.instance;

public class Initiator {
	public static void init() throws DockerException, InterruptedException {
		List<Language> all = LanguagePool.getAllLanguage();
        for (Language thisLanguage : all) {
            String run = thisLanguage.getRunDockerImageName();
            String compile = thisLanguage.getCompileDockerImageName();
            if (!instance().isImageExist(run))
                instance().pull(run);
            if (!instance().isImageExist(compile))
                instance().pull(compile);
        }
    }
}
