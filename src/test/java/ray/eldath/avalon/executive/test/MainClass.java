package ray.eldath.avalon.executive.test;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.tool.DockerOperator;

import java.nio.file.Paths;

public class MainClass {
	public static void main(String[] args) throws DockerException, InterruptedException {
		System.out.println(DockerOperator.instance().createContainer("busybox", Paths.get("G:\\executive")));
	}
}
