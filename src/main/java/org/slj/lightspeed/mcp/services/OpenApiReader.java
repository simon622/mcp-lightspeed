package org.slj.lightspeed.mcp.services;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

public class OpenApiReader {
    public static void read(final String endpoint) {

        String specUrl = "https://petstore3.swagger.io/api/v3/openapi.json";
        OpenAPI openAPI = new OpenAPIV3Parser().read(specUrl);

        if (openAPI != null) {
            System.out.println("Title: " + openAPI.getInfo().getTitle());
            System.out.println("Version: " + openAPI.getInfo().getVersion());
            System.out.println("Paths: " + openAPI.getPaths().keySet());
        } else {
            System.err.println("Failed to parse OpenAPI spec");
        }
    }

    public static void main(String[] args) {
        read("https://petstore3.swagger.io/api/v3/openapi.json");
    }
}