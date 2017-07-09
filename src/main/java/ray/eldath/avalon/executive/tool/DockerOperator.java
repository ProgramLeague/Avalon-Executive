package ray.eldath.avalon.executive.tool;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerState;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ray.eldath.avalon.executive.model.ExecState;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public class DockerOperator implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerOperator.class);
    private static DockerOperator instance = null;
    private DockerClient client;

    private DockerOperator() throws DockerException, InterruptedException {
        client = DefaultDockerClient.builder().uri(URI.create("http://127.0.0.1:2375")).build();
        String version = client.version().version();
        LOGGER.info("docker connected: ", version);
    }

    public String createImage(Path dockerfilePath, String imageName) throws InterruptedException, DockerException, IOException {
        final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();
        String returnedImageId = client.build(dockerfilePath, imageName,
                message -> {
                    final String imageId = message.buildImageId();
                    if (imageId != null)
                        imageIdFromMessage.set(imageId);
                });
        LOGGER.info(String.format("image %s:%s created", imageName, returnedImageId));
        return returnedImageId;
    }

    public boolean isImageExist(String imageName) throws DockerException, InterruptedException {
        return !client.listImages(DockerClient.ListImagesParam.byName(imageName)).isEmpty();

    }

    public String createContainer(String image) throws DockerException, InterruptedException {
        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(image)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .workingDir("/sandbox")
                .build();
        return createContainer(containerConfig);
    }

    public String createContainer(ContainerConfig config) throws DockerException, InterruptedException {
        String containerId = client.createContainer(config).id();
        client.startContainer(containerId);
        LOGGER.info("container created and running: " + containerId);
        return containerId;
    }

    public String createContainer(Path dockerfile, String imageName) throws InterruptedException, DockerException, IOException {
        return createContainer(createImage(dockerfile, imageName));
    }

    /**
     * @deprecated 请使用 {@link #pull(String)} 和 {@link #createContainer(String)} 完成相同功能。
     */
    public String createContainerOnline(String imageName) throws DockerException, InterruptedException {
        client.pull(imageName);
        return createContainer(imageName);
    }

    public void pull(String imageName) throws DockerException, InterruptedException {
        client.pull(imageName);
    }

    public void copyFileIn(String containerId, Path input, String pathInContainer)
            throws InterruptedException, DockerException, IOException {
        client.copyToContainer(input, containerId, pathInContainer);
        StringBuilder builder = new StringBuilder();
        LOGGER.info(builder.append("copy file ").append(input.toString()).append(" into container ")
                .append(containerId).append(":").append(pathInContainer).append(" successful").toString());
    }

    public void copyFileOut(String containerId, File output, String pathInContainer)
            throws DockerException, InterruptedException, IOException {
        IOUtils.copy(new TarArchiveInputStream(client.archiveContainer(containerId, pathInContainer)),
                new FileOutputStream(output));
        StringBuilder builder = new StringBuilder();
        LOGGER.info(builder.append("copy files ").append(pathInContainer).append(" out of container ")
                .append(containerId).append(" successful").toString());
    }

    public ExecState exec(String containerId, String[] cmd) throws DockerException, InterruptedException {
        final String execId = client.execCreate(containerId, cmd).id();
        String log;
        try (final LogStream stream = client.execStart(execId)) {
            log = stream.readFully();
        }
        LOGGER.info("execute cmd to container successful: " + execId);
        Integer integer = client.execInspect(execId).exitCode();
        return new ExecState(integer == null ? -1 : integer, log);
    }

    public void closeContainer(String containerId) throws DockerException, InterruptedException {
        client.stopContainer(containerId, 10);
        client.removeContainer(containerId);
    }

    public ContainerState inspectContainer(String containerId) throws DockerException, InterruptedException {
        return client.inspectContainer(containerId).state();
    }

    public DockerClient getClient() {
        return client;
    }

    public static DockerOperator instance() {
        if (instance == null)
            try {
                instance = new DockerOperator();
            } catch (DockerException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        return instance;
    }

    @Override
    public void close() {
        client.close();
    }
}
