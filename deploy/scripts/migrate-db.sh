#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

: "${SOURCE_DATABASE_URL:?SOURCE_DATABASE_URL must point to AWS RDS}"
: "${TARGET_DATABASE_URL:?TARGET_DATABASE_URL must point to Supabase and use TLS}"

if [[ "$TARGET_DATABASE_URL" != *"sslmode=require"* ]]; then
  echo "TARGET_DATABASE_URL must include sslmode=require." >&2
  exit 1
fi

if [[ "$(pg_dump --version)" != *"PostgreSQL) 17."* ]]; then
  echo "PostgreSQL 17 pg_dump is required for the RDS 17 source server." >&2
  exit 1
fi

output="${1:-production.dump}"

pg_dump \
  --format=custom \
  --no-owner \
  --no-privileges \
  --dbname="$SOURCE_DATABASE_URL" \
  --file="$output"

pg_restore \
  --exit-on-error \
  --no-owner \
  --no-privileges \
  --dbname="$TARGET_DATABASE_URL" \
  "$output"

echo "Restore completed. Run verify-db.sh before directing production traffic to Supabase."
