package org.slj.lightspeed.mcp.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slj.lightspeed.mcp.model.McpConfig;
import org.slj.lightspeed.mcp.model.ToolCallParams;
import org.slj.lightspeed.mcp.services.McpRegistry;
import org.slj.lightspeed.mcp.services.McpService;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class McpServiceImpl implements McpService {

    private static final Logger log = LoggerFactory.getLogger(McpServiceImpl.class);

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

        log.info("Tools-list command requested");

        List<McpService.Tool> tools = registry.getConfig().tools().stream()
                .filter(McpConfig.Tool::enabled)
                .map(this::toServiceTool)
                .toList();

        log.info("Tools-list command listed {} tools", tools.size());

        return new ToolsListResult(tools);
    }

    @Override
    public ToolCallResult toolsCall(String name, Map<String, Object> arguments) {
        return toolsCallOld(new ToolCallParams(name, arguments));
    }

//    @Override
    public ToolCallResult toolsCallOld(ToolCallParams params) {

        log.info("Making tools-call with params: {}", params);

        var tool = registry.getConfig().tools().stream()
                .filter(t -> t.name().equals(params.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unknown tool: " + params.getName()));

        log.info("Success - matched tool {} with arguments: {}", tool.name(), params.getArguments());

        try {
            return switch (tool.toolType()) {
                case Api -> new ApiToolHandler().callTool(tool, params);
                case JavaMethod -> new MethodToolHandler().callTool(tool, params);
            };
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("type", "error");
            error.put("text", "Error executing tool " + params.getName() + ": " + e.getMessage());
            return new ToolCallResult(List.of(error));
        }
    }
}