#!/usr/bin/env bash
set -Eeuo pipefail

: "${DATABASE_URL:?DATABASE_URL must be a libpq URL for the database to inspect}"

psql "$DATABASE_URL" -v ON_ERROR_STOP=1 -P pager=off <<'SQL'
SELECT version() AS postgres_version;
SELECT pg_size_pretty(pg_database_size(current_database())) AS database_size;

SELECT extname, extversion
FROM pg_extension
ORDER BY extname;

SELECT table_schema, count(*) AS table_count
FROM information_schema.tables
WHERE table_type = 'BASE TABLE'
GROUP BY table_schema
ORDER BY table_schema;

SELECT schemaname, sequencename, last_value
FROM pg_sequences
WHERE schemaname = 'public'
ORDER BY sequencename;

SELECT count(*) AS active_connections
FROM pg_stat_activity
WHERE datname = current_database();
SQL
