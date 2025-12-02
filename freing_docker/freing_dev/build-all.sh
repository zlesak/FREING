#!/bin/sh
# Skript pro plně automatický build a spuštění všech backend služeb

set -e
cd "$(dirname "$0")"

# Tenhle build je tu proto, že všechny svc využívají stejný builder image, aby se nestahoval gradle a JDK pro každou službu zvlášť
# Zároveň má v sobě obsaženej common, takže ten se taky nebuildí pro každou zvlášť, ale jen jednou, a pokud se nezmění, tak se nebuildí
echo "[1/4] Build builder image"
docker compose build backend_builder

# Buildí se všechny služby najednou, aby se využil ten samej builder image
echo "[2/4] Build all backend services"
docker compose build service_customer service_invoice service_payment service_rendering

# Buildí pro fe
echo "[3/4] Build frontend"
docker compose build frontend

#Klasický docker compose up -d na spuštění všech služeb po buildnutí
echo "[4/4] Run docker compose up -d"
docker compose up -d

echo "All done, wait for the services to spin up"
