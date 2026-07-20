FROM maven:3.9.15-eclipse-temurin-26-alpine@sha256:9b0bc1420b93f3eafb7401dc3030eac01e773d8d709686d3c4ac351fcf6f9f42 AS build

WORKDIR /workspace
COPY pom.xml .
RUN mvn --batch-mode --no-transfer-progress dependency:go-offline
COPY src ./src
RUN mvn --batch-mode --no-transfer-progress -Dmaven.test.skip=true -Djacoco.skip=true package

FROM eclipse-temurin:21.0.9_10-jre-alpine@sha256:08eecc477dbe3f2e33daac27f36e41daf7f4ec51d2f3396006e54fa41832c74c

RUN addgroup -S -g 10001 medisalud \
    && adduser -S -D -H -u 10001 -G medisalud medisalud

WORKDIR /app
COPY --from=build --chown=10001:10001 /workspace/target/medisalud-api-1.0.0.jar app.jar

USER 10001:10001

EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
    CMD wget -q -O /dev/null "http://127.0.0.1:${SERVER_PORT:-${PORT:-8080}}/actuator/health" || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
