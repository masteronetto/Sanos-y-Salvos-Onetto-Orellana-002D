FROM gradle:9.4.1-jdk21 AS builder
WORKDIR /project
COPY . /project
RUN gradle :services:geoservice:bootJar -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=builder /project/services/geoservice/build/libs/*.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
