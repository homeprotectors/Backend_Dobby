#!/usr/bin/env bash
set -Eeuo pipefail

mode="${1:?Usage: table-counts.sh source|target}"
case "$mode" in
  source)
    container="${SOURCE_CONTAINER:-homeprotectors-app-1}"
    database_url=''
    database_user=''
    database_password=''
    while IFS= read -r entry; do
      case "$entry" in
        DATABASE_URL=*) database_url="${entry#*=}" ;;
        DATABASE_USERNAME=*) database_user="${entry#*=}" ;;
        DATABASE_PASSWORD=*) database_password="${entry#*=}" ;;
      esac
    done < <(docker inspect --format '{{range .Config.Env}}{{println .}}{{end}}' "$container")
    if [[ ! "$database_url" =~ ^jdbc:postgresql://([^/:?]+):([0-9]+)/([^?]+) ]]; then
      echo "Source JDBC URL is missing or unsupported." >&2
      exit 1
    fi
    database_host="${BASH_REMATCH[1]}"
    database_port="${BASH_REMATCH[2]}"
    database_name="${BASH_REMATCH[3]}"
    ;;
  target)
    password_file="${SUPABASE_PASSWORD_FILE:-/srv/homeprotectors/secrets/supabase-db-password}"
    database_password="$(< "$password_file")"
    database_host=aws-0-ap-northeast-2.pooler.supabase.com
    database_port=5432
    database_name=postgres
    database_user=postgres.zulfdzwjmowupwwmrael
    ;;
  *) echo "Usage: table-counts.sh source|target" >&2; exit 2 ;;
esac

if [[ -z "$database_user" || -z "$database_password" ]]; then
  echo "Database credentials are missing." >&2
  exit 1
fi

PGPASSWORD="$database_password" PGSSLMODE=require psql \
  --host="$database_host" \
  --port="$database_port" \
  --username="$database_user" \
  --dbname="$database_name" \
  -At -F '|' -v ON_ERROR_STOP=1 \
  <<'SQL'
SELECT format('SELECT %L, count(*) FROM %I.%I;', 'TABLE|' || schemaname || '.' || tablename, schemaname, tablename)
FROM pg_tables
WHERE schemaname = 'public'
ORDER BY tablename
\gexec
SELECT 'SEQUENCE|' || schemaname || '.' || sequencename, coalesce(last_value::text, 'NULL')
FROM pg_sequences
WHERE schemaname = 'public'
ORDER BY sequencename;
SQL
