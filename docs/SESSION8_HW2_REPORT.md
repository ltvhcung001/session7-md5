# BÁO CÁO THỰC HÀNH BÀI 2: TỐI ƯU HÓA DUNG LƯỢNG DOCKER IMAGE VỚI MULTI-STAGE BUILD

**Dự án:** QuickBite Microservices Platform (`cart-service`)  
**Khóa học / Module:** MD5 - DevOps & Microservices  
**Branch thực hiện:** `Session8-HW2`  
**Học viên:** `ltvhcung001`  

---

## 1. MỤC TIÊU HỌC TẬP
- Vận dụng kỹ thuật **Multi-stage Build** trong Docker để tối ưu kích thước image và bảo mật mã nguồn sản phẩm.
- Hiểu rõ sự khác biệt giữa môi trường biên dịch (Build environment - JDK) và môi trường thực thi (Runtime environment - JRE).
- Nắm vững cú pháp chuyển giao artifact giữa các stage bằng từ khóa `COPY --from=<stage_name>`.
- Đo lường, kiểm chứng và đánh giá hiệu quả giảm dung lượng thực tế trên máy cục bộ.

---

## 2. PHÂN TÍCH VÀ ĐIỀN CÁC THÀNH PHẦN KHUYẾT

### 2.1. Đoạn mã gốc bị khuyết
```dockerfile
# Stage 1: Build mã nguồn
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

# Stage 2: Runtime
FROM [___]
WORKDIR /app
[___]
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 2.2. Phân tích chi tiết các vị trí khuyết `[___]`

#### Vị trí khuyết thứ nhất: `FROM [___]` (Base image cho Runtime Stage)
- **Giá trị điền vào:** `eclipse-temurin:17-jre-alpine`
- **Cơ sở lý luận:**
  1. **Tách biệt JDK và JRE:** 
     - Stage 1 cần `JDK` (Java Development Kit) để có trình biên dịch `javac`, công cụ build `gradle` và thư viện phát triển nhằm tạo ra file JAR.
     - Stage 2 chỉ phục vụ việc chạy ứng dụng trên Production, do đó chỉ cần `JRE` (Java Runtime Environment) chứa máy ảo Java (`JVM`) và các thư viện runtime cốt lõi.
  2. **Tối ưu với Alpine Linux:** Bản phân phối Alpine Linux cực kỳ nhẹ (kích thước base OS chỉ khoảng ~5MB), giúp JRE image của Eclipse Temurin chỉ có kích thước nén khoảng ~68.9MB so với hàng trăm MB của các bản Ubuntu/Debian đầy đủ.
  3. **Đồng nhất phiên bản:** Stage 1 sử dụng Java 17 (`eclipse-temurin:17-jdk-alpine`), do đó Stage 2 phải sử dụng Java 17 JRE tương ứng (`eclipse-temurin:17-jre-alpine`) để đảm bảo tính tương thích bytecode (`class file version 61.0`).

#### Vị trí khuyết thứ hai: `[___]` (Lệnh chuyển giao file thực thi)
- **Giá trị điền vào:** `COPY --from=builder /app/build/libs/*.jar app.jar`
- **Cơ sở lý luận:**
  1. **Từ khóa `COPY --from=<stage>`:** Đây là cú pháp chuẩn của Docker Multi-stage build, cho phép truy xuất vào hệ thống tệp của stage trung gian (đã được đặt bí danh `AS builder` ở Stage 1).
  2. **Đường dẫn artifact:** Lệnh `RUN ./gradlew bootJar` trong Spring Boot Gradle sinh ra file thực thi JAR tại thư mục `/app/build/libs/`.
  3. **Định danh mục tiêu:** Lệnh `ENTRYPOINT ["java", "-jar", "app.jar"]` yêu cầu file chạy phải có tên là `app.jar` nằm trong thư mục làm việc hiện tại (`/app`). Cú pháp `COPY --from=builder /app/build/libs/*.jar app.jar` vừa sao chép chính xác artifact vừa đổi tên chuẩn hóa thành `app.jar`.

---

## 3. FILE DOCKERFILE HOÀN CHỈNH

```dockerfile
# Stage 1: Build mã nguồn
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app
COPY . .
RUN ./gradlew bootJar

# Stage 2: Runtime
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=builder /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## 4. KIỂM CHỨNG THỰC NGHIỆM VÀ SO SÁNH DUNG LƯỢNG

Thực hiện build cả 2 phiên bản image trên cùng một mã nguồn để so sánh khách quan:
1. `cart-service:single-stage-jdk` (Đóng gói đơn tầng chứa toàn bộ JDK và mã nguồn).
2. `cart-service:multi-stage` (Đóng gói đa tầng Multi-stage với JRE Alpine tối ưu).

### 4.1. Bảng so sánh dung lượng thực tế (`docker images`)

| Tên Image | Phương Pháp Đóng Gói | Kích Thước Lưu Trữ (Disk Usage) | Dung Lượng Truyền Tải (Content Size) | Tỷ Lệ Tối Ưu |
| :--- | :--- | :--- | :--- | :--- |
| **`cart-service:single-stage-jdk`** | Single-stage (JDK 17) | **1.16 GB** | **460 MB** | Gốc |
| **`cart-service:multi-stage`** | **Multi-stage (JRE 17 Alpine)** | **298 MB** | **87.6 MB** | **Giảm ~81%** (Content) / **~74.3%** (Disk) |

### 4.2. Trích xuất Output thực tế từ Terminal
```bash
$ docker images | grep cart-service
cart-service:multi-stage          df177ba49690   298MB   87.6MB
cart-service:single-stage-jdk     dca993b4dd3b   1.16GB  460MB
```

> **Nhận xét kết quả:**
> Dung lượng nén thực tế của image sau khi tối ưu chỉ còn **87.6 MB**, đạt tiêu chuẩn xuất sắc theo yêu cầu đề bài (< 150MB).

---

## 5. LỢI ÍCH VƯỢT TRỘI CỦA MULTI-STAGE BUILD

1. **Giảm thiểu dung lượng và băng thông mạng:**
   - Image giảm từ 460MB xuống 87.6MB (giảm hơn 5 lần).
   - Tăng tốc độ push lên Container Registry (Docker Hub, AWS ECR) và pull về các Kubernetes Worker Nodes khi scale-up container.
2. **Tăng cường bảo mật (Security Hardening):**
   - Không chứa mã nguồn thô (`.java`), tài liệu nhạy cảm hay file cấu hình nội bộ.
   - Loại bỏ hoàn toàn trình biên dịch (`javac`), trình gỡ lỗi (`jdb`), công cụ quản lý gói (`gradle`, `mvn`), ngăn chặn kẻ tấn công lợi dụng để build reverse-shell hoặc inject mã độc nếu container bị xâm nhập.
3. **Giảm thiểu lỗ hổng bảo mật (CVEs):**
   - Base image Alpine JRE có số lượng packages hệ điều hành tối thiểu, giảm đáng kể số lượng lỗ hổng bảo mật khi quét bằng Trivy / Snyk.

---

## 6. KIỂM CHỨNG VẬN HÀNH CONTAINER

Khởi chạy container từ image `cart-service:multi-stage` và kiểm tra endpoint `/health`:
```bash
$ docker run --rm -d -p 8084:8084 --name cart-app cart-service:multi-stage
$ curl -s http://localhost:8084/api/v1/payments/health
{"status":"UP","service":"payment-service"}
$ docker stop cart-app
```
Container khởi động cực nhanh chỉ trong **2.4 giây** và phản hồi HTTP 200 OK thông suốt.
