package org.slj.lightspeed.mcp.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class McpSseResourceSimpleTest {

    @Mock
    private Sse sse;
    
    @Mock
    private HttpServletRequest request;

    private McpSseResource mcpSseResource;

    @BeforeEach
    void setUp() {
        mcpSseResource = new McpSseResource(sse);
    }

    @Test
    void testConnect_EstablishesSseConnection() {
        // Given
        when(request.getRemoteAddr()).thenReturn("127.0.0.1");
        when(request.getRemotePort()).thenReturn(12345);
        when(request.getParameter("endpoint")).thenReturn("sse");
        var mockBuilder = mock(jakarta.ws.rs.sse.OutboundSseEvent.Builder.class);
        var mockEvent = mock(jakarta.ws.rs.sse.OutboundSseEvent.class);
        when(sse.newEventBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.name(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.id(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.data(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.reconnectDelay(any(Long.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockEvent);
        
        TestSseEventSink testSink = new TestSseEventSink();

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> mcpSseResource.connect(request, testSink));
        assertEquals(1, testSink.getSendCount());
    }

    @Test
    void testConnect_WithNullRequest_EstablishesConnection() {
        // Given
        var mockBuilder = mock(jakarta.ws.rs.sse.OutboundSseEvent.Builder.class);
        var mockEvent = mock(jakarta.ws.rs.sse.OutboundSseEvent.class);
        when(sse.newEventBuilder()).thenReturn(mockBuilder);
        when(mockBuilder.name(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.id(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.data(anyString())).thenReturn(mockBuilder);
        when(mockBuilder.reconnectDelay(any(Long.class))).thenReturn(mockBuilder);
        when(mockBuilder.build()).thenReturn(mockEvent);
        
        TestSseEventSink testSink = new TestSseEventSink();

        // When & Then - should not throw exception
        assertDoesNotThrow(() -> mcpSseResource.connect(null, testSink));
        assertEquals(1, testSink.getSendCount());
    }

    @Test
    void testHandleJsonRpcMessage_ToolsCall_UnknownTool() throws Exception {
        // Given - call to unknown tool should return error
        String jsonBody = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"echo\",\"arguments\":{\"message\":\"test\"}}}";

        // When
        Response response = mcpSseResource.handleJsonRpcMessage(jsonBody);

        // Then - Should return JSON-RPC error response
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) response.getEntity();
        assertEquals("2.0", responseEntity.get("jsonrpc"));
        assertEquals(1, responseEntity.get("id"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) responseEntity.get("error");
        assertNotNull(error);
        assertEquals(-32603, error.get("code"));
        assertTrue(error.get("message").toString().contains("Tool call failed"));
    }

    @Test
    void testHandleJsonRpcMessage_ToolsList() throws Exception {
        // Given
        String jsonBody = "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"tools/list\",\"params\":{}}";

        // When
        Response response = mcpSseResource.handleJsonRpcMessage(jsonBody);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) response.getEntity();
        assertEquals("2.0", responseEntity.get("jsonrpc"));
        assertEquals(2, responseEntity.get("id"));
        assertNotNull(responseEntity.get("result"));
    }

    @Test
    void testHandleJsonRpcMessage_ResourcesList() throws Exception {
        // Given
        String jsonBody = "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"resources/list\",\"params\":{}}";

        // When
        Response response = mcpSseResource.handleJsonRpcMessage(jsonBody);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) response.getEntity();
        assertEquals("2.0", responseEntity.get("jsonrpc"));
        assertEquals(3, responseEntity.get("id"));
        assertNotNull(responseEntity.get("result"));
    }

    @Test
    void testHandleJsonRpcMessage_Initialize() throws Exception {
        // Given
        String jsonBody = "{\"jsonrpc\":\"2.0\",\"id\":4,\"method\":\"initialize\",\"params\":{}}";

        // When
        Response response = mcpSseResource.handleJsonRpcMessage(jsonBody);

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) response.getEntity();
        assertEquals("2.0", responseEntity.get("jsonrpc"));
        assertEquals(4, responseEntity.get("id"));
        assertNotNull(responseEntity.get("result"));
    }

    @Test
    void testHandleJsonRpcMessage_UnknownMethod() throws Exception {
        // Given
        String jsonBody = "{\"jsonrpc\":\"2.0\",\"id\":5,\"method\":\"unknown/method\",\"params\":{}}";

        // When
        Response response = mcpSseResource.handleJsonRpcMessage(jsonBody);

        // Then
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        
        @SuppressWarnings("unchecked")
        Map<String, Object> responseEntity = (Map<String, Object>) response.getEntity();
        assertEquals("2.0", responseEntity.get("jsonrpc"));
        assertEquals(5, responseEntity.get("id"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> error = (Map<String, Object>) responseEntity.get("error");
        assertEquals(-32601, error.get("code"));
        assertTrue(error.get("message").toString().contains("Method not found"));
    }

    @Test
    void testTriggerTool_ShouldThrowForUnknownTool() throws Exception {
        // Given
        String toolName = "echo";
        String jsonArgs = "{\"message\": \"test\"}";

        // When & Then - should throw exception for unknown tool
        assertThrows(RuntimeException.class, () -> mcpSseResource.triggerTool(toolName, jsonArgs));
    }

    @Test
    void testShutdown_ShouldExecuteWithoutError() {
        // When & Then - should not throw exception
        assertDoesNotThrow(() -> mcpSseResource.shutdown());
    }


    @Test
    void testSseOptionsRequest_ReturnsCorsHeaders() {
        // When
        Response response = mcpSseResource.handleSseOptions();

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("*", response.getHeaderString("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, OPTIONS", response.getHeaderString("Access-Control-Allow-Methods"));
        assertEquals("3600", response.getHeaderString("Access-Control-Max-Age"));
        assertNotNull(response.getHeaderString("Access-Control-Allow-Headers"));
    }

    @Test
    void testMessagesOptionsRequest_ReturnsCorsHeaders() {
        // When
        Response response = mcpSseResource.handleMessagesOptions();

        // Then
        assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        assertEquals("*", response.getHeaderString("Access-Control-Allow-Origin"));
        assertEquals("GET, POST, OPTIONS", response.getHeaderString("Access-Control-Allow-Methods"));
        assertEquals("3600", response.getHeaderString("Access-Control-Max-Age"));
        assertNotNull(response.getHeaderString("Access-Control-Allow-Headers"));
    }

    // Test implementation of SseEventSink to avoid Mockito issues with Java 25
    private static class TestSseEventSink implements jakarta.ws.rs.sse.SseEventSink {
        private boolean closed = false;
        private int sendCount = 0;

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public java.util.concurrent.CompletionStage<?> send(jakarta.ws.rs.sse.OutboundSseEvent event) {
            if (closed) {
                return java.util.concurrent.CompletableFuture.failedFuture(new RuntimeException("Sink is closed"));
            }
            sendCount++;
            return java.util.concurrent.CompletableFuture.completedFuture(null);
        }

        public int getSendCount() {
            return sendCount;
        }
    }
}