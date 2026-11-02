FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /workspace
COPY . .
RUN chmod +x ./gradlew && ./gradlew --no-daemon bootJar

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
COPY --from=build /workspace/build/libs/ourspace-0.0.1-SNAPSHOT.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-Djava.io.tmpdir=/tmp", "-jar", "/app/app.jar"]
