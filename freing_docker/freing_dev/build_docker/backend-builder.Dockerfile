# backend-builder.Dockerfile
# Společný builder pro všechny svc
FROM gradle:8.7.0-jdk21 AS builder

USER root
ENV FREING_BUILDER_IMAGE=1
LABEL maintainer="FREING"

COPY backend/common /home/gradle/project/backend/common

WORKDIR /home/gradle/project/backend/common/

RUN gradle jar --no-daemon -x test

# Zbytek kódu se bude kopírovat až ve service-specific Dockerfilu pro build jednotlivých služeb
