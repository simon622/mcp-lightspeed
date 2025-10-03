package org.slj.lightspeed.mcp.utilities;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.parameters.Parameter;
import org.slj.lightspeed.mcp.model.McpConfig;

import java.util.*;

public class OpenApiToMcpConverter {

    public static List<McpConfig.Tool> convert(McpConfig.Api api, OpenAPI openAPI) {
        List<McpConfig.Tool> tools = new ArrayList<>();

        if (openAPI.getPaths() == null) {
            return tools;
        }

        openAPI.getPaths().forEach((path, pathItem) -> {
            pathItem.readOperationsMap().forEach((httpMethod, operation) -> {
                McpConfig.Tool tool = convertOperation(api, path, httpMethod.name(), operation);
                tools.add(tool);
            });
        });

        return tools;
    }

    private static McpConfig.Tool convertOperation(McpConfig.Api api, String path, String method, Operation operation) {
        // Tool name: operationId or fallback
        String name = (operation.getOperationId() != null && !operation.getOperationId().isBlank())
                ? operation.getOperationId()
                : method.toLowerCase() + "_" + path.replace("/", "_");

        // Description: summary > description > fallback
        String description = Optional.ofNullable(operation.getSummary())
                .orElse(Optional.ofNullable(operation.getDescription()).orElse("No description"));

        // Build schema
        Map<String, McpConfig.Property> properties = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();

        // Parameters
        if (operation.getParameters() != null) {
            for (Parameter param : operation.getParameters()) {
                Schema<?> schema = param.getSchema();
                if (schema != null) {
                    properties.put(param.getName(),
                            new McpConfig.Property(schema.getType(), schema.getDescription(), schema.getMinItems()));
                    if (Boolean.TRUE.equals(param.getRequired())) {
                        required.add(param.getName());
                    }
                }
            }
        }

        // Request body
        if (operation.getRequestBody() != null && operation.getRequestBody().getContent() != null) {
            Content content = operation.getRequestBody().getContent();
            MediaType mt = content.get("application/json");
            if (mt != null && mt.getSchema() != null) {
                McpConfig.Schema bodySchema = mapSchema(mt.getSchema());
                if (bodySchema != null && bodySchema.properties() != null) {
                    properties.putAll(bodySchema.properties());
                }
                if (bodySchema != null && bodySchema.required() != null) {
                    required.addAll(bodySchema.required());
                }
            }
        }

        McpConfig.Schema schema = new McpConfig.Schema("object", properties, required);

        return new McpConfig.Tool(name, description, schema, true, api);
    }

    private static McpConfig.Schema mapSchema(io.swagger.v3.oas.models.media.Schema<?> swaggerSchema) {
        if (swaggerSchema == null) return null;

        Map<String, McpConfig.Property> props = new LinkedHashMap<>();
        if (swaggerSchema.getProperties() != null) {
            swaggerSchema.getProperties().forEach((k, v) -> {
                io.swagger.v3.oas.models.media.Schema<?> s = (io.swagger.v3.oas.models.media.Schema<?>) v;
                props.put(k, new McpConfig.Property(
                        s.getType(),
                        s.getDescription(),
                        s.getMinItems()
                ));
            });
        }

        List<String> required = swaggerSchema.getRequired() != null
                ? new ArrayList<>(swaggerSchema.getRequired())
                : List.of();

        return new McpConfig.Schema("object", props, required);
    }
}