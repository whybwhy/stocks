# stocks - Spring Boot + Kakao OAuth2 + Supabase

## 로컬 실행 방법

시크릿은 코드에 넣지 않고, **로컬 전용 설정 파일**을 사용합니다.

1. `src/main/resources/application-local.example.yml` 을 복사하여 `application-local.yml` 생성
2. Supabase URL/anon-key, 카카오 client-id/client-secret 을 실제 값으로 채움
3. 아래처럼 `local` 프로파일을 지정하여 실행

```bash
./gradlew bootRun --args='--spring.profiles.active=local'
```

또는 IDE Run Configuration 에서 **Active profiles** 에 `local` 입력 후 실행.

## 배포 (Render)

Git 연동 및 Render 배포 절차는 **[DEPLOY.md](DEPLOY.md)** 에 정리되어 있습니다.

docker build -t stocks .
docker run -p 8080:8080 -e PORT=8080 -e SUPABASE_URL=... -e SUPABASE_ANON_KEY=... stocks