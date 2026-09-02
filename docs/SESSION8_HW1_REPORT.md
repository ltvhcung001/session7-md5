# BÁO CÁO THỰC HÀNH BÀI 1: SỬA LỖI CẤU HÌNH CHIA SẺ DOCKER SOCKET (DooD)

**Dự án:** QuickBite Microservices Platform  
**Khóa học / Module:** MD5 - DevOps & Microservices  
**Branch thực hiện:** `Session8-HW1`  
**Học viên:** `ltvhcung001`  

---

## 1. MỤC TIÊU HỌC TẬP
- Nhận diện và làm chủ cấu hình chia sẻ Docker Socket trong môi trường CI/CD containerized.
- Phân tích sâu log lỗi Docker daemon để hiểu bản chất của mô hình **Docker-outside-of-Docker (DooD)**.
- Hiểu kiến trúc Client - Daemon của Docker và ánh xạ chính xác volume socket vật lý `/var/run/docker.sock`.
- Xử lý vấn đề phân quyền truy cập socket (File Permissions, User, GID của nhóm `docker`).

---

## 2. PHÂN TÍCH NGUYÊN NHÂN LỖI TRONG BỐI CẢNH TÌNH HUỐNG

### 2.1. Thông báo lỗi thực tế
Khi một Self-hosted Runner được triển khai bên trong một Docker container thực hiện lệnh `docker info` hoặc `docker build`, hệ thống báo lỗi:
```bash
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```

### 2.2. Phân tích bản chất kỹ thuật
1. **Kiến trúc Client - Server của Docker Engine:**
   - Docker Engine hoạt động theo mô hình Client - Server tách biệt:
     - **Docker CLI (`docker`)**: Phía Client, nhận lệnh từ người dùng và gửi API request.
     - **Docker Daemon (`dockerd`)**: Phía Server, chịu trách nhiệm quản lý images, containers, networks, volumes và lắng nghe API calls.
   - Theo mặc định trên hệ điều hành Linux, Docker CLI giao tiếp với Docker Daemon thông qua một UNIX domain socket vật lý tại đường dẫn:
     `/var/run/docker.sock`

2. **Cơ chế cô lập của Container Runner:**
   - Khi Self-hosted Runner được đóng gói và chạy bên trong một container Docker, nó được cô lập (isolated) thông qua cơ chế Linux Namespaces (Mount, PID, IPC, Network, UTS) và Cgroups.
   - Bên trong container của Runner chỉ được cài đặt sẵn công cụ **Docker CLI** nhằm thực hiện các câu lệnh `docker build`, `docker run`, `docker info`. Bản thân bên trong container **không có Docker Daemon** đang chạy.
   - Nếu file socket `/var/run/docker.sock` từ máy chủ vật lý (host) **chưa được ánh xạ (mount volume)** vào bên trong container Runner, Docker CLI bên trong container khi cố gắng mở kết nối tới đường dẫn mặc định `unix:///var/run/docker.sock` sẽ nhận thấy file socket này không tồn tại hoặc không có tiến trình nào lắng nghe.
   - Kết quả: Docker CLI ngay lập tức ném ra ngoại lệ:
     `Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?`

---

## 3. BẢN CHẤT MÔ HÌNH DOCKER-OUTSIDE-OF-DOCKER (DooD) VS DOCKER-IN-DOCKER (DinD)

