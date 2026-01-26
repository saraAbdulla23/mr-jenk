# Use a maintained Temurin base image with an up-to-date Ubuntu (Jammy) rootfs
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app
COPY ./target/media-service-0.0.1-SNAPSHOT.jar /app
EXPOSE 8300
CMD ["java", "-jar", "media-service-0.0.1-SNAPSHOT.jar"]