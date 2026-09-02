#!/usr/bin/env bash
set -e

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

echo "=== [QuickBite] Starting Self-Hosted GitHub Actions Runner with DooD ==="

# Kiểm tra docker socket tồn tại trên host
if [ ! -S /var/run/docker.sock ]; then
  echo "Error: /var/run/docker.sock not found. Please ensure Docker daemon is running."
  exit 1
fi

# Tự động lấy Docker GID từ host
HOST_DOCKER_GID=$(stat -c '%g' /var/run/docker.sock)
export DOCKER_GID=${HOST_DOCKER_GID}
echo "Detected Host Docker GID: ${DOCKER_GID}"

# Nạp file .env nếu có
if [ -f "${DIR}/.env.runner" ]; then
  echo "Loading configuration from .env.runner..."
  export $(grep -v '^#' "${DIR}/.env.runner" | xargs)
elif [ -f "${DIR}/runner/.env" ]; then
  echo "Loading configuration from runner/.env..."
  export $(grep -v '^#' "${DIR}/runner/.env" | xargs)
fi

if [ -z "${RUNNER_TOKEN}" ]; then
  echo "WARNING: RUNNER_TOKEN is not set!"
  echo "Please obtain a registration token from your GitHub repo:"
  echo "  Repo -> Settings -> Actions -> Runners -> New runner"
  echo "And set it using: export RUNNER_TOKEN='<TOKEN>' or create a .env.runner file."
  echo ""
  echo "Usage:"
  echo "  RUNNER_TOKEN=xxxx ./start-runner.sh"
  exit 1
fi

docker compose -f "${DIR}/docker-compose.runner.yml" up -d

echo "Runner container started successfully!"
echo "Check logs with: docker logs -f quickbite-github-runner"
