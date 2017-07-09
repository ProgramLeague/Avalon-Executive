package ray.eldath.avalon.executive.core;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import ray.eldath.avalon.executive.exception.RunErrorException;
import ray.eldath.avalon.executive.model.ExecState;
import ray.eldath.avalon.executive.model.Language;

import java.io.File;
import java.io.IOException;

import static ray.eldath.avalon.executive.tool.DockerOperator.instance;

public class Runner {
    public static ExecState run(Language language, File executableFile) throws DockerException, InterruptedException, RunErrorException, IOException {
        String image = language.getRunDockerImageName();
        instance().pull(image);
        ContainerConfig config = ContainerConfig.builder().networkDisabled(true).workingDir("/sandbox").image(image)
                .hostConfig(
                        HostConfig.builder().oomKillDisable(false).memory(67108864L).memorySwap(67108864L).build()
                ).healthcheck(
                        ContainerConfig.Healthcheck.builder().timeout(5L).build()
                ).build();
        String containerId = instance().createContainer(config);

        instance().copyFileIn(containerId, executableFile.getParentFile().toPath(), "/sandbox");
        ExecState state = instance().exec(containerId, new String[]{language.getRunCmd()});

        ContainerState containerState = instance().inspectContainer(containerId);
        if (containerState == null)
            throw new RuntimeException();
        Boolean oomKilled = containerState.oomKilled();
        if (oomKilled != null && oomKilled)
            throw new RunErrorException("<out of resources>");
        if (!containerState.running()) {
            if (containerState.finishedAt().getTime() - containerState.startedAt().getTime() >= 5000L)
                throw new RunErrorException("<out of time>");
            throw new RunErrorException("<unknown fatal error>");
        }
        instance().closeContainer(containerId);
        return state;
    }
}
