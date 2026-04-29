# ============================================================
# Render 배포용 Dockerfile (Spring Boot 3 + Java 21)
# ============================================================

# Build stage (단일 산출물 app.jar — COPY 와일드카드로 인한 BuildKit 캐시/체크섬 오류 방지)
FROM gradle:8.5-jdk21-alpine AS builder
WORKDIR /app

COPY build.gradle settings.gradle ./
RUN gradle dependencies --no-daemon

COPY src src
RUN gradle bootJar --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN adduser -D -u 1000 appuser

COPY --from=builder /app/build/libs/app.jar app.jar
RUN chown appuser:appuser app.jar

USER appuser

# Render는 PORT 환경 변수를 주입함 (application.yml 에서 ${PORT:8080} 사용)
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
