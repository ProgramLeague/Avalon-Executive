package ray.eldath.avalon.executive.tool;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
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

    public String createContainer(String image) throws DockerException, InterruptedException {
        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(image)
                .build();
        String containerId = client.createContainer(containerConfig).id();
        client.startContainer(containerId);
        LOGGER.info("container created and running: " + containerId);
        return containerId;
    }

    public String createContainer(Path dockerfile, String imageName) throws InterruptedException, DockerException, IOException {
        return createContainer(createImage(dockerfile, imageName));
    }

    public String createContainerOnline(String imageName) throws DockerException, InterruptedException {
        client.pull(imageName);
        return createContainer(imageName);
    }

    public void copyFileIn(String containerId, Path input, String pathInContainer)
            throws InterruptedException, DockerException, IOException {
        client.copyToContainer(input, containerId, pathInContainer);
        StringBuilder builder = new StringBuilder();
        LOGGER.info(builder.append("copy file ").append(input.toString()).append(" into container ")
                .append(containerId).append(":").append(pathInContainer).append(" successful").toString());
    }

    public void copyFileOut(String containerId, Path output, String pathInContainer)
            throws DockerException, InterruptedException, IOException {
        IOUtils.copy(new TarArchiveInputStream(client.archiveContainer(containerId, pathInContainer)),
                new FileOutputStream(output.toFile()));
        StringBuilder builder = new StringBuilder();
        LOGGER.info(builder.append("copy files ").append(pathInContainer).append(" out of container ")
                .append(containerId).append(":").append(pathInContainer).append(" successful").toString());
    }

    public String exec(String containerId, String[] cmd) throws DockerException, InterruptedException {
        final String execId = client.execCreate(containerId, cmd).id();
        LOGGER.info("execute cmd to container successful: " + execId);
        try (final LogStream logStream = client.execStart(execId)) {
            return logStream.readFully();
        }
    }

    public void closeContainer(String containerId) throws DockerException, InterruptedException {
        client.stopContainer(containerId, 10);
        client.removeContainer(containerId);
    }

    DockerClient getClient() {
        return client;
    }

    public static DockerOperator getInstance() {
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
