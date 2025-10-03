package org.slj.lightspeed.mcp.resources;

import com.googlecode.jsonrpc4j.JsonRpcServer;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.slj.lightspeed.mcp.services.McpService;
import org.slj.lightspeed.mcp.services.impl.McpRegistryImpl;
import org.slj.lightspeed.mcp.services.impl.McpServiceImpl;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Path("/mcp-rpc")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class McpJsonRcpResource {

    private final JsonRpcServer server;

    public McpJsonRcpResource() {
        this.server = new JsonRpcServer(new McpServiceImpl(McpRegistryImpl.getInstance()), McpService.class);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String handleRpc(String requestJson) throws Exception {
        try (ByteArrayInputStream in = new ByteArrayInputStream(requestJson.getBytes());
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            server.handleRequest(in, out);
            return out.toString();
        }
    }
}