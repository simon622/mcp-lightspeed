package org.slj.lightspeed.mcp.services;

import org.slj.lightspeed.mcp.model.McpConfig;

public interface McpRegistry {

    McpConfig getConfig();

    void setConfig(McpConfig config);
}

