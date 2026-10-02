#!/usr/bin/env bash
set -Eeuo pipefail

cd "${DEPLOY_DIR:-/srv/homeprotectors}"
export IMAGE_REPOSITORY=ghcr.io/homeprotectors/backend_dobby
export APP_VERSION=prod-local

docker compose -f compose.prod.yml -f compose.smoke.yml config --quiet
docker compose -f compose.prod.yml -f compose.smoke.yml up \
  --detach --no-deps --wait --wait-timeout 180 app
docker compose -f compose.prod.yml -f compose.smoke.yml exec -T app \
  curl --fail --silent http://localhost:8080/actuator/health
