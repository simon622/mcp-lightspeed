#!/bin/sh

# Require bearer token - exit if not provided
if [ -z "$BEARER_TOKEN" ]; then
    echo "Error: BEARER_TOKEN environment variable is required"
    exit 1
fi

# Run the Java application with environment variables
exec java -jar lightspeed-mcp-gateway-1.0.0.jar "$PORT" "$OPENAPI_SPEC" "$DOWNSTREAM_API" "$BEARER_TOKEN"