| Tiêu chí | Docker-outside-of-Docker (DooD) | Docker-in-Docker (DinD) |
| :--- | :--- | :--- |
| **Cơ chế hoạt động** | Mount socket `/var/run/docker.sock` từ máy host vào container Runner. Docker CLI trong Runner điều khiển trực tiếp daemon ngoài host. | Chạy một Docker Daemon hoàn toàn độc lập (`dockerd`) bên trong container Runner. |
| **Loại container sinh ra** | Tạo ra các **Sibling Containers** (container anh em chạy cùng cấp độ trên máy host). | Tạo ra các **Child Containers** (container con nằm lồng bên trong container Runner). |
| **Yêu cầu bảo mật** | Không cần cờ `--privileged`. Chỉ cần cấp quyền đọc/ghi vào file socket. | Bắt buộc phải chạy container ở chế độ đặc quyền cao nhất `--privileged`. |
| **Hiệu năng & Tài nguyên** | **Cực nhanh và nhẹ**: Dùng chung Docker Engine, CPU/RAM tối ưu, tái sử dụng toàn bộ Docker cache của máy host. | Tốn nhiều RAM/CPU vì chạy 2 daemon song song; cache bị xóa sạch khi container runner tắt. |
| **Hệ thống tệp (Filesystem)** | Sử dụng trực tiếp Storage Driver của host (`overlay2`), ổn định, không lỗi lồng FS. | Xảy ra tình trạng `overlay-on-overlay` dễ dẫn tới lỗi filesystem crash hoặc suy giảm hiệu năng IO. |
| **Đánh giá ứng dụng** | **Chuẩn mực tối ưu cho Self-hosted Runner trong CI/CD.** | Phù hợp khi cần kiểm thử chính Docker daemon hoặc môi trường đa tenant độc lập. |

---

## 4. GIẢI PHÁP VÀ CẤU HÌNH CHI TIẾT

### 4.1. Cấu hình Mount Volume trong `docker-compose.runner.yml`
Để khắc phục lỗi kết nối, ta thiết lập bind mount volume chia sẻ chính xác socket từ máy host vào bên trong container Runner:

```yaml
version: '3.8'

services:
  github-runner:
    image: myoung34/github-runner:latest
    container_name: quickbite-github-runner
    restart: unless-stopped
    environment:
      REPO_URL: ${REPO_URL:-https://github.com/ltvhcung001/session7-md5}
      RUNNER_TOKEN: ${RUNNER_TOKEN}
      RUNNER_NAME: ${RUNNER_NAME:-quickbite-linux-runner}
      RUNNER_LABELS: ${RUNNER_LABELS:-self-hosted,linux,x64}
      DOCKER_GID: ${DOCKER_GID:-973}
    volumes:
      # =========================================================================
      # [DooD Solution] Chia sẻ Docker Socket vật lý từ máy host vào trong container Runner
      # =========================================================================
      - /var/run/docker.sock:/var/run/docker.sock
      - runner-work-data:/_work

volumes:
  runner-work-data:
```

### 4.2. Xử lý phân quyền Socket (File Permissions & GID)
- Trên hệ điều hành Linux (Ubuntu), file `/var/run/docker.sock` có quyền hạn mặc định:
  ```bash
  $ ls -la /var/run/docker.sock
  srw-rw---- 1 root docker 0 Sep  3 02:28 /var/run/docker.sock
  ```
- File này thuộc sở hữu của user `root` và group `docker`, chỉ cho phép user thuộc group `docker` đọc và ghi.
- Để tránh lỗi `permission denied` khi user `runner` (không phải root) trong container truy cập socket:
  1. Kiểm tra GID của nhóm `docker` trên host:
     ```bash
     $ getent group docker | cut -d: -f3
     973
     ```
  2. Truyền biến môi trường `DOCKER_GID=973` vào container Runner. Image `myoung34/github-runner` tự động cập nhật group ID bên trong container cho khớp với GID của host, đảm bảo runner có toàn quyền tương tác với socket an toàn mà không cần `chmod 777 /var/run/docker.sock`.

---

## 5. THỰC NGHIỆM VÀ BẰNG CHỨNG KIỂM CHỨNG

### 5.1. Tái hiện lỗi khi chưa mount Docker Socket
Chạy container Runner và gọi lệnh `docker info` khi chưa mount `/var/run/docker.sock`:
```bash
$ docker run --rm --entrypoint docker myoung34/github-runner:latest info
Client: Docker Engine - Community
 Version:    28.1.1
 Context:    default
 Debug Mode: false

Server:
Cannot connect to the Docker daemon at unix:///var/run/docker.sock. Is the docker daemon running?
```
*(Lỗi hoàn toàn trùng khớp 100% với đề bài)*

