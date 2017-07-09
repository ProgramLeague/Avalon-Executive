package ray.eldath.avalon.executive.pool;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.util.HashMap;
import java.util.Map;

public class CompilerContainerPool {
    private static Map<Language, String> map = new HashMap<>();

    private static CompilerContainerPool instance = new CompilerContainerPool();

    public static CompilerContainerPool instance() {
        return instance;
    }

    private String newContainer(Language language) throws DockerException, InterruptedException {
        String name = language.getCompileDockerImageName();
        if (!DockerOperator.instance().isImageExist(name))
            DockerOperator.instance().pull(language.getCompileDockerImageName());
        String r = DockerOperator.instance().createContainer(name);
        map.put(language, r);
        return r;
    }

    public String getContainerId(Language language) {
        if (map.containsKey(language))
            return map.get(language);
        try {
            return newContainer(language);
        } catch (DockerException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
