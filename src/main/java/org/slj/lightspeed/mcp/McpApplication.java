package org.slj.lightspeed.mcp;

import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;
import org.slj.lightspeed.mcp.resources.McpConfigResource;

import java.util.Set;

@ApplicationPath("/api")
public class McpApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(McpConfigResource.class);
    }
}