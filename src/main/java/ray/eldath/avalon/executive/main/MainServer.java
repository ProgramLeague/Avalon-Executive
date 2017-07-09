package ray.eldath.avalon.executive.main;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ray.eldath.avalon.executive.servlet.Compile;

public class MainServer {
    public static void main(String[] args) throws Exception {
        Server server = new Server(80);
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/avalon/executive/v0");
        server.setHandler(context);
        server.setStopAtShutdown(true);

        context.addServlet(new ServletHolder(new Compile()), "/compile");
    }
}
