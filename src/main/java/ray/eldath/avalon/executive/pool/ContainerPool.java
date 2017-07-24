package ray.eldath.avalon.executive.pool;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.model.Language;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;

public abstract class ContainerPool implements Closeable {
	static Map<Language, String> map = new HashMap<>();

	abstract String newContainer(Language language) throws DockerException, InterruptedException;

	public String getContainerId(Language language) {
		try {
			if (map.containsKey(language)) {
				String value = map.get(language);
				if (!DockerOperator.instance().inspectContainer(value).running()) {
					map.remove(language);
					return create(language);
				}
				return map.get(language);
			}
			return create(language);
		} catch (DockerException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	private String create(Language language) {
		try {
			String result = newContainer(language);
			map.put(language, result);
			return result;
		} catch (DockerException | InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void close() {
		for (String thisContainerId : map.values()) {
			try {
				DockerOperator.instance().killContainer(thisContainerId);
			} catch (DockerException | InterruptedException ignore) {
			}
		}
	}
}
