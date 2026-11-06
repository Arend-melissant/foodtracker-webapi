# ==============================================================================
# FoodTracker WebAPI - Container Image
# Base Image: Eclipse Temurin OpenJDK 17 JRE on Alpine Linux
# ==============================================================================
FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="FoodTracker Team"
LABEL description="Hexagonal Java WebAPI for Food & Adverse Reaction Tracking"

WORKDIR /app

# Copy shaded fat-JAR built by Maven
COPY target/foodtracker-webapi-1.0.0-SNAPSHOT.jar app.jar

# Configure default container environment
ENV SERVER_HOST=0.0.0.0 \
    SERVER_PORT=8080 \
    DATABASE_URL=jdbc:sqlite:/app/data/foodtracker.db \
    JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Declare volume for persistent SQLite database
VOLUME ["/app/data"]

# Expose WebAPI HTTP port
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
