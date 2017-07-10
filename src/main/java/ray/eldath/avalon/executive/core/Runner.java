package ray.eldath.avalon.executive.core;

import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.HostConfig;
import ray.eldath.avalon.executive.exception.RunErrorException;
import ray.eldath.avalon.executive.model.ExecInfoSimple;
import ray.eldath.avalon.executive.model.ExecPair;
import ray.eldath.avalon.executive.model.Language;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static ray.eldath.avalon.executive.tool.DockerOperator.instance;

public class Runner {
    private static final int TIME_LIMIT_MILLISECONDS = 5000;

    public static ExecPair run(Language language, File executableFile) throws DockerException, InterruptedException, RunErrorException, IOException {
        String image = language.getRunDockerImageName();
        ContainerConfig config = ContainerConfig.builder()
                .networkDisabled(true)
                .workingDir("/sandbox")
                .image(image)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .hostConfig(
                        HostConfig.builder().oomKillDisable(false).memory(67108864L).memorySwap(67108864L).build()
                ).build();
        String containerId = instance().createContainer(config);
        instance().copyFileIn(containerId, executableFile.getParentFile().toPath(), "/sandbox");
        ExecPair state = instance().exec(containerId, language.getRunCmd());

        Thread.sleep(TIME_LIMIT_MILLISECONDS);

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
        instance().killContainer(containerId);
        Files.deleteIfExists(executableFile.getParentFile().toPath());
        return state;
    }
}
