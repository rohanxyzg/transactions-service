# Jar is built locally by run.sh before this runs.
# Docker's only job here is to package it into a runtime image.
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/transactions-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
