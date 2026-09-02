#!/usr/bin/env bash
set -e

# ==============================================================================
# QuickBite - Script Build và Push Image lên GitHub Container Registry (GHCR)
# ==============================================================================

# Cấu hình thông tin
GITHUB_USER="${GITHUB_USER:-ltvhcung001}"
# Chuyển đổi username sang chữ thường theo chuẩn bắt buộc của GHCR
GITHUB_USER_LOWER=$(echo "${GITHUB_USER}" | tr '[:upper:]' '[:lower:]')
IMAGE_NAME="payment-service"
VERSION="${VERSION:-1.0.0}"
IMAGE_TAG="ghcr.io/${GITHUB_USER_LOWER}/${IMAGE_NAME}:${VERSION}"
IMAGE_LATEST="ghcr.io/${GITHUB_USER_LOWER}/${IMAGE_NAME}:latest"

echo "=========================================================="
echo "  QuickBite: Đóng gói và Push lên GHCR"
echo "  Target Image: ${IMAGE_TAG}"
echo "=========================================================="

# Kiểm tra xác thực GHCR
if [ -z "${CR_PAT}" ]; then
  echo ""
  echo "[!] Biến môi trường CR_PAT (Personal Access Token) chưa được đặt."
  echo "    Vui lòng nhập Personal Access Token (PAT) có quyền 'write:packages':"
  read -s -p "Enter GitHub PAT: " CR_PAT
  echo ""
fi

echo ">> 1. Đăng nhập vào GitHub Container Registry (ghcr.io)..."
echo "${CR_PAT}" | docker login ghcr.io -u "${GITHUB_USER}" --password-stdin

echo ">> 2. Xây dựng Docker image cục bộ..."
docker build -t "${IMAGE_TAG}" -t "${IMAGE_LATEST}" -f payment-service/Dockerfile .

echo ">> 3. Đẩy image lên GitHub Container Registry..."
docker push "${IMAGE_TAG}"
docker push "${IMAGE_LATEST}"

echo "=========================================================="
echo " [THÀNH CÔNG] Image đã được đẩy lên GHCR!"
echo " URL Package: https://github.com/${GITHUB_USER}?tab=packages"
echo " Image URI:   ${IMAGE_TAG}"
echo "=========================================================="
