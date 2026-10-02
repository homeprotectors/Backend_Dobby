#!/usr/bin/env bash
set -Eeuo pipefail

: "${SOURCE_DATABASE_URL:?SOURCE_DATABASE_URL must point to AWS RDS}"
: "${TARGET_DATABASE_URL:?TARGET_DATABASE_URL must point to Supabase}"

table_query="SELECT quote_ident(table_schema) || '.' || quote_ident(table_name) FROM information_schema.tables WHERE table_schema = 'public' AND table_type = 'BASE TABLE' ORDER BY table_name"
mapfile -t tables < <(psql "$SOURCE_DATABASE_URL" -At -v ON_ERROR_STOP=1 -c "$table_query")

failed=0
printf '%-48s %12s %12s\n' "table" "source" "target"
for table in "${tables[@]}"; do
  source_count="$(psql "$SOURCE_DATABASE_URL" -At -v ON_ERROR_STOP=1 -c "SELECT count(*) FROM $table")"
  target_count="$(psql "$TARGET_DATABASE_URL" -At -v ON_ERROR_STOP=1 -c "SELECT count(*) FROM $table")"
  printf '%-48s %12s %12s\n' "$table" "$source_count" "$target_count"
  if [[ "$source_count" != "$target_count" ]]; then
    failed=1
  fi
done

if ((failed)); then
  echo "Row-count verification failed." >&2
  exit 1
fi

sequence_query="SELECT schemaname || '|' || sequencename || '|' || coalesce(last_value::text, 'NULL') FROM pg_sequences WHERE schemaname = 'public' ORDER BY sequencename"
source_sequences="$(psql "$SOURCE_DATABASE_URL" -At -v ON_ERROR_STOP=1 -c "$sequence_query")"
target_sequences="$(psql "$TARGET_DATABASE_URL" -At -v ON_ERROR_STOP=1 -c "$sequence_query")"

if [[ "$source_sequences" != "$target_sequences" ]]; then
  echo "Sequence verification failed. Source values:" >&2
  printf '%s\n' "$source_sequences" >&2
  echo "Target values:" >&2
  printf '%s\n' "$target_sequences" >&2
  exit 1
fi

echo "All public table row counts and sequence values match. Verify application workflows next."
