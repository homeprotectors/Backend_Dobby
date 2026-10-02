#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

source_file="${1:-/srv/homeprotectors/.env}"
output_file="${2:-/home/ubuntu/dueit-oci-vars.env}"
if [[ -e "$output_file" ]]; then
  echo "Refusing to overwrite existing export: $output_file" >&2
  exit 1
fi

# Carry application identity and push settings over, without copying AWS
# database credentials or the old image tag.
grep -E '^(APP_JWT_ISS|APP_JWT_AUD|APP_PUSH_ENABLED|APP_PUSH_FCM_PROJECT_ID|APP_PUSH_MANUAL_DISPATCH_ENABLED|APP_PUSH_MANUAL_DISPATCH_SECRET)=' \
  "$source_file" > "$output_file"

echo "Selected application settings exported without database credentials."
