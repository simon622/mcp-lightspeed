package org.slj.lightspeed.mcp.services.impl;

import org.slj.lightspeed.mcp.model.McpConfig;
import org.slj.lightspeed.mcp.model.ToolCallParams;
import org.slj.lightspeed.mcp.services.McpService;
import org.slj.lightspeed.mcp.services.McpToolHandler;

public class MethodToolHandler implements McpToolHandler {

    @Override
    public McpService.ToolCallResult callTool(final McpConfig.Tool tool, final ToolCallParams params) {
        System.err.println("MethodToolHandler not implemented yet");
        return null;
    }
}
