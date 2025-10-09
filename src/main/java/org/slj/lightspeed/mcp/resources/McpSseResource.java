package org.slj.lightspeed.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slj.lightspeed.mcp.services.McpService;
import org.slj.lightspeed.mcp.services.impl.McpRegistryImpl;
import org.slj.lightspeed.mcp.services.impl.McpServiceImpl;
import jakarta.inject.Inject;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
@Path("/sse")
public class McpSseResource {

    private static final Logger log = LoggerFactory.getLogger(McpSseResource.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Sse sse;
    private final McpService mcpService;
    private final Set<SseEventSink> sinks = ConcurrentHashMap.newKeySet();
    private final ScheduledExecutorService heartbeatExec =
            Executors.newSingleThreadScheduledExecutor(r -> new Thread(r, "SSE-Heartbeat"));
    private final AtomicLong msgCounter = new AtomicLong();

    public McpSseResource(@Context Sse sse) {
        this.sse = sse;
        // Use the global registry instance that's initialized in ServerMain
        this.mcpService = new McpServiceImpl(McpRegistryImpl.getInstance());

        // periodic heartbeat
        heartbeatExec.scheduleAtFixedRate(this::broadcastHeartbeat, 5, 5, TimeUnit.SECONDS);
    }

    @GET
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void connect(@Context HttpServletRequest req, @Context SseEventSink sink) {
        String remote = req != null ? req.getRemoteAddr() + ":" + req.getRemotePort() : "unknown";
        log.info("SSE connection request from {}", remote);

        // Check for endpoint type parameter or header for logging
        String endpointType = req != null ? req.getParameter("endpoint") : null;
        if (endpointType == null && req != null) {
            endpointType = req.getHeader("X-MCP-Endpoint-Type");
        }
        
        log.info("SSE endpoint type: {} -> establishing connection", endpointType);
        
        // Add sink to active connections
        sinks.add(sink);
        log.info("SSE connection established from {} -> {}, total sinks: {}", remote, sink, sinks.size());

        // Send initial MCP initialize result
        try {
            var initResult = mcpService.initialize("2024-11-05", Map.of(), Map.of());
            sendJson(sink, Map.of(
                    "jsonrpc", "2.0",
                    "id", msgCounter.incrementAndGet(),
                    "timestamp", Instant.now().toString(),
                    "result", initResult
            ));
            log.info("Sent initial MCP initialize result to {}", remote);
        } catch (Exception e) {
            log.error("Failed to send init event to {}: {}", remote, e.getMessage(), e);
            closeSink(sink);
        }
    }


    @GET
    @Path("/messages/")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void connectToMessages(@Context HttpServletRequest req, @Context SseEventSink sink) {
        String remote = req != null ? req.getRemoteAddr() + ":" + req.getRemotePort() : "unknown";
        log.debug("SSE messages connection established from {} -> {}", remote, sink);

        if (req != null) {
            var headerNames = req.getHeaderNames();
            if (headerNames != null) {
                while (headerNames.hasMoreElements()) {
                    String name = headerNames.nextElement();
                    log.debug("Header: {} = {}", name, req.getHeader(name));
                }
            }
        }

        sinks.add(sink);

        // Send initial MCP initialize result
        try {
            var initResult = mcpService.initialize("2024-11-05", Map.of(), Map.of());
            sendJson(sink, Map.of(
                    "jsonrpc", "2.0",
                    "id", msgCounter.incrementAndGet(),
                    "timestamp", Instant.now().toString(),
                    "result", initResult
            ));
        } catch (Exception e) {
            log.error("Failed to send init event: {}", e.getMessage(), e);
            closeSink(sink);
        }
    }

    /** Broadcast a JSON-RPC message to all connected sinks */
    public void broadcastJsonRpc(String json) {
        for (SseEventSink sink : sinks) {
            try {
                if (!sink.isClosed()) {
                    OutboundSseEvent event = sse.newEventBuilder()
                            .name("message")
                            .id(String.valueOf(msgCounter.incrementAndGet()))
                            .data(json)
                            .reconnectDelay(5000)
                            .build();
                    sink.send(event);
                } else {
                    closeSink(sink);
                }
            } catch (Exception e) {
                log.warn("Dropping broken sink: {}", e.getMessage());
                closeSink(sink);
            }
        }
    }

    private void sendJson(SseEventSink sink, Object data) throws IOException {
        String json = MAPPER.writeValueAsString(data);
        OutboundSseEvent event = sse.newEventBuilder()
                .name("message")
                .id(String.valueOf(msgCounter.incrementAndGet()))
                .data(json)
                .reconnectDelay(5000)
                .build();
        sink.send(event);
    }

    /** Heartbeat pings to keep connection alive and detect drops */
    private void broadcastHeartbeat() {
        if (sinks.isEmpty()) return;
        String pingJson = "{\"type\":\"ping\",\"timestamp\":\"" + Instant.now() + "\"}";
        for (SseEventSink sink : sinks) {
            try {
                if (!sink.isClosed()) {
                    sink.send(sse.newEventBuilder()
                            .name("ping")
                            .data(pingJson)
                            .build());
                } else {
                    closeSink(sink);
                }
            } catch (Exception e) {
                log.debug("Heartbeat failed, closing sink: {}", e.getMessage());
                closeSink(sink);
            }
        }
        log.debug("❤️ Sent heartbeat to {} active SSE clients", sinks.size());
    }


    @POST
    @Path("/messages/")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response handleJsonRpcMessage(String jsonBody) throws Exception {
        log.info("📬 MCP JSON-RPC message received: {}", jsonBody);
        
        // Parse the JSON-RPC message
        var messageMap = MAPPER.readValue(jsonBody, Map.class);
        String method = (String) messageMap.get("method");
        Object id = messageMap.get("id");
        Map<String, Object> params = (Map<String, Object>) messageMap.getOrDefault("params", Map.of());
        
        Object result = null;
        
        // Handle different MCP methods
        switch (method != null ? method : "") {
            case "tools/call":
                String toolName = (String) params.get("name");
                Map<String, Object> arguments = (Map<String, Object>) params.getOrDefault("arguments", Map.of());
                try {
                    result = mcpService.toolsCall(toolName, arguments);
                } catch (RuntimeException e) {
                    // Return JSON-RPC error for tool call failures
                    return Response.status(Response.Status.OK)  // Still return 200 for JSON-RPC errors
                            .entity(Map.of("jsonrpc", "2.0", "id", id != null ? id : msgCounter.incrementAndGet(), 
                                    "error", Map.of("code", -32603, "message", "Tool call failed: " + e.getMessage())))
                            .build();
                }
                break;
            case "tools/list":
                result = mcpService.toolsList();
                break;
            case "resources/list":
                // Return empty resources list since this service focuses on tools
                result = Map.of("resources", List.of());
                break;
            case "initialize":
                result = mcpService.initialize("2024-11-05", Map.of(), Map.of());
                break;
            default:
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("jsonrpc", "2.0", "id", id, "error", 
                               Map.of("code", -32601, "message", "Method not found: " + method)))
                        .build();
        }
        
        // Return JSON-RPC response
        Map<String, Object> response = Map.of(
                "jsonrpc", "2.0",
                "id", id != null ? id : msgCounter.incrementAndGet(),
                "result", result,
                "timestamp", Instant.now().toString()
        );
        
        return Response.ok(response).build();
    }

    @POST
    @Path("/trigger/{tool}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void triggerTool(@PathParam("tool") String tool, String jsonArgs) throws Exception {
        log.info("⚙️ SSE tool call activated: {} -> {}", tool, jsonArgs);
        var argsMap = MAPPER.readValue(jsonArgs, Map.class);
        var result = mcpService.toolsCall(tool, argsMap);
        String json = MAPPER.writeValueAsString(Map.of(
                "jsonrpc", "2.0",
                "id", msgCounter.incrementAndGet(),
                "result", result,
                "timestamp", Instant.now().toString()
        ));
        broadcastJsonRpc(json);
    }

    private void closeSink(SseEventSink sink) {
        log.info("Sink CLOSED down SSE resource, closing {} sinks...", sinks.size());
        sinks.remove(sink);
        try { sink.close(); } catch (Exception ignore) {}
    }

    @PreDestroy
    public void shutdown() {
        log.info("🧹 Shutting down SSE resource, closing {} sinks...", sinks.size());
        heartbeatExec.shutdownNow();
        for (SseEventSink sink : sinks) {
            closeSink(sink);
        }
    }
}