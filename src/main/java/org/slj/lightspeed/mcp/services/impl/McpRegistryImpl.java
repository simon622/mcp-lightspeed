package org.slj.lightspeed.mcp.services.impl;

import io.swagger.v3.oas.models.OpenAPI;
import org.slj.lightspeed.mcp.model.McpConfig;
import org.slj.lightspeed.mcp.services.McpRegistry;
import org.slj.lightspeed.mcp.utilities.OpenApiReader;
import org.slj.lightspeed.mcp.utilities.OpenApiToMcpConverter;

import java.util.List;
import java.util.Objects;

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
        return config;
    }

    public void setConfig(final McpConfig config) {
        this.config = config;
    }

    public void discover(){
        initializeFromApis(config);
    }

    private void initializeFromApis(final McpConfig config){
        if(!config.apis().isEmpty()){
            if(this.config == null || !Objects.equals(this.apis,config.apis())){
                config.tools().clear();
                config.apis().forEach(api -> {
                    List<McpConfig.Tool> tools = readToolsFromApi(api);
                    config.tools().addAll(tools);
                });
                this.apis = config.apis();
            }
        }
    }

    private static List<McpConfig.Tool> readToolsFromApi(final McpConfig.Api api){
        String urlString = api.url();
        if (urlString == null || urlString.isBlank()) {
           throw new IllegalArgumentException("API URL cannot be null or blank");
        }
        OpenAPI openAPI = OpenApiReader.read(urlString);
        return OpenApiToMcpConverter.convert(api, openAPI);
    }
}
