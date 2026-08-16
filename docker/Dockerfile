FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/distributed-cache-1.0.0.jar app.jar

EXPOSE 8080 8081 8082 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
