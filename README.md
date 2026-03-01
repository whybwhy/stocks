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

## 카카오 로그인 동작

- **로그인 후 이동**: 접속하려던 URL이 있으면 그 주소로, 없으면 `/` 로 리다이렉트됩니다.
- **인증 유효 시간**
  - **카카오 토큰**: Access Token 약 12시간, Refresh Token 약 60일(카카오 기준).
  - **이 서비스 인증**: 서버 **HTTP 세션** 기준이며, 기본 30분 비활동 시 만료됩니다. (`server.servlet.session.timeout` 으로 변경 가능)
- **인증 저장 위치**: 서버 **HttpSession** (메모리). 세션 ID는 쿠키 `JSESSIONID` 로 전달됩니다.
- **인증 만료(로그아웃)**: `GET` 또는 `POST` `/logout` 호출 시 세션 삭제 후 `/` 로 보냅니다.
- **요청 시 로그**: 인증된 사용자의 카카오 principal/attributes 는 요청마다 로그에 `[Kakao 인증]` 로 출력됩니다.

## 배포 (Render)

Git 연동 및 Render 배포 절차는 **[DEPLOY.md](DEPLOY.md)** 에 정리되어 있습니다.

docker build -t stocks .
docker run -p 8080:8080 -e PORT=8080 -e SUPABASE_URL=... -e SUPABASE_ANON_KEY=... stocks