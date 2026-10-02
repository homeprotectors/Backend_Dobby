#!/usr/bin/env bash
set -Eeuo pipefail

password_file="${SUPABASE_PASSWORD_FILE:-/srv/homeprotectors/secrets/supabase-db-password}"
if [[ ! -s "$password_file" ]]; then
  echo "Supabase database password file is missing." >&2
  exit 1
fi

PGPASSWORD="$(< "$password_file")" PGSSLMODE=require psql \
  --host=aws-0-ap-northeast-2.pooler.supabase.com \
  --port=5432 \
  --username=postgres.zulfdzwjmowupwwmrael \
  --dbname=postgres \
  -v ON_ERROR_STOP=1 \
  -P pager=off \
  <<'SQL'
SELECT count(*) AS public_tables FROM pg_tables WHERE schemaname = 'public';
SELECT extname, extnamespace::regnamespace AS extension_schema
FROM pg_extension
WHERE extname IN ('pgcrypto', 'uuid-ossp')
ORDER BY extname;
SQL
