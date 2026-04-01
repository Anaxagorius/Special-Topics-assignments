# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Cache dependencies before copying full source
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy source and build
COPY src ./src
RUN mvn package -DskipTests -q

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/target/blackjack-1.0.0.jar app.jar

# Default: run the console Blackjack game
# Override with: docker run ... cardgames-tb java -cp app.jar <ClassName>
ENTRYPOINT ["java", "-cp", "app.jar", "BlackjackGame"]
