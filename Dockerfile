# ============================================================
# Render 배포용 Dockerfile (Spring Boot 3 + Java 21)
# ============================================================

# Build stage
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon || true

COPY src src
RUN gradle bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 비 root 사용자로 실행 (보안)
RUN adduser -D -u 1000 appuser
USER appuser

COPY --from=builder /app/build/libs/*.jar app.jar

# Render는 PORT 환경 변수를 주입함 (application.yml 에서 ${PORT:8080} 사용)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
