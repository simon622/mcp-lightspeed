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

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        // Add Jersey ServletContainer
        ResourceConfig config = new ResourceConfig()
                .packages("org.slj.lightspeed.mcp.resources")
                .register(org.glassfish.jersey.jackson.JacksonFeature.class);

        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
        context.addServlet(jerseyServlet, "/api/*");


// STATIC
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
