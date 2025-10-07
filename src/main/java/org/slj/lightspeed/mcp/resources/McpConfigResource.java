package org.slj.lightspeed.mcp.resources;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slj.lightspeed.mcp.model.McpConfig;
import org.slj.lightspeed.mcp.services.impl.McpRegistryImpl;

@Path("/mcp-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class McpConfigResource {

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(final McpConfig config) {

        System.out.println("Received MCP config: " + config);
        McpRegistryImpl.getInstance().setConfig(config);
        return Response.ok()
                .build();
    }

    @GET
    @Path("/load")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response load() {

        McpConfig config = McpRegistryImpl.getInstance().getConfig();
        System.out.println("Loading MCP config: " + config);
        return Response.ok(config)
                .build();
    }

    @POST
    @Path("/discover")
    @Consumes(MediaType.APPLICATION_JSON)
    public McpConfig discover() {

        McpRegistryImpl.getInstance().discover();
        return McpRegistryImpl.getInstance().getConfig();
    }
}
