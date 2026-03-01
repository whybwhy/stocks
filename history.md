# 변경 이력 (History)

이 문서에는 프로젝트에 반영된 주요 변경 사항이 요약되어 기록됩니다. 앞으로도 수정·기능 추가 시 여기에 요약을 기재합니다.

---

## 2026-02-28 (카카오 로그인 동작 정리)

### 1. 카카오 로그인 후 처음 접속하려던 URL로 리다이렉트

- **SecurityConfig**에 `SavedRequestAwareAuthenticationSuccessHandler` 사용.
- 로그인 전에 접근하려던 URL이 있으면 **그 주소로**, 없으면 **`/`** 로 이동.
- `setAlwaysUseDefaultTargetUrl(false)` 로 “저장된 요청 우선” 동작 유지.

### 2. 카카오 로그인 인증 유효 시간

- **카카오 토큰**: Access Token 약 12시간(43,199초), Refresh Token 약 60일.
- **이 서비스 인증**: **HTTP 세션** 기준, Spring Boot 기본 **30분** 비활동 시 세션 만료. `application.yml` 의 `server.servlet.session.timeout` 으로 변경 가능.

### 3. 카카오 로그인 인증 저장 위치

- **서버 HttpSession**에 저장. **SecurityContext → Authentication(OAuth2User)**. 세션 ID는 클라이언트 쿠키 **`JSESSIONID`** 로 전달.

### 4. 카카오 로그인 인증 만료(로그아웃) 방법

- **SecurityConfig**에 로그아웃 추가.
  - **URL**: `GET` 또는 `POST` **`/logout`**
  - 동작: 세션 무효화, `JSESSIONID` 쿠키 삭제 후 **`/`** 로 리다이렉트.
- `https://your-app.onrender.com/logout` 또는 `http://localhost:8080/logout` 로 로그아웃.

### 5. 요청이 올 때마다 카카오 인증 정보 로그 출력

- **KakaoAuthLoggingFilter** 추가. **AuthorizationFilter** 직전에 실행.
- 인증된 OAuth2 사용자에 대해 요청마다 **INFO** 로그: `[Kakao 인증] principal=..., name=..., attributes=[...]`
- 미인증 요청은 **DEBUG** 레벨에서만 로그 (기본 INFO 시 미출력).

### 수정·추가된 파일

- `SecurityConfig.java`: 로그인 성공 시 원래 URL 리다이렉트, 로그아웃 설정, `KakaoAuthLoggingFilter` 등록.
- `KakaoAuthLoggingFilter.java`: 요청마다 카카오(OAuth2) 인증 정보 로그.
- `README.md`: 위 1~5 항목 요약 정리.

### 세션 타임아웃 변경 방법

`application.yml` 에서 예시:

```yaml
server:
  servlet:
    session:
      timeout: 30m   # 원하는 값으로 변경 (예: 1h, 60m)
```

---

## 2026-02-28 (주식 가격 원화 포맷)

- **1·2·3차 매수 가격**: Thymeleaf `#numbers.formatDecimal(..., 1, 'POINT', 0, 'COMMA')` 적용. 천 단위 콤마(,)로 표시 (예: 70,000원).

---

## 2026-02-28 (Render IP 대역, KAKAO_REDIRECT_URI)

- **Render IP**: Render 아웃바운드 IP는 고정이 아니며, 리전별 CIDR 은 대시보드 **Connect → Outbound** 탭에서 확인하도록 README/답변에 정리.
- **KAKAO_REDIRECT_URI**:  
  - 로컬: 미설정 시 `{baseUrl}` → `http://localhost:8080`.  
  - Render: `KAKAO_REDIRECT_URI=https://stocks-ser4.onrender.com/login/oauth2/code/kakao` 로 환경 변수 설정.  
  - `application.yml` 주석 및 DEPLOY.md 에 위 내용 반영.

---

## 2026-02-28 (Render Docker 배포)

- **Dockerfile**: Gradle 8.5 + JDK 21 빌드, Eclipse Temurin 21 JRE 런타임. 비 root 사용자 실행. Render `PORT` 환경 변수 대응.
- **.dockerignore**: `application-local.yml`, `stocks.sql` 제외. 빌드/IDE 불필요 파일 제외.
- **render.yaml**: Render Blueprint. `runtime: docker`, `healthCheckPath: /health`.

---

