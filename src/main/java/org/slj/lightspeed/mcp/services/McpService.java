package org.slj.lightspeed.mcp.services;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.googlecode.jsonrpc4j.JsonRpcMethod;
import com.googlecode.jsonrpc4j.JsonRpcParam;
import org.slj.lightspeed.mcp.model.ToolCallParams;

import java.util.List;
import java.util.Map;

public interface McpService {

    record InitializeParams(
            @JsonProperty("protocolVersion") String protocolVersion,
            @JsonProperty("capabilities") Map<String, Object> capabilities
    ) {}

    record InitializeResult(
            @JsonProperty("serverInfo") Map<String, String> serverInfo,
            @JsonProperty("capabilities") Map<String, Object> capabilities
    ) {}

    record Tool(
            String name,
            String description,
            Map<String, Object> inputSchema
    ) {}

    record ToolsListResult(
            List<Tool> tools
    ) {}

    // === 3. tools/call ===
//    record ToolCallParams(
//            String name,
//            Map<String, Object> arguments
//    ) {}

    record ToolCallResult(
            List<Map<String, Object>> content
    ) {}

    @JsonRpcMethod("initialize")
    InitializeResult initialize(InitializeParams params);

    @JsonRpcMethod("tools/list")
    ToolsListResult toolsList();

    @JsonRpcMethod("tools/call")
    ToolCallResult toolsCall(@JsonRpcParam("name") String name, @JsonRpcParam("arguments") Map<String, Object> arguments);

//    @JsonRpcMethod("tools/call")
//    ToolCallResult toolsCall(ToolCallParams params);

//    public ToolCallResult toolsCall(String name, Map<String, Object> arguments)




}
