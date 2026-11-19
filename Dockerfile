FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml ./
COPY src/main/ ./src/main/
RUN --mount=type=cache,target=/root/.m2 mvn -B -Dmaven.test.skip=true package

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/pulsepass.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/pulsepass.jar"]

