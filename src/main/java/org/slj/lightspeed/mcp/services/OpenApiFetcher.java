package org.slj.lightspeed.mcp.services;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;

public class OpenApiFetcher {

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Fetches the OpenAPI spec from the given URL and returns it as a String.
     */
    public String fetchAsString(final String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json, application/yaml, */*")
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            return response.body();
        } else {
            throw new IOException("Failed to fetch OpenAPI spec. HTTP " + response.statusCode());
        }
    }

    /**
     * Fetches the OpenAPI spec and saves it to a file.
     */
    public Path fetchToFile(String url, Path outputFile) throws IOException, InterruptedException {
        String spec = fetchAsString(url);
        Files.writeString(outputFile, spec);
        return outputFile;
    }

    // Example usage
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java com.example.openapi.OpenApiFetcher <url> <outputFile>");
            return;
        }

        String url = args[0];
        Path output = Path.of(args[1]);

        OpenApiFetcher fetcher = new OpenApiFetcher();
        try {
            fetcher.fetchToFile(url, output);
            System.out.println("✅ OpenAPI spec downloaded to " + output.toAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println(" Failed to fetch OpenAPI spec: " + e.getMessage());
        }
    }
}


