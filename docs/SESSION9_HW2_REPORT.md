# BÁO CÁO THỰC HÀNH BÀI 2 (SESSION 9): CONTAINER HÓA ỨNG DỤNG SPRING BOOT MICROSERVICES

**Dự án:** QuickBite Microservices Platform (`Payment Service`)  
**Khóa học / Module:** MD5 - DevOps & Containerization  
**Branch thực hiện:** `Session9-HW2`  
**Học viên:** `ltvhcung001`  
**File kết quả:** `Dockerfile`  

---

## 1. MỤC TIÊU KIẾN THỨC & BỐI CẢNH
- Nắm vững vai trò và chức năng của các chỉ thị cơ bản trong Dockerfile: `FROM`, `WORKDIR`, `COPY`, `EXPOSE`, `ENTRYPOINT`.
- Đóng gói ứng dụng Spring Boot Microservices từ file thực thi `.jar` (do Gradle tạo ra) thành một Docker Image sẵn sàng deploy lên môi trường Production.
- Áp dụng các nguyên tắc tối ưu hóa kích thước image bằng cách chọn base image `JRE Alpine` thay vì `JDK`.

---

## 2. PHÂN TÍCH VÀ ĐIỀN KHUYẾT CÁC CHỈ THỊ DOCKERFILE

Template ban đầu:
```dockerfile
# 1. Chọn base image chứa JRE 17 gọn nhẹ
_____ eclipse-temurin:17-jre-alpine

# 2. Tạo thư mục làm việc trong container
_____ /app

# 3. Copy file jar từ thư mục build của Gradle vào container
_____ build/libs/payment-service-1.0.0.jar app.jar

# 4. Mở port 8080 để giao tiếp với các microservices khác
_____ 8080

# 5. Lệnh khởi chạy ứng dụng Spring Boot
_____ ["java", "-jar", "app.jar"]
```

### Bảng phân tích chi tiết 5 từ khóa (Keywords):

| Vị trí | Keyword điền | Cú pháp hoàn chỉnh | Vai trò & Giải thích kỹ thuật |
| :---: | :---: | :--- | :--- |
| **# 1** | **`FROM`** | `FROM eclipse-temurin:17-jre-alpine` | Chỉ định **Base Image** nền tảng cho container. Sử dụng Eclipse Temurin JRE 17 trên nền Alpine Linux giúp tối ưu dung lượng (chỉ ~68.9MB nén), loại bỏ các công cụ phát triển thừa của JDK. |
| **# 2** | **`WORKDIR`** | `WORKDIR /app` | Thiết lập **thư mục làm việc** (Working Directory) mặc định bên trong container. Mọi lệnh `COPY`, `RUN`, `ENTRYPOINT` phía sau đều lấy `/app` làm gốc. Nếu thư mục chưa có, Docker sẽ tự động khởi tạo. |
| **# 3** | **`COPY`** | `COPY build/libs/payment-service-1.0.0.jar app.jar` | **Sao chép artifact** từ máy host (đường dẫn `build/libs/payment-service-1.0.0.jar`) vào thư mục `/app` trong container và đổi tên chuẩn hóa thành `app.jar`. |
| **# 4** | **`EXPOSE`** | `EXPOSE 8080` | **Khai báo cổng mạng** (Port metadata) mà container sẽ lắng nghe khi chạy. Giúp tài liệu hóa và hỗ trợ mapping port liên lạc giữa các microservices. |
| **# 5** | **`ENTRYPOINT`** | `ENTRYPOINT ["java", "-jar", "app.jar"]` | Định nghĩa **tiến trình chính** (PID 1) sẽ luôn luôn được thực thi khi container khởi động. Dùng cú pháp Exec Form (mảng JSON) để JVM nhận trực tiếp các tín hiệu hệ điều hành (`SIGTERM`). |

---

## 3. TỆP TIN `Dockerfile` HOÀN CHỈNH

```dockerfile
# 1. Chọn base image chứa JRE 17 gọn nhẹ
FROM eclipse-temurin:17-jre-alpine

# 2. Tạo thư mục làm việc trong container
WORKDIR /app

# 3. Copy file jar từ thư mục build của Gradle vào container
COPY build/libs/payment-service-1.0.0.jar app.jar

# 4. Mở port 8080 để giao tiếp với các microservices khác
EXPOSE 8080

# 5. Lệnh khởi chạy ứng dụng Spring Boot
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 4. KIỂM CHỨNG THỰC NGHIỆM LOCAL

### 4.1. Đóng gói image bằng Docker CLI
```bash
$ docker build -t payment-service:1.0.0 .
```

*Output build:*
```text
[+] Building 0.7s (8/8) FINISHED
 => [1/3] FROM docker.io/library/eclipse-temurin:17-jre-alpine
 => [2/3] WORKDIR /app
 => [3/3] COPY build/libs/payment-service-1.0.0.jar app.jar
 => naming to docker.io/library/payment-service:1.0.0
```

### 4.2. Khởi chạy và kiểm tra tính sẵn sàng của Container
```bash
$ docker run --rm -d -p 8084:8084 --name test-payment payment-service:1.0.0
$ curl -s http://localhost:8084/api/v1/payments/health
{"status":"UP","service":"payment-service"}
$ docker stop test-payment
```
Ứng dụng khởi động trong **2.5 giây** và phản hồi HTTP 200 OK thông suốt.

---

## 5. HƯỚNG DẪN NỘP BÀI LÊN PORTAL

1. **File nộp:**
   * File [`Dockerfile`](file:///home/cungh/Documents/rikkei/md5/ss7/Dockerfile) (không có đuôi mở rộng).
2. **Đường dẫn link GitHub Repository:**
   👉 **`https://github.com/ltvhcung001/session7-md5/tree/Session9-HW2`**
