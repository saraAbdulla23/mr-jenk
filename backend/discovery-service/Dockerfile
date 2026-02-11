# Use same base JDK image as your working service
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the Spring Boot JAR
COPY ./target/discovery-service-0.0.1-SNAPSHOT.jar /app/app.jar

# Expose Eureka port
EXPOSE 8761

# Run the JAR
CMD ["java", "-jar", "app.jar"]
