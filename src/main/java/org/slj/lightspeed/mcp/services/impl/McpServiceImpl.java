package org.slj.lightspeed.mcp.services.impl;

import org.slj.lightspeed.mcp.model.McpConfig;
import org.slj.lightspeed.mcp.services.McpRegistry;
import org.slj.lightspeed.mcp.services.McpService;

import java.util.List;
import java.util.Map;

public class McpServiceImpl implements McpService {

    private final McpRegistry registry;

    public McpServiceImpl(McpRegistry registry) {
        this.registry = registry;
    }

    @Override
    public InitializeResult initialize(InitializeParams params) {
        return new InitializeResult(
            Map.of("name", "lightspeed-mcp-gateway", "version", "0.1.0"),
            Map.of("tools", Map.of())
        );
    }

    McpService.Tool toServiceTool(McpConfig.Tool cfg) {
        Map<String, Object> schema = Map.of(
                "type", cfg.inputSchema().type(),
                "properties", cfg.inputSchema().properties(),
                "required", cfg.inputSchema().required()
        );
        return new McpService.Tool(cfg.name(), cfg.description(), schema);
    }

    @Override
    public ToolsListResult toolsList() {
        List<McpService.Tool> tools = registry.getConfig().tools().stream()
                .filter(McpConfig.Tool::enabled)
                .map(this::toServiceTool)
                .toList();
        return new ToolsListResult(tools);
    }

    @Override
    public ToolCallResult toolsCall(ToolCallParams params) {
        // Lookup tool by name
        var tool = registry.getConfig().tools().stream()
                .filter(t -> t.name().equals(params.name()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknown tool: " + params.name()));

        System.err.println("Execute tool: " + tool.name() + " with args: " + params.arguments());

        return new ToolCallResult(List.of(
            Map.of("type", "text", "text", "Executed tool " + params.name() + " with args " + params.arguments())
        ));
    }
}