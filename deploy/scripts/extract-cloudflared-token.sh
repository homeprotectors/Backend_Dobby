#!/usr/bin/env bash
set -Eeuo pipefail
umask 077

source_unit="${1:?Usage: extract-cloudflared-token.sh copied-service-file [output-file]}"
output_file="${2:-/srv/homeprotectors/secrets/cloudflare-tunnel-token}"
if [[ ! -s "$source_unit" || -e "$output_file" ]]; then
  echo "Source service file is missing or output already exists." >&2
  exit 1
fi

token="$(sed -nE 's/^ExecStart=.*[[:space:]]--token[[:space:]]+([^[:space:]]+).*$/\1/p' "$source_unit")"
if [[ "$token" != eyJ* || ${#token} -lt 100 ]]; then
  echo "Existing Cloudflare tunnel token was not found in the service file." >&2
  exit 1
fi

printf '%s\n' "$token" > "$output_file"
chmod 400 "$output_file"
echo "Existing Cloudflare tunnel token installed without displaying it."
