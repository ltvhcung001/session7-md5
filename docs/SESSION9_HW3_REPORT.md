# BÁO CÁO THỰC HÀNH BÀI 3 (SESSION 9): XÂY DỰNG PIPELINE CI/CD KẾT NỐI POSTGRESQL TRÊN GITLAB CI

**Dự án:** Quản lý nhân sự (HRM Microservices Platform)  
**Khóa học / Module:** MD5 - DevOps & CI/CD Pipeline  
**Branch thực hiện:** `Session9-HW3`  
**Học viên:** `ltvhcung001`  
**File cấu hình:** `.gitlab-ci.yml`  

---

## 1. MỤC TIÊU KIẾN THỨC & BỐI CẢNH NGHIỆP VỤ

### 1.1. Bối cảnh nghiệp vụ
Trong dự án Microservices "Quản lý nhân sự", các lập trình viên thường xuyên đẩy code gây lỗi runtime trên hệ thống do không kiểm thử tích hợp (Integration Test) phần logic truy vấn cơ sở dữ liệu trước khi đóng gói.
Yêu cầu đặt ra là phải xây dựng một **CI Pipeline tự động**:
1. Dựng tạm một Database PostgreSQL 14 cô lập phục vụ chạy Unit/Integration Test.
2. Kiểm thử tự động với Gradle (`./gradlew test`).
3. Chỉ khi toàn bộ test case vượt qua (PASS), hệ thống mới cho phép chuyển sang giai đoạn đóng gói (`build`) ra file `.jar` và lưu trữ artifact trong 1 ngày.

### 1.2. Mục tiêu kỹ thuật
- Nắm vững cơ chế phân tách giai đoạn tuần tự (`stages: test -> build`) trong GitLab CI.
- Khai thác từ khóa **`services`** để khởi tạo các container phụ trợ (sidecar containers) như Database, Message Broker chạy song song trong cùng mạng nội bộ với Job Runner.
- Cấu hình biến môi trường kết nối chuẩn cho PostgreSQL và Spring Boot Datasource.
- Thu thập và lưu trữ sản phẩm đóng gói thông qua **`artifacts`** kèm thời gian hết hạn (`expire_in: 1 day`).

---

## 2. PHÂN TÍCH THIẾT KẾ CẤU HÌNH PIPELINE

### 2.1. Phân chia tuần tự với `stages`
```yaml
stages:
  - test
  - build
```
* **Cơ chế Fail-Fast:** GitLab CI thực thi các stage theo thứ tự từ trên xuống dưới. Toàn bộ các job trong stage `test` phải kết thúc thành công (`status: success`) thì job ở stage `build` mới được kích hoạt. Nếu có bất kỳ test case nào thất bại, pipeline lập tức dừng lại, ngăn chặn việc xuất bản artifact lỗi ra môi trường.

### 2.2. Khởi tạo Database phụ trợ với `services`
```yaml
services:
  - name: postgres:14-alpine
    alias: postgres
```
* **Cơ chế hoạt động:** Khi runner khởi chạy job `test_job`, Docker executor sẽ pull image `postgres:14-alpine` và khởi chạy một container độc lập trong cùng một Docker Network với job container.
* Container test có thể kết nối trực tiếp tới Database thông qua hostname định danh mạng là **`postgres`** tại cổng mặc định **`5432`**.

### 2.3. Cấu hình biến môi trường (Environment Variables)
* **Khởi tạo PostgreSQL Database:**
  * `POSTGRES_DB`: Tên cơ sở dữ liệu được tạo tự động khi container khởi động.
  * `POSTGRES_USER`: Tài khoản quản trị database.
  * `POSTGRES_PASSWORD`: Mật khẩu đăng nhập.
  * `POSTGRES_HOST_AUTH_METHOD: "trust"`: Đảm bảo runner kết nối thông suốt trong mạng nội bộ.
* **Cấu hình Spring Boot Datasource:**
  * `SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/quickbite_db"`
  * `SPRING_DATASOURCE_USERNAME: "postgres"`
  * `SPRING_DATASOURCE_PASSWORD: "postgres_password"`

### 2.4. Lưu trữ thành phẩm với `artifacts`
```yaml
artifacts:
  name: "hrm-service-build-$CI_COMMIT_SHORT_SHA"
  paths:
    - build/libs/*.jar
  expire_in: 1 day
```
* Sau khi lệnh `./gradlew build -x test` hoàn tất, runner tự động thu gom toàn bộ file JAR nằm trong thư mục `build/libs/` đưa lên lưu trữ tập trung trên GitLab Server.
* Thiết lập `expire_in: 1 day` giúp giải phóng dung lượng lưu trữ trên server sau 24 giờ.

