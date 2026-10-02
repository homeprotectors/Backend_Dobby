#!/usr/bin/env bash
set -Eeuo pipefail

# Inspect the database used by the existing application without printing its
# connection credentials. Run this on the current host before migration.
container="${1:-homeprotectors-app-1}"
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

if [[ -z "$database_url" || -z "$database_user" || -z "$database_password" ]]; then
  echo "The application container is missing a database connection setting." >&2
  exit 1
fi

if [[ ! "$database_url" =~ ^jdbc:postgresql://([^/:?]+):([0-9]+)/([^?]+) ]]; then
  echo "The application database URL is not a supported PostgreSQL JDBC URL." >&2
  exit 1
fi

database_host="${BASH_REMATCH[1]}"
database_port="${BASH_REMATCH[2]}"
database_name="${BASH_REMATCH[3]}"

PGPASSWORD="$database_password" PGSSLMODE=require psql \
  --host="$database_host" \
  --port="$database_port" \
  --username="$database_user" \
  --dbname="$database_name" \
  -v ON_ERROR_STOP=1 \
  -P pager=off \
  <<'SQL'
SELECT current_setting('server_version') AS postgres_version,
       pg_size_pretty(pg_database_size(current_database())) AS database_size,
       pg_database_size(current_database()) AS database_size_bytes;
SELECT extname, extversion FROM pg_extension ORDER BY extname;
SQL
