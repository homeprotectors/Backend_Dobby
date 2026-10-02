#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

deploy_dir="${DEPLOY_DIR:-/srv/homeprotectors}"
legacy_vars="$deploy_dir/secrets/legacy-vars.env"
password_file="$deploy_dir/secrets/supabase-db-password"
output_file="$deploy_dir/.env"

if [[ -e "$output_file" ]]; then
  echo "Refusing to overwrite existing $output_file" >&2
  exit 1
fi
if [[ ! -s "$legacy_vars" || ! -s "$password_file" ]]; then
  echo "Legacy application settings or Supabase password file is missing." >&2
  exit 1
fi

{
  printf '%s\n' \
    'IMAGE_REPOSITORY=ghcr.io/homeprotectors/backend_dobby' \
    'APP_VERSION=prod-latest' \
    'GHCR_DEPLOY_USER=shayjung' \
    'APP_DOMAIN=api.dueit.date' \
    'ACME_EMAIL=sosat4425@gmail.com' \
    'SPRING_PROFILES_ACTIVE=prod' \
    'DATABASE_URL=jdbc:postgresql://aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require' \
    'DATABASE_USERNAME=postgres.zulfdzwjmowupwwmrael' \
    'BACKUP_DATABASE_URL=postgresql://postgres.zulfdzwjmowupwwmrael@aws-0-ap-northeast-2.pooler.supabase.com:5432/postgres?sslmode=require' \
    'APP_JWT_PRIVATE_PEM_PATH=/run/secrets/jwt_private' \
    'APP_JWT_PUBLIC_PEM_PATH=/run/secrets/jwt_public' \
    'APP_PUSH_FCM_CREDENTIALS_PATH=/run/secrets/firebase_service_account' \
    'CORS_ALLOWED_ORIGINS=https://dueit.date,https://www.dueit.date' \
    'APPLE_TEAM_ID=' \
    'APPLE_KEY_ID=' \
    'APPLE_CLIENT_ID=' \
    'APPLE_PRIVATE_KEY_P8='
  cat "$legacy_vars"
  printf 'DATABASE_PASSWORD=%s\n' "$(< "$password_file")"
} > "$output_file"

chmod 600 "$output_file"
echo "OCI application environment created."
