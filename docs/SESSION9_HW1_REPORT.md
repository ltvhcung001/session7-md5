# BÁO CÁO THỰC HÀNH BÀI 1 (SESSION 9): KHẮC PHỤC SỰ CỐ PIPELINE CI/CD CƠ BẢN TRÊN GITLAB CI

**Dự án:** QuickBite Microservices Platform (`User Service`)  
**Khóa học / Module:** MD5 - DevOps & CI/CD Pipeline  
**Branch thực hiện:** `Session9-HW1`  
**Học viên:** `ltvhcung001`  
**File cấu hình:** `.gitlab-ci.yml`  

---

## 1. PHÂN TÍCH NGUYÊN NHÂN SỰ CỐ TRÊN PIPELINE GỐC

### 1.1. Đoạn mã gốc bị lỗi
```yaml
stages:
  build_app

build_job:
  stage: build_app
  script:
    - ./gradlew clean build -x test
```

### 1.2. Phân tích chi tiết các điểm lỗi & thiếu sót

#### LỖI 1: Sai cú pháp khai báo mảng (Sequence / List) trong YAML tại keyword `stages`
* **Hiện tượng:**
  ```yaml
  stages:
    build_app
  ```
* **Nguyên nhân kỹ thuật:**
  * Trong GitLab CI, từ khóa `stages` dùng để định nghĩa trình tự các giai đoạn của pipeline và bắt buộc phải là một **mảng các chuỗi ký tự (Array of Strings / YAML Sequence)**.
  * Theo chuẩn cú pháp YAML, mỗi phần tử trong mảng phải bắt đầu bằng một dấu gạch ngang và một dấu cách (`- <value>`).
  * Việc viết `build_app` không có dấu gạch ngang khiến bộ phân tích cú pháp YAML (YAML Parser) hiểu đây là một chuỗi vô hướng (scalar string) hoặc một key không có giá trị, dẫn đến lỗi cú pháp nghiêm trọng:
    ```text
    jobs:stages config should be an array of strings
    ```
  * **Hậu quả:** GitLab CI từ chối khởi tạo toàn bộ Pipeline ngay tại bước Parse YAML (Pipeline parsing error / Invalid YAML syntax), không có job nào được lập lịch chạy.

#### LỖI 2: Thiếu định nghĩa môi trường thực thi (keyword `image`)
* **Hiện tượng:**
  Trong file hoàn toàn không khai báo từ khóa `image:` ở cấp toàn cục (global) hoặc cấp job.
* **Nguyên nhân kỹ thuật:**
  * GitLab Runner sử dụng Docker Executor để khởi chạy các job trong các container cách ly. Nếu file cấu hình không chỉ định rõ Docker image cần dùng, runner sẽ tải image mặc định của hệ thống (thường là `ruby:latest`, `alpine:latest` hoặc `ubuntu:latest` tùy cấu hình runner).
  * Các image mặc định này là môi trường hệ điều hành cơ bản, **hoàn toàn không cài đặt sẵn Java Runtime / OpenJDK (Java Development Kit) hay Gradle**.
  * Khi runner chuyển sang bước thực thi `script:` và gọi lệnh `./gradlew clean build -x test`:
    * Script `gradlew` kiểm tra biến môi trường `JAVA_HOME` và tìm kiếm binary `java` trong `$PATH`.
    * Do không có Java, shell lập tức báo lỗi:
      ```text
      /bin/sh: eval: line 1: ./gradlew: not found
      HOẶC
      ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
      HOẶC
      bash: java: command not found
      ```
  * **Hậu quả:** Pipeline thất bại ngay từ giây đầu tiên của job `build_job`.

#### ĐIỂM THIẾU SÓT BỔ SUNG (Best Practices trong môi trường Enterprise):
1. **Thiếu cấp quyền thực thi cho Gradle Wrapper:**
   * Khi mã nguồn được clone về runner, file `./gradlew` có thể bị mất cờ thực thi (`chmod +x`), gây ra lỗi `Permission denied`. Cần bổ sung hook `before_script: - chmod +x ./gradlew`.
