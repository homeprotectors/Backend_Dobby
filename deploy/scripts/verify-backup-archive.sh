#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

object_name="${1:?Usage: verify-backup-archive.sh database/YYYY/MM/DD/file.dump.enc}"
source /etc/homeprotectors/infra.env
: "${OCI_BACKUP_BUCKET:?}"
: "${OCI_BACKUP_NAMESPACE:?}"
: "${OCI_REGION:?}"

work_dir="$(mktemp -d)"
encrypted_file="$work_dir/backup.dump.enc"
dump_file="$work_dir/backup.dump"
cleanup() {
  rm -f -- "$encrypted_file" "$dump_file"
  rmdir -- "$work_dir"
}
trap cleanup EXIT

oci os object get \
  --auth instance_principal \
  --region "$OCI_REGION" \
  --namespace-name "$OCI_BACKUP_NAMESPACE" \
  --bucket-name "$OCI_BACKUP_BUCKET" \
  --name "$object_name" \
  --file "$encrypted_file" >/dev/null

openssl enc -d -aes-256-cbc -pbkdf2 -iter 200000 \
  -pass file:/srv/homeprotectors/secrets/backup-passphrase \
  -in "$encrypted_file" \
  -out "$dump_file"

toc="$(pg_restore --list "$dump_file")"
table_count="$(printf '%s\n' "$toc" | awk '$4 == "TABLE" && $5 != "DATA" {count++} END {print count+0}')"
schemas="$(printf '%s\n' "$toc" | awk '$4 == "TABLE" && $5 != "DATA" {print $5}' | sort -u | paste -sd ',' -)"
if (( table_count == 0 )); then
  echo "Backup archive has no tables." >&2
  exit 1
fi
echo "Backup archive decrypts successfully: $table_count tables in schemas: $schemas"
