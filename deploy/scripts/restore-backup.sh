#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

: "${RESTORE_DATABASE_URL:?RESTORE_DATABASE_URL must point to a disposable PostgreSQL database}"
: "${OCI_BACKUP_BUCKET:?OCI_BACKUP_BUCKET is required}"
: "${OCI_BACKUP_NAMESPACE:?OCI_BACKUP_NAMESPACE is required}"
: "${OCI_REGION:?OCI_REGION is required}"
: "${OBJECT_NAME:?OBJECT_NAME is the database/...dump.enc object to restore}"

DEPLOY_DIR="${DEPLOY_DIR:-/srv/homeprotectors}"
PASSPHRASE_FILE="${BACKUP_PASSPHRASE_FILE:-$DEPLOY_DIR/secrets/backup-passphrase}"
OCI_CLI_AUTH="${OCI_CLI_AUTH:-instance_principal}"
work_dir="$(mktemp -d)"

if [[ "$(pg_restore --version)" != *"PostgreSQL) 17."* ]]; then
  echo "PostgreSQL 17 pg_restore is required for this restore." >&2
  exit 1
fi

cleanup() {
  rm -rf -- "$work_dir"
}
trap cleanup EXIT

encrypted_file="$work_dir/backup.dump.enc"
dump_file="$work_dir/backup.dump"

oci os object get \
  --auth "$OCI_CLI_AUTH" \
  --region "$OCI_REGION" \
  --namespace-name "$OCI_BACKUP_NAMESPACE" \
  --bucket-name "$OCI_BACKUP_BUCKET" \
  --name "$OBJECT_NAME" \
  --file "$encrypted_file"

openssl enc -d -aes-256-cbc -pbkdf2 -iter 200000 \
  -pass "file:$PASSPHRASE_FILE" \
  -in "$encrypted_file" \
  -out "$dump_file"

pg_restore \
  --exit-on-error \
  --no-owner \
  --no-privileges \
  --dbname="$RESTORE_DATABASE_URL" \
  "$dump_file"

echo "Backup restored successfully into the disposable restore-test database."
