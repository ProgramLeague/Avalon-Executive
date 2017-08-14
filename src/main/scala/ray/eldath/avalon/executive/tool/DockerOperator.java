package ray.eldath.avalon.executive.tool;

import com.google.common.io.Files;
import com.spotify.docker.client.DefaultDockerClient;
import com.spotify.docker.client.DockerClient;
import com.spotify.docker.client.DockerClient.ExecCreateParam;
import com.spotify.docker.client.LogStream;
import com.spotify.docker.client.exceptions.DockerException;
import com.spotify.docker.client.messages.ContainerConfig;
import com.spotify.docker.client.messages.ContainerState;
import com.spotify.docker.client.messages.ExecState;
import com.spotify.docker.client.messages.HostConfig;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ray.eldath.avalon.executive.model.ExecInfoSimple;
import ray.eldath.avalon.executive.model.ExecPair;
import ray.eldath.avalon.executive.model.SafetyOutputStream;

import java.io.*;
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

	public String createContainer(String image, Path volume) throws DockerException, InterruptedException {
		ContainerConfig containerConfig = ContainerConfig.builder()
				.image(image)
				.workingDir("/sandbox")
				.hostConfig(
						HostConfig.builder().binds(HostConfig.Bind.builder().from(volume.toString()).to("/sandbox").build()).build()
				).cmd("sh", "-c", "while :; do sleep 1; done")
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
		File temp = new File(output.getParent() + File.separator + "_temp.tar");
		Files.createParentDirs(temp);
		if (!temp.createNewFile())
			throw new IOException("can not new temp file: " + temp.toString());
		TarArchiveOutputStream aos = new TarArchiveOutputStream(new FileOutputStream(temp));
		try (final TarArchiveInputStream tarStream = new TarArchiveInputStream(
				client.archiveContainer(containerId, pathInContainer))) {
			TarArchiveEntry entry;
			while ((entry = tarStream.getNextTarEntry()) != null) {
				aos.putArchiveEntry(entry);
				IOUtils.copy(tarStream, aos);
				aos.closeArchiveEntry();
			}
		}
		aos.finish();
		aos.close();
		boolean unTarStatus = unTar(new TarArchiveInputStream(new FileInputStream(temp)), output.getParent());
		StringBuilder builder = new StringBuilder();
		if (!unTarStatus)
			throw new RuntimeException("un tar file error");
		if (!temp.delete())
			builder.append("temp file delete failed, but ");
		LOGGER.info(builder.append("copy files ").append(pathInContainer).append(" out of container ")
				.append(containerId).append(" successful").toString());
	}

	public ExecPair exec(String containerId, String cmd) throws DockerException, InterruptedException, IOException {
		final String execId = client.execCreate(
				containerId,
				cmd.split(" "),
				ExecCreateParam.attachStdout(),
				ExecCreateParam.tty())
				.id();
		SafetyOutputStream outputStream = new SafetyOutputStream();
		try (final LogStream stream = client.execStart(execId)) {
			stream.attach(outputStream, outputStream);
		}
		LOGGER.info("execute cmd to container successful - execId: " + execId);
		String r = outputStream.get();
		return new ExecPair(execId, r
				.replace("\r", "")
				.replace("\n", " ")
		);
	}

	public ExecInfoSimple inspectExec(String execId) throws DockerException, InterruptedException {
		ExecState state = client.execInspect(execId);
		Integer integer = state.exitCode();
		return new ExecInfoSimple(state.running(), integer == null ? 0 : integer, state);
	}

	public void closeContainer(String containerId) throws DockerException, InterruptedException {
		client.stopContainer(containerId, 6);
		client.removeContainer(containerId);
	}

	public void killContainer(String containerId) throws DockerException, InterruptedException {
		client.killContainer(containerId);
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

	private boolean unTar(TarArchiveInputStream tarIn, String outputDir) throws IOException {
		ArchiveEntry entry;
		boolean newFile = false;
		while ((entry = tarIn.getNextEntry()) != null) {
			File tmpFile = new File(outputDir + "/" + entry.getName());
			newFile = tmpFile.createNewFile();
			OutputStream out = new FileOutputStream(tmpFile);
			int length;
			byte[] b = new byte[2048];
			while ((length = tarIn.read(b)) != -1)
				out.write(b, 0, length);
			out.close();
		}
		tarIn.close();
		return newFile;
	}
}
