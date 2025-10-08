package org.slj.lightspeed.mcp.resources;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/api")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocalServerTest {

    @POST
    @Path("/test-endpoint")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response apiEndpoint(final TestModel model) {

        System.out.println("Received MCP tool call!!!: " + model);
        return Response.ok().build();
    }


    static class TestModel {

        public String hello;
        public String world;

        public String getHello() {
            return hello;
        }

        public void setHello(final String hello) {
            this.hello = hello;
        }

        public String getWorld() {
            return world;
        }

        public void setWorld(final String world) {
            this.world = world;
        }
    }
}
