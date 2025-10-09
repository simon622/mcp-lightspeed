# Use a JDK image to run the application
FROM eclipse-temurin:21-jre
WORKDIR /app
ARG version=0.0.1

# Copy the JAR file into the Docker image
COPY target/lightspeed-mcp-gateway-1.0.0.jar ./lightspeed-mcp-gateway-1.0.0.jar
COPY config/openapi.yml ./openapi.yml

# Copy the entrypoint script
COPY config/entrypoint.sh ./entrypoint.sh
RUN chmod +x ./entrypoint.sh

# Set default environment variables
ENV PORT=8080
ENV OPENAPI_SPEC=openapi.yml
ENV DOWNSTREAM_API=http://127.0.0.1:9000

# Set the entrypoint
ENTRYPOINT ["/app/entrypoint.sh"]