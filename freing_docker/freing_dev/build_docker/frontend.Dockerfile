FROM node:20-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./

COPY freing_docker/freing_dev/build_docker/frontend-start.sh /app/frontend-start.sh
RUN chmod +x /app/frontend-start.sh

EXPOSE 4100

CMD ["/bin/sh", "/app/frontend-start.sh"]
