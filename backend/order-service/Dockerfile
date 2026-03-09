FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

COPY ./target/order-service-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8083

CMD ["java", "-jar", "app.jar"]
