#!/usr/bin/env bash
set -Eeuo pipefail

DEPLOY_DIR="${DEPLOY_DIR:-/srv/homeprotectors}"
cd "$DEPLOY_DIR"

if [[ ! -s .env || ! -s secrets/ghcr-deploy-token ]]; then
  echo "Deployment environment or GHCR read token is missing." >&2
  exit 1
fi

deploy_user="$(sed -n 's/^GHCR_DEPLOY_USER=//p' .env | tail -n 1)"
image_repository="$(sed -n 's/^IMAGE_REPOSITORY=//p' .env | tail -n 1)"
: "${deploy_user:?Set GHCR_DEPLOY_USER in .env}"
: "${image_repository:?Set IMAGE_REPOSITORY in .env}"

docker login ghcr.io --username "$deploy_user" --password-stdin < secrets/ghcr-deploy-token >/dev/null
docker pull "${image_repository}:prod-latest" >/dev/null

latest_image_id="$(docker image inspect --format '{{.Id}}' "${image_repository}:prod-latest")"
running_image_id="$(docker inspect --format '{{.Image}}' homeprotectors-app-1 2>/dev/null || true)"
if [[ "$latest_image_id" == "$running_image_id" ]]; then
  exit 0
fi

IMAGE_REPOSITORY="$image_repository" APP_VERSION=prod-latest "$DEPLOY_DIR/scripts/deploy.sh"
