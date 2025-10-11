FROM node:20-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./

EXPOSE 4100

CMD ["npm", "run", "start:docker"]