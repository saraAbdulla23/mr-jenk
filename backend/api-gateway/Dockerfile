# Use same base JDK image as your working service
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the Spring Boot JAR
COPY ./target/api-gateway-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose API Gateway port
EXPOSE 8087

# Run the JAR
CMD ["java", "-jar", "app.jar"]
