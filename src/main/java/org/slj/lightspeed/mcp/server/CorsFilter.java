package org.slj.lightspeed.mcp.server;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;

@Provider
public class CorsFilter implements ContainerResponseFilter {
    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) throws IOException {
        response.getHeaders().add("Access-Control-Allow-Origin", "*");
        response.getHeaders().add("Access-Control-Allow-Headers", "origin, content-type, accept, authorization, cache-control, x-requested-with, x-mcp-endpoint-type");
        response.getHeaders().add("Access-Control-Allow-Credentials", "true");
        response.getHeaders().add("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
        response.getHeaders().add("Access-Control-Max-Age", "3600");
        
        // Add SSE-specific headers for Server-Sent Events
        if (request.getUriInfo().getPath().contains("/sse") || 
            request.getUriInfo().getPath().contains("/messages")) {
            response.getHeaders().add("Access-Control-Expose-Headers", "Content-Type, Cache-Control");
            response.getHeaders().add("Cache-Control", "no-cache");
        }
    }
}