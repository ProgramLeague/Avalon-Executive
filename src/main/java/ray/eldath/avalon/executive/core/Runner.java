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
import java.nio.file.Path;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static ray.eldath.avalon.executive.tool.DockerOperator.instance;

public class Runner {
    private static final int _TIME_LIMIT_MILLISECONDS = 1500;

    public static ExecPair run(Language language, File executableFile)
            throws DockerException, InterruptedException, RunErrorException, IOException {
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
        instance().killContainer(containerId);

        delAll(executableFile.getParentFile().toPath());

        return state;
    }

    private static void delAll(Path path) throws IOException {
        List<Path> paths = Files.list(path).collect(toList());
        for (Path thisPath : paths)
            Files.deleteIfExists(thisPath);
        Files.deleteIfExists(path);
    }
}
