FROM gradle:8.14-jdk21 AS builder
WORKDIR /app
COPY . .
RUN gradle :spring:bootJar --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /app/spring/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
