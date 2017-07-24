package ray.eldath.avalon.executive.test.core;

import com.spotify.docker.client.exceptions.DockerException;
import ray.eldath.avalon.executive.core.Compiler;
import ray.eldath.avalon.executive.core.PreProcessor;
import ray.eldath.avalon.executive.core.Runner;
import ray.eldath.avalon.executive.exception.CompileErrorException;
import ray.eldath.avalon.executive.exception.RunErrorException;
import ray.eldath.avalon.executive.model.ExecPair;
import ray.eldath.avalon.executive.pool.CompilerContainerPool;
import ray.eldath.avalon.executive.pool.LanguagePool;
import ray.eldath.avalon.executive.pool.RunContainerPool;

import java.io.IOException;

public class FullTest {
	private static class shutdownHook extends Thread {
		@Override
		public void run() {
			CompilerContainerPool.instance().close();
			RunContainerPool.instance().close();
		}
	}

	public static void main(String[] args) throws InterruptedException, DockerException, IOException {
		Runtime.getRuntime().addShutdownHook(new shutdownHook());
		System.out.println(Protected.py3.getSubmitTime());
		try {
			PreProcessor.instance().createFile(Protected.py3);
			Compiler.compile(Protected.py3);
		} catch (CompileErrorException e) {
			System.err.println(e.getState().toString());
		}
		try {
			ExecPair pair = Runner.run(LanguagePool.getById("py3"), Protected.py3);
			System.out.println(pair.toString());
		} catch (RunErrorException e) {
			System.err.println(e.toString());
		}
	}
}
