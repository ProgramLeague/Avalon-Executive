package ray.eldath.avalon.executive.test.tool;

import com.spotify.docker.client.exceptions.DockerCertificateException;
import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.pool.ConstantPool;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.io.IOException;
import java.nio.file.Paths;

public class DockerOperatorTest {
    public static void main(String[] args) throws IOException, InterruptedException, DockerException, DockerCertificateException {
        String containerId = DockerOperator.getInstance().createContainer(Paths.get(
                ConstantPool.currentPath() + "/docker"), "ray-eldath/avalon-executive");
        try {
            DockerOperator.getInstance().copyFileIn(containerId, Paths.get("F:\\test"), "/sandbox");
        } catch (Exception e) {
            DockerOperator.getInstance().closeContainer(containerId);
            DockerOperator.getInstance().close();
            e.printStackTrace();
        }
    }
}
