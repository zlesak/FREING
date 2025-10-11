# Multi-stage Dockerfile for building Kotlin/Spring Boot services using Gradle wrapper
# Build stage: use local builder base from backend-builder.Dockerfile (inlined)
FROM amazoncorretto:21 AS builder

# Keep running as root so downstream builds can change file permissions and run gradle safely
USER root
# Metadata
ENV FREING_BUILDER_IMAGE=1
LABEL maintainer="FREING"

RUN yum -y install curl && yum clean all

# Copy repository backend sources into builder
COPY backend/ /home/gradle/project/backend/
# We'll build a specific subproject by passing SERVICE_DIR build-arg
ARG SERVICE_DIR=customer-service
WORKDIR /home/gradle/project/backend/${SERVICE_DIR}

# Ensure wrapper is executable
RUN if [ -f ./gradlew ]; then chmod +x ./gradlew; fi

# Build the application (assemble jar)
# Use --no-daemon to avoid background daemons in containers
RUN ./gradlew clean bootJar --no-daemon -x test || ./gradlew clean assemble --no-daemon -x test

# Runtime stage: use shared runtime base image
FROM builder
ARG SERVICE_DIR=customer-service
WORKDIR /app

# Copy the built jar from builder stage
COPY --from=builder /home/gradle/project/backend/${SERVICE_DIR}/build/libs/ /app/

# Copy start script and make executable (start.sh moved into build/)
COPY freing_docker/freing_dev/build_docker/start.sh /app/start.sh
RUN chmod +x /app/start.sh

ENV JAVA_OPTS="-Xms256m -Xmx512m"
ENV SERVER_PORT=8080

EXPOSE ${SERVER_PORT}

HEALTHCHECK --interval=15s --timeout=4s --retries=6 --start-period=60s \
  CMD sh -c 'P="${SERVER_PORT:-8080}"; \
    BODY=$(curl -sf http://127.0.0.1:$P/actuator/health 2>/dev/null || true); \
    echo "$BODY" | grep -q "\"status\":\"UP\"" || exit 1'

# Use start.sh as entrypoint (JSON form is recommended)
ENTRYPOINT ["/bin/sh", "/app/start.sh"]
