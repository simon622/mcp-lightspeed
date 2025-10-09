# mcp-lightspeed

## Building and Running with Docker

### Quick Start with Docker Compose (Recommended)

This runs the MCP server behind a Caddy reverse proxy with HTTPS:

```bash
# Build the JAR
mvn clean package

# Start both containers
BEARER_TOKEN=your_secret_token docker-compose up
```

Access the server at `https://localhost` (Caddy uses a self-signed certificate for local development).

### Environment Variables

- `PORT` - Server port (default: `8080`)
- `OPENAPI_SPEC` - OpenAPI spec file path (default: `openapi.yml`)
- `DOWNSTREAM_API` - Downstream API endpoint (default: `http://127.0.0.1:9000`)
- `BEARER_TOKEN` - **Required** - Bearer token for authentication

Example with custom values:
```bash
BEARER_TOKEN=your_secret_token \
DOWNSTREAM_API=http://127.0.0.1:9000 \
docker-compose up
```

### Running without Docker Compose

Build the image:
```bash
docker build -t lightspeed-mcp .
```

Run the container:
```bash
docker run -p 8080:8080 -e BEARER_TOKEN=your_secret_token lightspeed-mcp
```