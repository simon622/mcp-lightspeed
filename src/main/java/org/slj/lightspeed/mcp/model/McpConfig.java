package org.slj.lightspeed.mcp.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slj.lightspeed.mcp.Constants;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record McpConfig (
        String name,
        String version,
        List<Tool> tools,
        List<Model> models,
        List<Resource> resources,
        List<Prompt> prompts,
        List<Api> apis
) {

    public record Tool(
            String name,
            String description,
            @JsonProperty("input_schema") Schema inputSchema,
            boolean enabled,
            McpToolType toolType,
            Api api,
            @JsonIgnore ApiRequestMethod requestMethod,
            @JsonIgnore Method method,
            @JsonIgnore String path
    ) {}

    public record Schema(
            String type,
            Map<String, Property> properties,
            List<String> required
    ) {}

    public record Property(
            String type,
            String description,
            Integer minimum
    ) {}

    public record Model(
            String id,
            String name,
            List<String> capabilities,
            Limits limits,
            String provider,
            boolean enabled
    ) {}

    public record Limits(
            @JsonProperty("context_length") Integer contextLength,
            @JsonProperty("output_tokens") Integer outputTokens
    ) {}

    public record Resource(
            String uri,
            String name,
            String description,
            String mimeType,
            boolean enabled
    ) {}

    public record Prompt(
            String name,
            String description,
            List<Argument> arguments,
            String template,
            boolean enabled
    ) {}

    public record Argument(
            String name,
            String type,
            String description
    ) {}

    public record Api(
            String name,
            String url,
            String apiKey,
            String username,
            String password,
            boolean enabled
    ) {}

    public static McpConfig create() {
        return new McpConfig(Constants.NAME, Constants.VERSION, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }
}

