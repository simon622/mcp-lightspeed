package org.slj.lightspeed.mcp.services.impl;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.models.OpenAPI;
import org.slj.lightspeed.mcp.model.ApiRequestMethod;
import org.slj.lightspeed.mcp.model.McpConfig;
import org.slj.lightspeed.mcp.model.McpToolType;
import org.slj.lightspeed.mcp.services.McpRegistry;
import org.slj.lightspeed.mcp.utilities.McpToolScanner;
import org.slj.lightspeed.mcp.utilities.OpenApiReader;
import org.slj.lightspeed.mcp.utilities.OpenApiToMcpConverter;

import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public class McpRegistryImpl implements McpRegistry {

    private McpRegistryImpl() {}

    private static volatile McpRegistryImpl instance;

    private McpConfig config;
    private List<McpConfig.Api> apis;

    public static McpRegistryImpl getInstance() {
        if(instance == null){
            synchronized (McpRegistryImpl.class) {
                if(instance == null){
                    instance = new McpRegistryImpl();
                }
            }
        }
        return instance;
    }

    public McpConfig getConfig() {
        if(config == null){
            synchronized (McpRegistryImpl.class) {
                if (config == null){
                    initialize();
                }
            }
        }
        return config;
    }

    public synchronized void initialize(){
        config = McpConfig.create();
        discoverFromClasspath(config);
    }

    public void initializeWith(String openApiSpec, String endpointUrl, String bearerToken){

        initialize();
        OpenAPI openAPI = OpenApiReader.readFromFile(openApiSpec);
        McpConfig.Api api = new McpConfig.Api(openAPI.getInfo().getTitle(),endpointUrl, bearerToken, null, null, true);
        List<McpConfig.Tool> tools = OpenApiToMcpConverter.convert(api, openAPI);
        config.tools().addAll(tools);
        config.apis().add(api);
    }

    public void setConfig(final McpConfig config) {
        this.config = config;
    }

    public void discover(){

        config.tools().clear();

        //-- discover from API
        discoverFromApis(config);

        //-- discovery from classpath
        discoverFromClasspath(config);
    }

    private void discoverFromApis(final McpConfig config){
        if(!config.apis().isEmpty()){
            if(this.config == null || !Objects.equals(this.apis,config.apis())){
                config.apis().forEach(api -> {
                    List<McpConfig.Tool> tools = readToolsFromApi(api);
                    config.tools().addAll(tools);
                });
                this.apis = config.apis();
            }
        }
    }

    private void discoverFromClasspath(final McpConfig config){

        List<McpToolScanner.McpToolMethod> methods = McpToolScanner.findAll("org.slj.lightspeed.mcp.tools");

        for (McpToolScanner.McpToolMethod toolMethod : methods) {

            Method m = toolMethod.method;

            // Build a human-readable signature
            String signature = Arrays.stream(m.getParameters())
                    .map(p -> p.getType().getSimpleName() + " " + p.getName())
                    .collect(Collectors.joining(", ", m.getName() + "(", ")"))
                    + " : " + m.getReturnType().getSimpleName();

            Map<String, McpConfig.Property> properties = new LinkedHashMap<>();
            for (var p : m.getParameters()) {
                properties.put(
                        p.getName(),
                        new McpConfig.Property(
                                toJsonType(p.getType()),             // type
                                "Parameter of type " + p.getType().getSimpleName() , null
                        )
                );
            }

            McpConfig.Schema schema = new McpConfig.Schema(
                    "object",
                    properties,
                    new ArrayList<>(properties.keySet()) // mark all as required
            );


            // Append signature to description so you don’t need to change your model
            String descWithSig = (toolMethod.description == null || toolMethod.description.isBlank())
                    ? "Signature: " + signature
                    : toolMethod.description + " | Signature: " + signature;

            McpConfig.Tool tool = new McpConfig.Tool(
                    toolMethod.name,
                    descWithSig,
                    schema,
                    toolMethod.enabled,
                    McpToolType.JAVA_METHOD,
                    null,null,
                    toolMethod.method,null
            );

            if (config.tools().stream().noneMatch(t -> t.name().equals(tool.name()))) {
                config.tools().add(tool);
            }
        }
    }

    private static String toJsonType(Class<?> t) {
        if (t == String.class || CharSequence.class.isAssignableFrom(t)) return "string";
        if (t == int.class || t == Integer.class || t == long.class || t == Long.class
                || t == short.class || t == Short.class || t == byte.class || t == Byte.class) return "integer";
        if (t == float.class || t == Float.class || t == double.class || t == Double.class) return "number";
        if (t == boolean.class || t == Boolean.class) return "boolean";
        if (t.isArray() || Collection.class.isAssignableFrom(t)) return "array";
        if (Map.class.isAssignableFrom(t)) return "object";
        return "object"; // default for custom types
    }

    private static List<McpConfig.Tool> readToolsFromApi(final McpConfig.Api api){
        String urlString = api.url();
        if (urlString == null || urlString.isBlank()) {
           throw new IllegalArgumentException("API URL cannot be null or blank");
        }
        OpenAPI openAPI = OpenApiReader.readFromEndpoint(urlString);
        return OpenApiToMcpConverter.convert(api, openAPI);
    }
}