2. **Thiếu cơ chế Cache:**
   * Việc không cấu hình `cache:` cho thư mục `.gradle/caches/` và `.gradle/wrapper/` khiến mỗi lần chạy pipeline runner đều phải tải lại toàn bộ dependencies và bộ wrapper phân phối của Gradle từ internet, làm lãng phí băng thông và kéo dài thời gian build.
3. **Thiếu lưu trữ Artifacts:**
   * Sau khi build ra file `.jar`, nếu không khai báo `artifacts:`, file build sẽ bị xóa ngay khi container kết thúc, không thể bàn giao cho các stage tiếp theo (như deploy/dockerize).

---

## 2. NỘI DUNG FILE `.gitlab-ci.yml` HOÀN CHỈNH ĐÃ SỬA

```yaml
# ==============================================================================
# GitLab CI Configuration - QuickBite Microservices Platform
# Module: User / Payment Service Build Pipeline
# ==============================================================================

# 1. Khai báo Docker image chứa sẵn môi trường OpenJDK 17 và Alpine Linux siêu nhẹ
image: eclipse-temurin:17-jdk-alpine

# 2. Khai báo danh sách các stages đúng chuẩn cú pháp mảng trong YAML (sử dụng dấu gạch ngang)
stages:
  - build_app

# Cấu hình biến môi trường hỗ trợ Gradle
variables:
  GRADLE_OPTS: "-Dorg.gradle.daemon=false"

# Thiết lập bộ nhớ đệm cache để tối ưu tốc độ build ở các lần chạy tiếp theo
cache:
  key: "$CI_COMMIT_REF_SLUG"
  paths:
    - .gradle/wrapper/
    - .gradle/caches/

# Job thực hiện đóng gói ứng dụng
build_job:
  stage: build_app
  before_script:
    # Cấp quyền thực thi cho Gradle wrapper script
    - chmod +x ./gradlew
  script:
    # Thực hiện build ứng dụng bỏ qua bước chạy test
    - ./gradlew clean build -x test
  artifacts:
    name: "user-service-build-$CI_COMMIT_SHORT_SHA"
    paths:
      - build/libs/*.jar
    expire_in: 1 week
```

---

## 3. KIỂM THỬ THỰC TẾ LỆNH BUILD TRONG CONTAINER ĐƯỢC CHỈ ĐỊNH

Đã chạy kiểm chứng trực tiếp câu lệnh `./gradlew clean build -x test` bên trong container `eclipse-temurin:17-jdk-alpine`:
```bash
$ docker run --rm -v $(pwd):/app -w /app eclipse-temurin:17-jdk-alpine ./gradlew clean build -x test
```

### Trích xuất Output thực tế từ Terminal:
```text
> Task :clean
> Task :compileJava
> Task :processResources
> Task :classes
> Task :resolveMainClassName
> Task :bootJar
> Task :jar
> Task :assemble
> Task :check
> Task :build

BUILD SUCCESSFUL in 1m 2s
6 actionable tasks: 6 executed
```

File JAR đầu ra được sinh thành công tại `build/libs/`:
- `payment-service-0.0.1-SNAPSHOT.jar` (Kích thước: 21MB)

---

## 4. HƯỚNG DẪN NỘP BÀI LÊN PORTAL

1. **File nộp:**
   * File cấu hình [`.gitlab-ci.yml`](file:///home/cungh/Documents/rikkei/md5/ss7/.gitlab-ci.yml).
   * File giải thích lỗi [`SESSION9_HW1_EXPLANATION.txt`](file:///home/cungh/Documents/rikkei/md5/ss7/SESSION9_HW1_EXPLANATION.txt).
2. **Đường dẫn (URL) Repository GitHub trên branch `Session9-HW1`:**
   * **`https://github.com/ltvhcung001/session7-md5/tree/Session9-HW1`**
