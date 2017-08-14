package ray.eldath.avalon.executive.main;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ray.eldath.avalon.executive.core.Initiator;
import ray.eldath.avalon.executive.pool.Constants;
import ray.eldath.avalon.executive.servlet.Compile;
import ray.eldath.avalon.executive.servlet.GetAllLang;
import ray.eldath.avalon.executive.servlet.Run;
import ray.eldath.avalon.executive.tool.PathUtils;

import java.nio.file.Paths;

public class MainServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(MainServer.class);

	private static final String CONTEXT_PATH = "/avalon/executive/v0";
	private static final int PORT = 11310;

	private static final class ShutdownHook extends Thread {
		@Override
		public void run() {
			LOGGER.info("Catch INT signal, Bye!");
			LOGGER.info("Do final cleaning...");
			PathUtils.deleteAll(Paths.get(Constants._WORK_DIR()));
			LOGGER.info("Final cleaning finished. System exited.");
		}
	}

	public static void main(String[] args) throws Exception {
		Initiator.init();

		Server server = new Server(PORT);
		ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
		context.setContextPath(CONTEXT_PATH);
		server.setHandler(context);
		server.setStopAtShutdown(true);

		context.addServlet(new ServletHolder(new GetAllLang()), "/get_all_lang");
		context.addServlet(new ServletHolder(new Compile()), "/compile");
		context.addServlet(new ServletHolder(new Run()), "/run");
		server.join();
		server.start();

		LOGGER.info("Avalon-Executive server is now running at http://127.0.0.1:" + PORT + CONTEXT_PATH);
	}
}
