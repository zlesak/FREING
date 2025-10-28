# Multi-stage Dockerfile, nejdříve build a pak se do nového fetchne jen výsledný jar
# 1. stage: builder (použije backend-builder.Dockerfile)
FROM freing-backend-builder:latest AS builder

# Kopíruje pouze zdrojáky konkrétní svc dle poskytnutého ARG nebo defaultně customer-service
ARG SERVICE_DIR=customer-service
COPY backend/${SERVICE_DIR}/ /home/gradle/project/backend/${SERVICE_DIR}/

WORKDIR /home/gradle/project/backend/${SERVICE_DIR}

# Build pouze konkrétní službu, common už je zbuilděný a v cache
RUN if [ -f ../gradlew ]; then chmod +x ../gradlew; fi
RUN ../gradlew :${SERVICE_DIR}:bootJar --no-daemon -x test || ../gradlew :${SERVICE_DIR}:assemble --no-daemon -x test

# 2. stage: runtime
FROM amazoncorretto:21
ARG SERVICE_DIR=customer-service
WORKDIR /app

# Zkopíruje jar z builderu z předchozí stage
COPY --from=builder /home/gradle/project/backend/${SERVICE_DIR}/build/libs/ /app/

# Start script kopiován z freing_docker
COPY freing_docker/freing_dev/build_docker/start.sh /app/start.sh
RUN chmod +x /app/start.sh

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SERVER_PORT=8080

EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=15s --timeout=4s --retries=6 --start-period=60s \
  CMD sh -c 'P="${SERVER_PORT:-8080}"; \
    BODY=$(curl -sf http://127.0.0.1:$P/actuator/health 2>/dev/null || true); \
    echo "$BODY" | grep -q "\"status\":\"UP\"" || exit 1'

ENTRYPOINT ["/bin/sh", "/app/start.sh"]
