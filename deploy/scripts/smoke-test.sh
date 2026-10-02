#!/usr/bin/env bash
set -Eeuo pipefail

BASE_URL="${1:-${SMOKE_TEST_BASE_URL:-}}"
if [[ -z "$BASE_URL" ]]; then
  echo "Usage: $0 https://api.example.com" >&2
  exit 2
fi

HEALTH_URL="${BASE_URL%/}/actuator/health"
MAX_ATTEMPTS="${SMOKE_TEST_ATTEMPTS:-18}"

for ((attempt = 1; attempt <= MAX_ATTEMPTS; attempt++)); do
  response="$(curl --fail --silent --show-error --max-time 10 "$HEALTH_URL" 2>/dev/null || true)"
  if [[ "$response" == *'"status":"UP"'* ]]; then
    echo "Smoke test passed: $HEALTH_URL"
    exit 0
  fi
  sleep 5
done

echo "Smoke test failed after $MAX_ATTEMPTS attempts: $HEALTH_URL" >&2
exit 1
