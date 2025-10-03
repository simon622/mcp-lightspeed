package org.slj.lightspeed.mcp.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.slj.lightspeed.mcp.model.McpConfig;

@Path("/mcp-config")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class McpConfigResource {

    @POST
    @Path("/save")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response save(final McpConfig config) {

        System.out.println("Received MCP config: " + config);

        // Example response
        return Response.ok()
                .entity("{\"status\":\"saved\",\"server\":\"" + config.name() + "\"}")
                .build();
    }
}
