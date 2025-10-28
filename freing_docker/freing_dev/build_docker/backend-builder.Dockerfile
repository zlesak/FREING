# backend-builder.Dockerfile
# Společný builder pro všechny svc
FROM amazoncorretto:21 AS builder

USER root
ENV FREING_BUILDER_IMAGE=1
LABEL maintainer="FREING"

RUN yum -y install curl && yum clean all

COPY backend/gradlew /home/gradle/project/backend/gradlew
COPY backend/gradlew.bat /home/gradle/project/backend/gradlew.bat
COPY backend/gradle/wrapper /home/gradle/project/backend/gradle/wrapper
COPY backend/build.gradle.kts /home/gradle/project/backend/build.gradle.kts
COPY backend/settings.gradle.kts /home/gradle/project/backend/settings.gradle.kts
COPY backend/common/build.gradle.kts /home/gradle/project/backend/common/build.gradle.kts
COPY backend/common/src /home/gradle/project/backend/common/src

WORKDIR /home/gradle/project/backend/

RUN chmod +x ./gradlew

RUN ./gradlew :common:jar --no-daemon -x test

# Zbytek kódu se bude kopírovat až ve service-specific Dockerfilu pro build jednotlivých služeb