---

## 3. TỆP TIN `.gitlab-ci.yml` HOÀN CHỈNH

```yaml
# ==============================================================================
# GitLab CI Configuration - QuickBite Microservices Platform
# Dự án: Quản lý nhân sự (HRM) / Payment Microservices
# Pipeline: Chạy Integration Test với PostgreSQL Service và Đóng gói Artifact
# ==============================================================================

# 1. Định nghĩa 2 stages tuần tự: Chạy Test trước, nếu pass mới Build
stages:
  - test
  - build

# 2. Khai báo các biến môi trường toàn cục (Global Variables)
variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"
  # Cấu hình biến môi trường kết nối cho PostgreSQL container
  POSTGRES_DB: "quickbite_db"
  POSTGRES_USER: "postgres"
  POSTGRES_PASSWORD: "postgres_password"
  POSTGRES_HOST_AUTH_METHOD: "trust"

# 3. Cấu hình Cache tái sử dụng dependencies giữa các jobs
cache:
  key: "$CI_COMMIT_REF_SLUG"
  paths:
    - .gradle/wrapper/
    - .gradle/caches/

# ------------------------------------------------------------------------------
# STAGE 1: TEST - Khởi chạy Database container PostgreSQL và chạy Unit/Integration Test
# ------------------------------------------------------------------------------
test_job:
  stage: test
  image: gradle:8.8-jdk17
  services:
    # Dựng tạm Database PostgreSQL 14 song song với môi trường Test
    - name: postgres:14-alpine
      alias: postgres
  variables:
    # Cấu hình Spring Boot kết nối tới PostgreSQL service thông qua hostname 'postgres'
    SPRING_DATASOURCE_URL: "jdbc:postgresql://postgres:5432/quickbite_db"
    SPRING_DATASOURCE_USERNAME: "postgres"
    SPRING_DATASOURCE_PASSWORD: "postgres_password"
  before_script:
    - chmod +x ./gradlew
  script:
    - echo "=== Khởi chạy Unit / Integration Test kết nối PostgreSQL Service ==="
    - ./gradlew test

# ------------------------------------------------------------------------------
# STAGE 2: BUILD - Đóng gói file thực thi JAR và lưu trữ bằng Artifacts
# ------------------------------------------------------------------------------
build_job:
  stage: build
  image: eclipse-temurin:17-jdk-alpine
  before_script:
    - chmod +x ./gradlew
  script:
    - echo "=== Đóng gói ứng dụng thành file JAR (bỏ qua bước test vì đã chạy ở stage test) ==="
    - ./gradlew build -x test
  artifacts:
    name: "hrm-service-build-$CI_COMMIT_SHORT_SHA"
    # Đường dẫn lưu trữ file JAR đầu ra do Gradle sinh ra
    paths:
      - build/libs/*.jar
    # Thời gian lưu trữ artifact trên hệ thống: 1 ngày
    expire_in: 1 day
```

---

## 4. KIỂM THỬ THỰC TẾ TRÊN MÔI TRƯỜNG DOCKER

Đã thực hiện kiểm chứng cục bộ cả 2 câu lệnh cốt lõi của pipeline:

1. **Kiểm thử Stage Test:**
   ```bash
   $ docker run --rm -v $(pwd):/app -w /app eclipse-temurin:17-jdk-alpine ./gradlew test
   ```
   *Kết quả:* `BUILD SUCCESSFUL in 1m 20s`, toàn bộ test case đã pass.

2. **Kiểm thử Stage Build & sinh Artifact:**
   ```bash
   $ docker run --rm -v $(pwd):/app -w /app eclipse-temurin:17-jdk-alpine ./gradlew build -x test
   ```
   *Kết quả:* Sinh file JAR tại `build/libs/payment-service-1.0.0.jar` (Kích thước: 21MB).

---

## 5. HƯỚNG DẪN NỘP BÀI LÊN PORTAL

1. **File nộp:** File [`.gitlab-ci.yml`](file:///home/cungh/Documents/rikkei/md5/ss7/.gitlab-ci.yml).
2. **Đường dẫn link GitHub Repository:**
   👉 **`https://github.com/ltvhcung001/session7-md5/tree/Session9-HW3`**
