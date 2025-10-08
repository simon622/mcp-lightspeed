package org.slj.lightspeed.mcp.services.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slj.lightspeed.mcp.model.McpConfig;
import org.slj.lightspeed.mcp.model.ToolCallParams;
import org.slj.lightspeed.mcp.services.McpService;
import org.slj.lightspeed.mcp.services.McpToolHandler;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ApiToolHandler implements McpToolHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiToolHandler.class);

    @Override
    public McpService.ToolCallResult callTool(final McpConfig.Tool tool, final ToolCallParams params) {

        try {
            // Resolve API config
            var api = tool.api();
            if (api == null || api.url() == null || api.url().isBlank()) {
                throw new IllegalStateException("Tool has no associated API endpointUrl: " + tool.name());
            }

            // Combine endpoint base + path if path is set
            String fullUrl = api.url();
            String path = tool.path();

            if (path != null && !path.isBlank()) {
                fullUrl = api.url().replaceAll("/+$", "") + "/" + path.replaceAll("^/+", "");
            }

            log.info("Invoking API endpoint {}, arguments: {}", fullUrl, params.getArguments());

            // Prepare JSON body
            ObjectMapper mapper = new ObjectMapper();
            String jsonBody = mapper.writeValueAsString(params.getArguments());

            // Open connection
            URL url = new URL(fullUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod(tool.requestMethod() != null ? tool.requestMethod().name() : "POST");
            conn.setRequestProperty("Content-Type", "application/json");

            // -------------------------
            // Add Authorization headers
            // -------------------------
            if (api.apiKey() != null && !api.apiKey().isBlank()) {
                log.info("Using Bearer token {} for API authorization", api.apiKey());
                conn.setRequestProperty("Authorization", "Bearer " + api.apiKey());
            } else if (api.username() != null && api.password() != null) {
                log.info("Using Basic authentication {},**** for API authorization", api.username());
                String encoded = Base64.getEncoder().encodeToString(
                        (api.username() + ":" + api.password()).getBytes(StandardCharsets.UTF_8));
                conn.setRequestProperty("Authorization", "Basic " + encoded);
            }

            conn.setDoOutput(true);

            // -------------------------
            // Write JSON body for POST/PUT/PATCH
            // -------------------------
            String method = conn.getRequestMethod().toUpperCase();
            if (List.of("POST", "PUT", "PATCH").contains(method)) {
                try (OutputStream os = conn.getOutputStream()) {
                    os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
                }
            }

            // -------------------------
            // Read response
            // -------------------------
            int status = conn.getResponseCode();
            InputStream stream = (status >= 200 && status < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            String responseBody;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                responseBody = reader.lines().reduce("", (a, b) -> a + b);
            }

            conn.disconnect();

            // -------------------------
            // Return structured result
            // -------------------------
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("type", "text");
            result.put("text", String.format("Executed %s → HTTP %d", tool.name(), status));
            result.put("response", responseBody);

            return new McpService.ToolCallResult(List.of(result));

        } catch(Exception e){
            throw new RuntimeException();
        }
    }
}
