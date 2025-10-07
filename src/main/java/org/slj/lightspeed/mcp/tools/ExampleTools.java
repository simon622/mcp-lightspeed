package org.slj.lightspeed.mcp.tools;

import org.slj.lightspeed.mcp.model.McpTool;

public class ExampleTools {

    @McpTool(name = "add", description = "add two numbers and return an integer")
    public int add(int a, int b) {
        return a + b;
    }

    @McpTool(name = "subtract", description = "subtract one number way from another")
    public int subtract(int a, int b) {
        return a - b;
    }

}
