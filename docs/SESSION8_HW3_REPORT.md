# BÁO CÁO THỰC HÀNH BÀI 3: THIẾT LẬP QUY TRÌNH BUILD VÀ PUSH IMAGE LÊN GHCR

**Dự án:** QuickBite Microservices Platform (`payment-service`)  
**Khóa học / Module:** MD5 - DevOps & Microservices  
**Branch thực hiện:** `Session8-HW3`  
**Học viên:** `ltvhcung001`  
**Registry đích:** GitHub Container Registry (`ghcr.io`)  
**Package URL:** `https://github.com/ltvhcung001?tab=packages`  

---

## 1. MỤC TIÊU HỌC TẬP
- Làm chủ quy tắc đặt tên và tagging image chuẩn OCI để đẩy lên kho lưu trữ từ xa GitHub Container Registry (`ghcr.io`).
- Hiểu rõ cơ chế xác thực an toàn bằng Personal Access Token (PAT) thay thế mật khẩu tài khoản GitHub.
- Xử lý lỗi bảo mật `denied: requested access to the resource is denied` khi tương tác với GHCR qua Docker CLI.
- Tự động hóa quy trình đóng gói và xuất bản image qua luồng CI/CD GitHub Actions.

---

## 2. PHÂN TÍCH LỖI VÀ NGUYÊN LÝ BẢO MẬT

### 2.1. Thông báo lỗi thực tế
Khi người dùng chạy lệnh:
```bash
$ docker push ghcr.io/ltvhcung001/payment-service:1.0.0
```
Hệ thống Docker CLI phản hồi lỗi:
```bash
error from registry: denied
denied: requested access to the resource is denied
```

### 2.2. Nguyên nhân gốc rễ (Root Cause)
1. **Chính sách bảo mật của GitHub:**
   - GitHub không cho phép sử dụng mật khẩu đăng nhập tài khoản chính để xác thực với các dịch vụ Git CLI và Container Registry (`ghcr.io`) nhằm ngăn chặn rò rỉ thông tin đăng nhập và hỗ trợ bắt buộc tính năng xác thực hai yếu tố (2FA).
   - Khi Docker CLI chưa thực hiện lệnh `docker login ghcr.io` hoặc sử dụng thông tin đăng nhập không hợp lệ, GHCR từ chối truy cập với mã lỗi HTTP 403 / 401: `denied: requested access to the resource is denied`.

2. **Yêu cầu về quyền của Personal Access Token (PAT):**
   - Để đẩy một container image lên GHCR, tài khoản cần một token được phân quyền tường minh:
     - `write:packages`: Cho phép upload container images lên GitHub Packages.
     - `read:packages`: Cho phép download/pull images từ kho lưu trữ.
   - Nếu token không có quyền `write:packages`, máy chủ registry sẽ từ chối việc push dù token có hợp lệ cho các thao tác Git khác.

3. **Quy tắc đặt tên Namespace trên GHCR:**
   - Khác với Docker Hub, GHCR yêu cầu đường dẫn đầy đủ:
     `ghcr.io/<GITHUB_USERNAME_OR_ORG>/<IMAGE_NAME>:<TAG>`
   - **Đặc biệt lưu ý:** GitHub Container Registry bắt buộc toàn bộ tên người dùng (namespace) trong tag image phải được viết thường hoàn toàn (lowercase). Ví dụ: `ltvhcung001`, không được viết `Ltvhcung001`.

---

## 3. CÁC BƯỚC THỰC HIỆN CHI TIẾT

### Bước 1: Khởi tạo Personal Access Token (PAT)
1. Đăng nhập vào GitHub cá nhân (`https://github.com/ltvhcung001`).
2. Vào **Settings** (ở góc trên bên phải avatar) -> **Developer Settings** -> **Personal access tokens** -> Chọn **Tokens (classic)**.
3. Click **Generate new token (classic)**.
4. Đặt tên Token: `quickbite-ghcr-token`.
5. Đánh dấu tích vào các Scope:
   - [x] **`write:packages`** (Upload packages to GitHub Package Registry)
   - [x] **`read:packages`** (Tự động được tích kèm)
   - [x] **`delete:packages`** (Tùy chọn: Để quản lý vòng đời image)
6. Nhấn **Generate token** và sao chép chuỗi token (dạng `ghp_...`).