### 5.2. Kiểm chứng thành công khi đã cấu hình DooD Mount
Chạy container Runner kèm tham số mount volume `-v /var/run/docker.sock:/var/run/docker.sock`:
```bash
$ docker run --rm --entrypoint docker -v /var/run/docker.sock:/var/run/docker.sock myoung34/github-runner:latest info
Client: Docker Engine - Community
 Version:    28.1.1
 Context:    default

Server:
 Containers: 2
  Running: 1
  Paused: 0
  Stopped: 1
 Images: 13
 Server Version: 29.7.2
 Storage Driver: overlayfs
 Operating System: Ubuntu 26.04.1 LTS
```
Kết quả: Docker CLI bên trong container kết nối mượt mà tới Docker Daemon 29.7.2 trên host!

### 5.3. Kiểm chứng lệnh `docker build` thực thi thông suốt từ trong Runner
Thực hiện build một image từ bên trong runner container qua Docker socket của host:
```bash
$ docker run --rm -v /var/run/docker.sock:/var/run/docker.sock -v $(pwd):/workspace -w /workspace --entrypoint docker myoung34/github-runner:latest build -t test-dood:latest -f Dockerfile.test .
#0 building with "default" instance using docker driver
#1 [internal] load build definition from Dockerfile.test
#2 [internal] load metadata for docker.io/library/alpine:latest
#5 [2/2] RUN echo 'DooD Verified'
#5 0.107 DooD Verified
#6 naming to docker.io/library/test-dood:latest done
```
Kết quả: Image được build thành công ngay trên Docker daemon của máy host!

---

## 6. CẬP NHẬT GITHUB ACTIONS CI WORKFLOW

File `.github/workflows/ci.yml` được cập nhật bổ sung job `docker_job` chạy trên Self-hosted Runner (`runs-on: [self-hosted, linux]`):
```yaml
  docker_job:
    name: Docker DooD Build & Verification
    needs: build_job
    runs-on: [self-hosted, linux]
    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Verify Docker Daemon Connection (DooD)
        run: |
          echo "=== 1. Checking Docker Daemon via DooD Socket ==="
          docker info
          docker version

      - name: Build Payment Service Docker Image
        run: |
          echo "=== 2. Testing Docker Build via DooD Socket ==="
          docker build -t quickbite-payment-service:latest -f payment-service/Dockerfile .

      - name: Verify Built Docker Image
        run: |
          echo "=== 3. Verifying Docker Images on Host Daemon ==="
          docker images | grep quickbite-payment-service
```

---

## 7. HƯỚNG DẪN KHỞI CHẠY RUNNER VÀ NỘP BÀI

### 7.1. Lấy Runner Token từ GitHub
1. Truy cập vào GitHub Repository của bạn: `https://github.com/ltvhcung001/session7-md5` (hoặc repo tương ứng).
2. Chọn **Settings** -> **Actions** -> **Runners** -> Click **New self-hosted runner**.
3. Sao chép chuỗi token đăng ký hiển thị trong câu lệnh `./config.sh --token <TOKEN>`.

### 7.2. Khởi chạy Runner bằng Docker Compose
Chỉ cần chạy script tiện ích được tạo sẵn:
```bash
RUNNER_TOKEN="<TOKEN_VỪA_LẤY>" ./start-runner.sh
```
Hoặc dùng docker compose trực tiếp:
```bash
RUNNER_TOKEN="<TOKEN_VỪA_LẤY>" docker compose -f docker-compose.runner.yml up -d
```
Xem log của runner:
```bash
docker logs -f quickbite-github-runner
```
Khi thấy dòng log: `Listening for Jobs` là Runner đã sẵn sàng nhận việc.

### 7.3. Đẩy code lên GitHub và nộp bài
1. Push branch `Session8-HW1` lên GitHub:
   ```bash
   git push origin Session8-HW1
   ```
2. Mở tab **Actions** trên GitHub Repo, theo dõi pipeline chạy job `docker_job` trên Self-hosted runner.
3. Chụp ảnh màn hình log thành công của các bước `Verify Docker Daemon Connection (DooD)` và `Build Payment Service Docker Image`.
4. Nộp file `docker-compose.runner.yml` (hoặc `runner/docker-compose.yml`) và ảnh chụp màn hình lên hệ thống Portal LMS.
