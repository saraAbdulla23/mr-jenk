FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Updated JAR name to match Maven output
COPY ./target/user_service-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8082

CMD ["java", "-jar", "app.jar"]
