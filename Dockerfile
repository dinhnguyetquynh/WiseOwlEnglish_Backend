# spring-app/Dockerfile
FROM eclipse-temurin:21-jdk-jammy

# (Không cần ffmpeg nếu transcriber convert file)
# Nếu bạn muốn app convert trước khi upload, uncomment install ffmpeg
# RUN apt-get update && apt-get install -y ffmpeg && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY build/libs/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","/app/app.jar"]
