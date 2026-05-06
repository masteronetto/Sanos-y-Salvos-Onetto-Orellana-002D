FROM gradle:9.4.1-jdk21 AS builder
WORKDIR /project
COPY . /project
RUN gradle :apps:bff-service:clean :apps:bff-service:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /project/apps/bff-service/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
