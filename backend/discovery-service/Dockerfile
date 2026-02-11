# Temurin 21 JDK Alpine
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy the JAR from the target folder (ensure Maven build ran first!)
COPY ./target/discovery-service-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8761

CMD ["java", "-jar", "app.jar"]
