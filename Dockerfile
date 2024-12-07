FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

FROM openjdk:21-jdk-slim
VOLUME /tmp
COPY --from=build /app/target/recarga-pay-ms-1.0-SNAPSHOT.jar recarga-pay-ms.jar
ENTRYPOINT ["java","-jar","/recarga-pay-ms.jar"]