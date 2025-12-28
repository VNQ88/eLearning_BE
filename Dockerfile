# Stage 1: Build with Gradle Wrapper (Java 21)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Cài bash (gradlew đôi khi cần)
RUN apk add --no-cache bash

# Copy các file cấu hình trước để tận dụng cache
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle.kts settings.gradle.kts ./
COPY gradle.properties* ./

# Tải dependencies trước (tăng cache). Nếu dự án bạn cần src để resolve, dòng này vẫn ổn nhờ "|| true".
RUN chmod +x ./gradlew && ./gradlew --no-daemon dependencies || true

# Copy source
COPY src ./src

# Build bootJar, bỏ qua test
RUN ./gradlew --no-daemon clean bootJar -x test


# Stage 2: Run with JRE
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Set timezone
ENV TZ=Asia/Ho_Chi_Minh

# Tạo user không phải root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy jar đã được đặt tên cố định là app.jar từ bootJar config
COPY --from=builder /app/build/libs/app.jar app.jar

# Đổi quyền sở hữu
RUN chown appuser:appgroup app.jar

# Chạy bằng user thường
USER appuser

EXPOSE 8080
ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
