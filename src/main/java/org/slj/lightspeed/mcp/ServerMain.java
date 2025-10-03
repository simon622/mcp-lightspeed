package org.slj.lightspeed.mcp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;

import java.util.Objects;

public class ServerMain {

    public static void main(String[] args) throws Exception {

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        context.addServlet(ServletContainer.class, "/api/*").setInitParameter(
                "jersey.config.server.provider.packages", "org.slj.lightspeed.mcp.resources");

// Serve static content from classpath (src/main/resources/web)
        ServletHolder staticHolder = new ServletHolder("static", DefaultServlet.class);

        String resourceBase = Objects.requireNonNull(ServerMain.class.getClassLoader()
                .getResource("web")).toExternalForm();

        staticHolder.setInitParameter("resourceBase", resourceBase);
        staticHolder.setInitParameter("dirAllowed", "true");
        staticHolder.setInitParameter("pathInfoOnly", "true");

        context.addServlet(staticHolder, "/static/*");

        Server server = new Server(8080);
        server.setHandler(context);

        System.out.println("Starting server on http://localhost:8080 ...");
        server.start();
        server.join();
    }

}
