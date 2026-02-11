FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY ./target/product-service-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8085

CMD ["java", "-jar", "app.jar"]
