FROM node:20-alpine
WORKDIR /app

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ ./

EXPOSE 4100

CMD ["npm", "run", "start:docker"]