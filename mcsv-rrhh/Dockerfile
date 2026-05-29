# Multi-stage build. Context must be the repo root (the reactor pom lives there).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /workspace
COPY . .
RUN mvn -B -ntp -pl mcsv-rrhh -am -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /workspace/mcsv-rrhh/target/*.jar app.jar
EXPOSE 9096
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
