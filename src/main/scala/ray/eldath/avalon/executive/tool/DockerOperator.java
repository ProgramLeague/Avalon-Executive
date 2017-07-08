package ray.eldath.avalon.executive.tool;

import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ray.eldath.avalon.executive.pool.ConstantPool;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicReference;

public class DockerOperator implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(DockerOperator.class);
    private DockerClient client;
    private String containerId;

    public DockerOperator() throws DockerCertificateException, DockerException, InterruptedException, IOException {
        client = DefaultDockerClient.builder().uri(URI.create("http://127.0.0.1:2375")).build();
        String version = client.version().version();
        LOGGER.info("docker connected: ", version);
        String imageId = "";
        if (!RunDataSystem.getBoolean("created_image"))
            imageId = createImage();
        // Create container
        ContainerConfig containerConfig = ContainerConfig.builder()
                .image(imageId.isEmpty() ? "ray-eldath/avalon-executive" : imageId)
                .cmd("sh", "-c", "while :; do sleep 1; done")
                .build();
        containerId = client.createContainer(containerConfig).id();
        client.startContainer(containerId);
        LOGGER.info("container created and running: " + containerId);
    }

    public void copyFileIn(Path input, String pathInContainer) throws InterruptedException, DockerException, IOException {
        client.copyToContainer(input, containerId, pathInContainer);
        StringBuilder builder = new StringBuilder();
        LOGGER.info(builder.append("copy file ").append(input.toString()).append(" into container ")
                .append(containerId).append(":").append(pathInContainer).append(" successful").toString());
    }

    public void copyFileOut(Path output, String pathInContainer) throws DockerException, InterruptedException, IOException {
        IOUtils.copy(new TarArchiveInputStream(client.archiveContainer(containerId, pathInContainer)),
                new FileOutputStream(output.toFile()));
        StringBuilder builder = new StringBuilder();
        LOGGER.info(builder.append("copy files ").append(pathInContainer).append(" out of container ")
                .append(containerId).append(":").append(pathInContainer).append(" successful").toString());
    }

    public DockerClient getClient() {
        return client;
    }

    @Override
    public void close() throws IOException {
        try {
            client.stopContainer(containerId, 10);
            client.removeContainer(containerId);
        } catch (DockerException | InterruptedException e) {
            LOGGER.error(e.toString());
        }
        client.close();
    }

    private String createImage() throws InterruptedException, DockerException, IOException {
        final AtomicReference<String> imageIdFromMessage = new AtomicReference<>();
        String returnedImageId = client.build(
                Paths.get(ConstantPool.currentPath() + "/Dockerfile"), "ray-eldath/avalon-executive", message -> {
                    final String imageId = message.buildImageId();
                    if (imageId != null)
                        imageIdFromMessage.set(imageId);
                });
        RunDataSystem.putBoolean("created_image", true);

        return returnedImageId;
    }
}
