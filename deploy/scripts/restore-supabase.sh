#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

replace=false
if [[ "${1:-}" == "--replace" ]]; then
  replace=true
  shift
fi
if (( $# > 1 )); then
  echo "Usage: restore-supabase.sh [--replace] [dump-file]" >&2
  exit 2
fi

dump="${1:-/srv/homeprotectors/secrets/precutover.dump}"
password_file="${SUPABASE_PASSWORD_FILE:-/srv/homeprotectors/secrets/supabase-db-password}"
if [[ ! -s "$dump" || ! -s "$password_file" ]]; then
  echo "Database dump or Supabase password file is missing." >&2
  exit 1
fi

export PGPASSWORD="$(< "$password_file")" PGSSLMODE=require
connection=(
  --host=aws-0-ap-northeast-2.pooler.supabase.com
  --port=5432
  --username=postgres.zulfdzwjmowupwwmrael
  --dbname=postgres
)

target_tables="$(psql "${connection[@]}" -At -v ON_ERROR_STOP=1 \
  -c "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename")"
dump_tables="$(pg_restore --list "$dump" | awk '$4 == "TABLE" && $5 == "public" {print $6}' | sort)"
if [[ -z "$dump_tables" ]]; then
  echo "Refusing to restore: dump has no public tables." >&2
  exit 1
fi
if [[ -n "$target_tables" ]]; then
  if [[ "$replace" != true ]]; then
    echo "Refusing to restore: Supabase public schema is not empty. Use --replace only during a planned write freeze." >&2
    exit 1
  fi
  if [[ "$target_tables" != "$dump_tables" ]]; then
    echo "Refusing to replace: target public tables differ from the dump." >&2
    exit 1
  fi
fi

restore_list="$(mktemp)"
trap 'rm -f -- "$restore_list"' EXIT
pg_restore --list "$dump" | awk \
  '!/^[0-9]+; [0-9]+ [0-9]+ SCHEMA - public / && !/^[0-9]+; [0-9]+ [0-9]+ COMMENT - SCHEMA public /' \
  > "$restore_list"

restore_options=(--single-transaction)
if [[ "$replace" == true ]]; then
  restore_options+=(--clean --if-exists)
fi

pg_restore \
  --exit-on-error \
  --no-owner \
  --no-privileges \
  "${restore_options[@]}" \
  --use-list="$restore_list" \
  "${connection[@]}" \
  "$dump"

echo "Supabase public schema restored. Verify row counts before cutover."
