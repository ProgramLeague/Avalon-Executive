package ray.eldath.avalon.executive.test.tool;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.io.IOException;

public class DockerOperatorTest {
    public static void main(String[] args) throws InterruptedException, DockerException, IOException {
        System.out.println(DockerOperator.instance().exec("06108c85354d", "echo Hello World!"));
    }
}
