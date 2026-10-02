#!/usr/bin/env bash
set -Eeuo pipefail

DEPLOY_DIR="${DEPLOY_DIR:-/srv/homeprotectors}"
cd "$DEPLOY_DIR"

: "${IMAGE_REPOSITORY:?IMAGE_REPOSITORY is required}"
: "${APP_VERSION:?APP_VERSION is required}"

if [[ ! -f .env ]]; then
  echo "Missing $DEPLOY_DIR/.env; copy env.example and populate secrets first." >&2
  exit 1
fi

required_secret_files=(
  secrets/jwt_private.pem
  secrets/jwt_public.pem
  secrets/firebase-service-account.json
  secrets/backup-passphrase
  secrets/cloudflare-tunnel-token
)

for secret_file in "${required_secret_files[@]}"; do
  if [[ ! -s "$secret_file" ]]; then
    echo "Missing or empty secret file: $DEPLOY_DIR/$secret_file" >&2
    exit 1
  fi
done

if ! grep -Eq '^DATABASE_URL=.*sslmode=require' .env; then
  echo "DATABASE_URL must use the Supabase pooler with sslmode=require." >&2
  exit 1
fi

export IMAGE_REPOSITORY APP_VERSION
docker compose -f compose.prod.yml config --quiet
if [[ "${DEPLOY_FROM_LOCAL:-false}" != true ]]; then
  docker compose -f compose.prod.yml pull
fi
docker compose -f compose.prod.yml up -d --remove-orphans --wait --wait-timeout 180

systemctl enable --now homeprotectors-backup.timer
docker compose -f compose.prod.yml exec -T app curl --fail --silent http://localhost:8080/actuator/health
if [[ "$APP_VERSION" == prod-latest ]]; then
  systemctl enable --now homeprotectors-update.timer
fi
