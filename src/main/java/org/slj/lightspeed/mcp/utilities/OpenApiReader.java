package org.slj.lightspeed.mcp.utilities;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;

import java.net.MalformedURLException;
import java.net.URL;

public class OpenApiReader {

    public static final String TESTURL = "https://petstore3.swagger.io/api/v3/openapi.json";

    public static OpenAPI read(final String endpointUrl) {

        try {
            URL url = new URL(endpointUrl);
            OpenAPI openAPI = new OpenAPIV3Parser().read(endpointUrl);
            if (openAPI != null) {
                System.out.println("Title: " + openAPI.getInfo().getTitle());
                System.out.println("Version: " + openAPI.getInfo().getVersion());
                System.out.println("Paths: " + openAPI.getPaths().keySet());
            } else {
                System.err.println("Failed to parse OpenAPI spec");
            }
            return openAPI;

        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid API URL: " + endpointUrl + " (" + e.getMessage() + ")");
        }
    }

    public static void main(String[] args) {
        read("https://petstore3.swagger.io/api/v3/openapi.json");
    }
}