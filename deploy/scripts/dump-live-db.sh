#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

# Run on the AWS app host. The official PostgreSQL 17 container supplies a
# pg_dump version that matches the RDS major version without changing the host.
container="${1:-homeprotectors-app-1}"
output="${2:-/home/ubuntu/homeprotectors-precutover.dump}"
database_url=''
database_user=''
database_password=''

if [[ -e "$output" ]]; then
  echo "Refusing to overwrite existing dump: $output" >&2
  exit 1
fi

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
export PGPASSWORD="$database_password" PGSSLMODE=require

docker run --rm --network host \
  -e PGPASSWORD -e PGSSLMODE \
  postgres:17 pg_dump \
  --host="$database_host" \
  --port="$database_port" \
  --username="$database_user" \
  --dbname="$database_name" \
  --format=custom \
  --no-owner \
  --no-privileges \
  --schema=public \
  > "$output"

echo "Dump created: $output ($(stat -c %s "$output") bytes)"
