# 1. Sử dụng nền móng Java 21 (Eclipse Temurin là bản phân phối OpenJDK rất tốt)
FROM eclipse-temurin:21-jdk-alpine

# 2. Tạo thư mục làm việc
WORKDIR /app

# 3. Copy file .jar từ thư mục build của Gradle
# Lưu ý: Gradle lưu file build trong "build/libs/" thay vì "target/" như Maven
COPY build/libs/*.jar app.jar

# 4. Lệnh chạy ứng dụng
ENTRYPOINT ["java", "-jar", "app.jar"]
