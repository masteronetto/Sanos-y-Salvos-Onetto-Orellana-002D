FROM gradle:9.4.1-jdk21 AS builder
WORKDIR /project
COPY . /project
RUN gradle :services:pet-service:clean :services:pet-service:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /project/services/pet-service/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