### Bước 2: Xác thực Docker CLI với GHCR
Lưu token vào biến môi trường và đăng nhập thông qua standard input (`--password-stdin` để tránh lưu token vào lịch sử bash):
```bash
export CR_PAT="ghp_YOUR_TOKEN_HERE"
echo $CR_PAT | docker login ghcr.io -u ltvhcung001 --password-stdin
```
*Kết quả thành công:*
```bash
Login Succeeded
```

### Bước 3: Đóng gói và Gắn Tag Image chuẩn GHCR
Sử dụng file [`payment-service/Dockerfile`](file:///home/cungh/Documents/rikkei/md5/ss7/payment-service/Dockerfile) đã được tối ưu để build image và gắn tag:
```bash
docker build -t ghcr.io/ltvhcung001/payment-service:1.0.0 \
             -t ghcr.io/ltvhcung001/payment-service:latest \
             -f payment-service/Dockerfile .
```

Kiểm tra image đã sẵn sàng trong Docker Engine:
```bash
$ docker images | grep ghcr.io
ghcr.io/ltvhcung001/payment-service   1.0.0    a56b722f33d4   449MB   160MB
ghcr.io/ltvhcung001/payment-service   latest   a56b722f33d4   449MB   160MB
```

### Bước 4: Đẩy (Push) Image lên GitHub Container Registry
Thực hiện push cả phiên bản `1.0.0` và nhãn `latest`:
```bash
docker push ghcr.io/ltvhcung001/payment-service:1.0.0
docker push ghcr.io/ltvhcung001/payment-service:latest
```

---

## 4. TỰ ĐỘNG HÓA VỚI GITHUB ACTIONS CI (`.github/workflows/ghcr-push.yml`)

Để đảm bảo tính tự động hóa và độ tin cậy chuẩn DevOps, một workflow chuyên trách được thiết lập tại [`.github/workflows/ghcr-push.yml`](file:///home/cungh/Documents/rikkei/md5/ss7/.github/workflows/ghcr-push.yml):
```yaml
name: Build and Push Payment Service to GHCR

on:
  push:
    branches:
      - Session8-HW3
  workflow_dispatch:

permissions:
  contents: read
  packages: write

jobs:
  build-and-push:
    name: Build & Push to GHCR
    runs-on: ubuntu-latest
    steps:
      - name: Checkout repository
        uses: actions/checkout@v4

      - name: Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}

      - name: Build and Tag Payment Service Image
        run: |
          OWNER=$(echo "${{ github.repository_owner }}" | tr '[:upper:]' '[:lower:]')
          IMAGE_TAG="ghcr.io/${OWNER}/payment-service:1.0.0"
          IMAGE_LATEST="ghcr.io/${OWNER}/payment-service:latest"
          
          echo "=== Building Image: ${IMAGE_TAG} ==="
          docker build -t "${IMAGE_TAG}" -t "${IMAGE_LATEST}" -f payment-service/Dockerfile .

      - name: Push Image to GHCR
        run: |
          OWNER=$(echo "${{ github.repository_owner }}" | tr '[:upper:]' '[:lower:]')
          IMAGE_TAG="ghcr.io/${OWNER}/payment-service:1.0.0"
          IMAGE_LATEST="ghcr.io/${OWNER}/payment-service:latest"
          
          echo "=== Pushing Image: ${IMAGE_TAG} ==="
          docker push "${IMAGE_TAG}"
          docker push "${IMAGE_LATEST}"
```

> **Điểm nổi bật của CI Pipeline:**
> - Sử dụng trực tiếp `secrets.GITHUB_TOKEN` kèm quyền `packages: write`, không cần tạo thêm PAT thủ công trong CI.
> - Tự động chuẩn hóa username thành chữ thường bằng `tr '[:upper:]' '[:lower:]'`.
> - Tự động kích hoạt khi push code lên branch `Session8-HW3`.

---

## 5. KẾT QUẢ ĐẠT ĐƯỢC & ĐƯỜNG DẪN KIỂM CHỨNG

1. **Trang Packages GitHub:**
   - Truy cập: `https://github.com/ltvhcung001?tab=packages`
   - Image xuất hiện tại mục Packages với tên: `payment-service` và tag phiên bản `1.0.0`.
2. **Lệnh pull kiểm chứng từ xa:**
   ```bash
   docker pull ghcr.io/ltvhcung001/payment-service:1.0.0
   ```
