# Multi-stage build for Spring Boot application
FROM gradle:8.11.1-jdk17 AS builder

# Set working directory
WORKDIR /app

# Copy gradle wrapper and build file first
COPY gradlew gradlew.bat build.gradle ./
COPY gradle/ gradle/

# Give execute permission to gradlew
RUN chmod +x gradlew

# Copy source code
COPY src/ src/

# Build the application
RUN gradle clean build -x test

# Runtime stage
FROM eclipse-temurin:17-jre

# Set working directory
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Expose port (8081)
EXPOSE 8081

# Set environment variables
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SERVER_PORT=8081

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:${SERVER_PORT}/actuator/health || exit 1

# Run the application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=${SERVER_PORT}"]
