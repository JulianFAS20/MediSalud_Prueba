FROM eclipse-temurin:21.0.9_10-jre-alpine@sha256:08eecc477dbe3f2e33daac27f36e41daf7f4ec51d2f3396006e54fa41832c74c

RUN addgroup -S -g 10001 medisalud \
    && adduser -S -D -H -u 10001 -G medisalud medisalud

WORKDIR /app
COPY --chown=10001:10001 target/medisalud-api-1.0.0.jar app.jar

USER 10001:10001

EXPOSE 8080
HEALTHCHECK --interval=10s --timeout=3s --start-period=30s --retries=5 \
    CMD wget -q -O /dev/null http://127.0.0.1:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
