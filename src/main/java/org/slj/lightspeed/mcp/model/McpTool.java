package org.slj.lightspeed.mcp.model;

import java.lang.annotation.*;

/**
 * Marks a method as an MCP Tool definition.
 * The name and description can be used to expose
 * the tool via an MCP registry or LLM tool manifest.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface McpTool {

    /**
     * The unique name of the tool, as seen by the LLM or registry.
     */
    String name();

    /**
     * Optional human-readable description for documentation or schema export.
     */
    String description() default "";

    /**
     * Optional category or namespace.
     */
    String category() default "";

    /**
     * Whether the tool is enabled for exposure by default.
     */
    boolean enabled() default true;
}