## 2026-02-28 (Git 연동, stocks.sql 제외)

- **stocks.sql 커밋 제외**: `git rm --cached src/main/resources/stocks.sql` 후 `git commit --amend`. 로컬 파일은 유지.
- **.gitignore**: `src/main/resources/stocks.sql` 추가.

---

## 2026-02-28 (환경 변수 분리, Render 배포 가이드)

- **application.yml**: Supabase/Kakao 값을 환경 변수로 분리 (`SUPABASE_URL`, `SUPABASE_ANON_KEY`, `KAKAO_CLIENT_ID`, `KAKAO_CLIENT_SECRET`, `KAKAO_REDIRECT_URI`). `server.port: ${PORT:8080}` 로 Render 대응.
- **application-local.example.yml**: 로컬용 설정 예시. 복사 후 `application-local.yml` 로 사용 (gitignore 대상).
- **.gitignore**: 신규 생성. `application-local.yml`, build/IDE/bin 등.
- **DEPLOY.md**: Git 초기화·GitHub 연동·Render Web Service 생성·환경 변수·카카오 Redirect URI 설정을 단계별로 정리.

---

## 2026-02-28 (Supabase 요청/응답 로그)

- **SupabaseConfig**: `ClientHttpRequestInterceptor` 로 Supabase RestClient 요청(method, uri, headers, body)·응답(status, headers) 로그 출력.

---

## 2026-02-28 (SupabaseController @RequestParam 이름)

- **SupabaseController**: `@RequestParam String table` → `@RequestParam("table")`, `@RequestParam(name = "select", ...)` 로 변경. `-parameters` 미사용 환경에서도 동작하도록 수정.

---

## 2026-02-28 (Asia/Seoul 타임존)

- **application.yml**: `spring.jackson.time-zone`, `spring.mvc.time-zone` 을 `Asia/Seoul` 로 설정.
- **stocks.sql**: INSERT 전 `SET TIME ZONE 'Asia/Seoul'` 추가.

---

## 2026-02-28 (주식 테이블 Thymeleaf + Tailwind)

- **의존성**: `spring-boot-starter-thymeleaf` 추가.
- **StockDto**: Supabase `stocks` 테이블 JSON 매핑 (id, stockName, firstBuyPrice, secondBuyPrice, thirdBuyPrice, description, createdAt).
- **SupabaseService**: `getStocks()` 추가. `/rest/v1/stocks` 조회 후 `List<StockDto>` 반환.
- **StockViewController**: `GET /stocks` → `stocks/list` 뷰, 모델에 `stocks` 전달.
- **templates/stocks/list.html**: Tailwind 테이블. `th:each`, 가격·createdAt 포맷. 데이터 없을 때 안내 문구. 테이블 셀 간격(px-6, py-3/4). 가격 소수점 제거.

---

## 2026-02-28 (주식 테이블 SQL)

- **stocks.sql**: `public.stocks` 테이블 DDL. `id` bigserial PK, stock_name, first_buy_price, second_buy_price, third_buy_price, description, type, recommended_at, created_at, updated_at. KST 기본값용 `SET TIME ZONE 'Asia/Seoul'` 포함. (파일은 .gitignore로 저장소 제외)

---

## 2026-02-28 (Supabase REST 연동)

- **SupabaseProperties**: `supabase.url`, `supabase.anon-key` 바인딩.
- **SupabaseConfig**: Supabase 전용 `RestClient` 빈. baseUrl, apikey/Authorization 헤더.
- **SupabaseService**: `getTableRows(table, select)` 로 REST 조회.
- **SupabaseController**: `GET /api/supabase/rows?table=...&select=...` 테스트용 API.

---

## 2026-02-28 (프로젝트 초기 생성)

- **Spring Boot 3.3.3 + Java 21**: Gradle, spring-boot-starter-web, security, oauth2-client, validation.
- **SecurityConfig**: `/`, `/health` permitAll, 나머지 인증 필요. OAuth2 로그인 기본 설정.
- **application.yml**: server.port 8080, Kakao OAuth2 registration/provider (client-id, client-secret, redirect-uri, scope 등), supabase.url/anon-key placeholder.
- **HealthController**: `GET /health` → `{"status":"ok"}`.
- **StocksApplication**: `@SpringBootApplication` 진입점.

---

*이후 변경이 있을 때마다 상단에 날짜와 함께 요약을 추가해 주세요.*
