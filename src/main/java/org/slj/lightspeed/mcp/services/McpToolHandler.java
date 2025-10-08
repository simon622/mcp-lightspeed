package org.slj.lightspeed.mcp.services;

import org.slj.lightspeed.mcp.model.McpConfig;
import org.slj.lightspeed.mcp.model.ToolCallParams;

public interface McpToolHandler {

    McpService.ToolCallResult callTool(McpConfig.Tool tool, ToolCallParams params);
}
