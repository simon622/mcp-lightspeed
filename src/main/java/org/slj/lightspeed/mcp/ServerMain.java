package org.slj.lightspeed.mcp;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.slj.lightspeed.mcp.model.McpTool;
import org.slj.lightspeed.mcp.services.impl.McpRegistryImpl;

import java.io.File;
import java.nio.file.Files;
import java.util.Objects;

public class ServerMain {

    public static void main(String[] args) throws Exception {

        // --------------------------
        // 1. Parse startup arguments
        // --------------------------
        if (args.length < 1) {
            System.err.println("""
                Usage: java -jar lightspeed-mcp.jar <port> <openapi.yml>
                Example: java -jar lightspeed-mcp.jar 9090 ./config/openapi.yml
                """);
            System.exit(1);
        }

        // Port first
        int port;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + args[0]);
            System.exit(1);
            return;
        }

        // Then YAML (OpenAPI spec) path
        if(args.length >= 2){
            String yamlPath = args[1];
            File yamlFile = new File(yamlPath);
            if (!yamlFile.exists()) {
                System.err.println("OpenAPI spec not found: " + yamlFile.getAbsolutePath());
                System.exit(1);
            }

            // Optionally read the YAML contents
            String yamlContent = Files.readString(yamlFile.toPath());
            McpRegistryImpl.getInstance().initializeWith(yamlContent);
        }


        // --------------------------
        // 2. Setup Jetty + Jersey
        // --------------------------
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");

        ResourceConfig config = new ResourceConfig()
                .packages("org.slj.lightspeed.mcp.resources")
                .register(org.glassfish.jersey.jackson.JacksonFeature.class);

        ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
        context.addServlet(jerseyServlet, "/api/*");

        // Serve static files from /web
        ServletHolder staticHolder = new ServletHolder("static", DefaultServlet.class);
        String resourceBase = Objects.requireNonNull(
                ServerMain.class.getClassLoader().getResource("web")).toExternalForm();

        staticHolder.setInitParameter("resourceBase", resourceBase);
        staticHolder.setInitParameter("dirAllowed", "true");
        staticHolder.setInitParameter("pathInfoOnly", "true");

        context.addServlet(staticHolder, "/static/*");

        // --------------------------
        // 3. Start the Jetty server
        // --------------------------
        Server server = new Server(port);
        server.setHandler(context);

        System.out.printf("Starting MCP server on http://localhost:%d ...%n", port);
        server.start();
        server.join();
    }

}
