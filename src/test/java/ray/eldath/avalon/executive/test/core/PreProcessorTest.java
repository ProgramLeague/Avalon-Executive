package ray.eldath.avalon.executive.test.core;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.core.PreProcessor;

import java.io.IOException;

public class PreProcessorTest {
    private static final String workDir = "F:\\test";

    public static void main(String[] args) throws InterruptedException, DockerException, IOException {
        PreProcessor.instance().createFile(workDir, Protected.py3);
    }
}
