#!/bin/sh
set -e

# Seznam endpointů, na které čekat
ENDPOINTS="\
  http://service-customer:8081/api/customers/v3/api-docs\
  http://service-invoice:8082/api/invoices/v3/api-docs\
  http://service-payment:8083/api/payments/v3/api-docs\
  http://service-rendering:8084/api/rendering/v3/api-docs\
"

MAX_RETRIES=30
SLEEP=5

for url in $ENDPOINTS; do
  echo "Waiting for $url ..."
  i=1
  while [ $i -le $MAX_RETRIES ]; do
    if curl -sf "$url" > /dev/null; then
      echo "$url is available."
      break
    fi
    echo "  Attempt $i/$MAX_RETRIES failed, retrying in $SLEEP seconds..."
    i=$((i+1))
    sleep $SLEEP
  done
  if [ $i -gt $MAX_RETRIES ]; then
    echo "ERROR: $url not available after $MAX_RETRIES attempts."
    exit 1
  fi
done

echo "All OpenAPI endpoints are available. Starting frontend..."

exec npm run start:docker

