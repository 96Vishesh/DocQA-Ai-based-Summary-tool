# Backend Dockerfile
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# Copy pom.xml first for dependency caching
COPY pom.xml .

# Download dependencies (this layer is cached if pom.xml doesn't change)
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests -B

# Runtime stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy the built jar
COPY --from=build /app/target/*.jar app.jar

# Create uploads directory
RUN mkdir -p /app/uploads

# Expose port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
