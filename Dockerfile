# --- build stage ---
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY gradlew settings.gradle build.gradle gradle.properties ./
COPY gradle ./gradle
COPY src ./src
RUN chmod +x gradlew && ./gradlew build -x test --no-daemon

# --- runtime stage ---
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/quarkus-app/ ./
COPY docker-entrypoint.sh /app/docker-entrypoint.sh
# Strip any CRLF (script may be authored on Windows) and make executable.
RUN sed -i 's/\r$//' /app/docker-entrypoint.sh && chmod +x /app/docker-entrypoint.sh
EXPOSE 8080
# Entrypoint converts DATABASE_URL into Quarkus datasource vars, then starts the app.
ENTRYPOINT ["/app/docker-entrypoint.sh"]
