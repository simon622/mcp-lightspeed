package org.slj.lightspeed.mcp.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InitializeResult(

        @JsonProperty("protocolVersion")
        String protocolVersion,

        @JsonProperty("capabilities")
        Capabilities capabilities,

        @JsonProperty("serverInfo")
        ServerInfo serverInfo,

        @JsonProperty("instructions")
        String instructions
) {

    public static record Capabilities(
            @JsonProperty("logging")
            Map<String, Object> logging,

            @JsonProperty("prompts")
            Prompts prompts,

            @JsonProperty("resources")
            Resources resources,

            @JsonProperty("tools")
            Tools tools
    ) {}

    public static record Prompts(
            @JsonProperty("listChanged")
            boolean listChanged
    ) {}

    public static record Resources(
            @JsonProperty("subscribe")
            boolean subscribe,

            @JsonProperty("listChanged")
            boolean listChanged
    ) {}

    public static record Tools(
            @JsonProperty("listChanged")
            boolean listChanged
    ) {}

    public static record ServerInfo(
            @JsonProperty("name")
            String name,

            @JsonProperty("title")
            String title,

            @JsonProperty("version")
            String version
    ) {}
}