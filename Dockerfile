# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app


# Copy wrapper FIRST so we can run it
COPY .mvn .mvn
COPY mvnw mvnw
# Copy pom first for dependency cachin
COPY pom.xml .

# Ensure mvnw is executable (linux containers)
RUN chmod +x mvnw

# Download deps (cacheable layer)
RUN ./mvnw -q -DskipTests dependency:go-offline

# Copy source and build
COPY src src
RUN ./mvnw -q clean package -DskipTests

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# Create non-root user
RUN useradd -r -u 10001 -g root appuser

# Copy jar from build stage
COPY --from=build /app/target/FraudRuleEngine-0.0.1-SNAPSHOT.jar app.jar

# Run as non-root
USER 10001
# Expose app port (your app maps 8080 in container)
EXPOSE 8080


# Run
ENTRYPOINT ["java","-jar","/app/app.jar"]

# (Optional) JVM flags can be overridden at runtime
#ENV JAVA_OPTS=""




