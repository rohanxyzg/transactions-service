# ---- Build stage ----
# maven:3.9-eclipse-temurin-17 includes Maven pre-installed — no apt needed.
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
# Resolve all dependencies before copying source so this layer is cached
# as long as pom.xml hasn't changed. Source edits don't re-download deps.
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn -q -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre AS runtime
WORKDIR /app
COPY --from=builder /app/target/transactions-service-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
