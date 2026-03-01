# 변경 이력 (History)

이 문서에는 프로젝트에 반영된 주요 변경 사항이 요약되어 기록됩니다. 앞으로도 수정·기능 추가 시 여기에 요약을 기재합니다.

---

## 2026-02-28 (세션 ID 쿠키 – 어디에 어떤 이름으로 저장되는지)

**Q. sid 가 어디에 어떤 이름으로 저장되어 있어?**

**"sid"** 는 설명할 때 든 **예시 이름**일 뿐이고, 카카오가 실제로 쓰는 쿠키 이름과는 다를 수 있습니다.

### 1. 어디에 저장되나

- **브라우저의 쿠키 저장소**에 저장됩니다.
- **도메인**은 카카오 쪽이므로 예: `*.kakao.com`, `kauth.kakao.com` (카카오 로그인/동의 페이지), `*.kakao.co.kr` 등 카카오가 지정한 도메인.

즉, **"브라우저가 카카오 도메인용으로 저장해 둔 쿠키"** 안에, 세션을 가리키는 값이 **어떤 이름으로든** 하나 이상 들어 있을 수 있습니다.

### 2. 실제로 어떤 이름인지 확인하는 방법

카카오가 정한 **정확한 쿠키 이름**은 문서를 보거나 직접 확인해야 합니다.

- **Chrome / Edge**: 카카오 로그인(또는 로그인된) 페이지에서 **F12** → **Application** 탭 → 왼쪽 **Cookies** → `https://kauth.kakao.com` 등 카카오 도메인 선택 → 나오는 **Name** 목록이 실제 저장 이름.
- **Firefox**: **F12** → **Storage** 탭 → **Cookies** → 카카오 도메인 선택 후 **Name** 확인.

여기서 보이는 이름이 **실제로 저장되는 이름**이고, **sid** 는 그중 하나일 수도 있고, 다른 이름(예: `_kadu`, `_kawlt` 등 카카오가 쓰는 이름)일 수도 있습니다.

### 요약

- **어디에**: 브라우저 **쿠키** (카카오 도메인용).
- **어떤 이름으로**: 카카오가 정한 이름이라 **공식 문서**나 **개발자 도구 → Application → Cookies** 에서 확인하는 것이 맞고, **sid 는 예시**임.

---

## 2026-02-28 (카카오 – 세션/쿠키로 사용자를 어떤 정보로 파악하는지)

**Q. 카카오가 기존 로그인 정보(세션/쿠키)로 “이 사람이 누구인지” 어떤 정보로 파악하고 있는 거야?**

카카오가 쓰는 건 **브라우저에 저장된 “세션을 가리키는 값”(세션 ID)** 이고, **그 사람의 이메일·이름 같은 정보는 그 값 안에 들어 있지 않습니다.**

### 1. 브라우저에 있는 것 (쿠키 등)

- 카카오에 한 번 로그인해 두면, 카카오가 **쿠키**에 넣어 두는 것은 보통 **세션 ID(세션 식별자)** — 긴 랜덤 문자열 같은 “이 로그인 세션을 가리키는 값” (예: `sid=abc123...` 형태).
- 이 값 **자체**에는 이메일, 전화번호, 이름, 비밀번호 같은 **개인 정보가 들어 있지 않고**, “어떤 로그인 세션인지”만 구분하는 **키** 역할만 함.

즉, **“어떤 정보로 파악하냐”**라고 하면 → **그 “세션을 가리키는 값(세션 ID)”** 로 파악하는 것이고, 그 값만 보면 사람이 누구인지는 알 수 없으며, **카카오 서버가 그 키를 보고 “누구”인지 찾아냄.**

### 2. 카카오 서버 쪽에서 하는 일

- 브라우저가 카카오 페이지를 열 때 **저 쿠키(세션 ID)** 를 같이 보냄.
- 카카오 **서버**는 그 **세션 ID**를 받아서, 자기가 가진 **세션 저장소**(DB/캐시 등)에서 “이 세션 ID = 이 카카오 계정(내부 user_id 등)” 이라고 **매핑**해 둔 것을 찾음.
- 그래서 “이 사람이 누구인지”를 아는 것은, **클라이언트(브라우저)** 는 “세션 ID”라는 **정보 한 개**만 갖고 있고, **서버**는 그 세션 ID에 연결된 **계정 정보(누구인지)** 를 이미 알고 있어서 그걸로 파악하는 구조임.

### 요약

- **“기존 로그인 정보(세션/쿠키)로 어떤 정보로 파악하냐?”**  
  → **쿠키에 들어 있는 “세션 ID”** 로 파악함.
- **“실제로 누구인지(이메일·이름 등)”** 는 그 세션 ID에 대응되는 **서버 쪽 저장 정보** 로 알아내는 방식임.

---

## 2026-02-28 (카카오 로그인 – 계정 입력 없이 로그인되는 이유)

**Q. 카카오톡 로그인 후 계정을 입력 안 했는데도 로그인되는 이유가 뭐야?**

**그 시점에 이미 카카오에 로그인된 상태**이기 때문입니다.

### 카카오 로그인 흐름

1. 우리 사이트에서 **「카카오 로그인」** 클릭
2. 브라우저가 **카카오 로그인/동의 페이지**로 이동
3. 카카오 화면에서 **이미 로그인됐는지 / 안 됐는지**에 따라 다르게 동작함

### “계정 입력 없이” 로그인되는 경우

- **이미 그 브라우저(또는 카카오 쪽)에서 카카오에 로그인되어 있는 경우**
  - 카카오가 **기존 로그인 정보(세션/쿠키)** 로 “이 사람이 누구인지” 알고 있음
  - 그래서 **이메일·비밀번호 입력란을 안 보여주고**, **“○○ 앱이 닉네임·프로필 이미지를 사용하려고 합니다. 동의할까요?”** 같은 **동의 버튼만** 보여줌
  - 사용자는 **동의(또는 로그인)** 버튼만 누르면 됨 → 우리 사이트로 돌아오면서 로그인 완료

즉, **“우리 사이트나 카카오 페이지에서 ID/비밀번호를 입력하지 않았다”**고 느끼는 이유는, **계정 정보는 “지금 입력”한 게 아니라 “이미 카카오에 로그인된 상태”로 쓰이기 때문**입니다. 카카오가 **기존 세션**으로 사용자를 알아보고, 우리 앱에는 “이 사용자 허용할지”만 물어보는 구조라서, **추가로 계정을 입력하지 않아도** 로그인이 됩니다.

### “계정 입력”이 나오는 경우

- **카카오에 로그인되어 있지 않은 경우**
  - 카카오 로그인 페이지에서 **이메일(또는 전화번호)·비밀번호** 입력
  - 그다음 앱 동의 화면에서 **동의** → 이때만 “계정을 입력했다”고 느끼게 됨

### 요약

**카카오톡 로그인 후 “계정을 입력 안 했는데” 로그인되는 이유**는, **그 브라우저/환경에서 이미 카카오에 로그인되어 있어서**, 카카오가 사용자를 자동으로 인식하고 **동의만 받으면 되기 때문**입니다. 그래서 우리 사이트나 카카오 페이지에서 ID/비밀번호를 다시 입력하지 않아도 로그인이 완료됩니다.

---

## 2026-02-28 (카카오 로그인 버튼 클릭 시 전송 데이터)

**Q. 로그인 버튼만 눌러도 카카오 API에 요청이 가는 것 같은데, 어떤 데이터를 전송하는 거야? 사용자는 우리 사이트에서 아무것도 입력한 적이 없다.**

### ① 우리 서버 → 브라우저 → 카카오 (리다이렉트)

버튼을 누르면 브라우저가 먼저 **우리 서버** `/oauth2/authorization/kakao` 로 갔다가, 곧바로 **카카오 로그인 페이지**로 리다이렉트됩니다. 이때 **URL에 붙어서 카카오로 전달되는 값**은 전부 **앱 설정값**이며, 사용자가 입력한 적 없는 것들입니다.

| 전달 데이터 | 설명 | 누가 정한 값 |
|-------------|------|--------------|
| **client_id** | 우리 앱의 카카오 REST API 키 | 앱 설정(application.yml 등) |
| **redirect_uri** | 로그인 후 돌아올 우리 사이트 주소 | 앱 설정 |
| **response_type** | `code` (인증 코드 방식) | OAuth2 규격 |
| **scope** | 요청 권한 (예: 프로필 닉네임, 프로필 이미지) | 앱 설정 |
| **state** | 요청마다 생성하는 랜덤 값 (CSRF 방지) | 우리 서버가 자동 생성 |

즉, **“로그인 버튼만 눌렀을 때” 우리가 카카오로 보내는 건 위와 같은 “어떤 앱이, 어떤 주소로 돌아오게 해달라고 요청하는지”에 대한 정보**이며, **사용자가 우리 사이트에서 입력한 ID·비밀번호 같은 건 이 단계에서는 전혀 전송되지 않습니다.**

### ② 사용자 “입력”이 나오는 곳 (카카오 쪽)

리다이렉트되면 **카카오가 제공하는 로그인/동의 화면**이 뜹니다.

- 이미 카카오에 로그인된 상태면: “○○ 앱이 닉네임·프로필 이미지를 사용하려고 합니다. 동의할까요?” 같은 **동의 버튼**만 누르면 됨. → 우리 사이트에서는 입력한 적 없음.
- 카카오에 로그인 안 된 상태면: **카카오 로그인 화면**에서 이메일/비밀번호 등을 입력하게 됨. → 그 입력은 **카카오 페이지에서만** 이루어지고, 그 값은 **카카오 서버로만** 전달되며, 우리 서버에는 ID/비밀번호가 전달되지 않음.

### 요약

- **우리가 카카오로 보내는 것**: “우리 앱(client_id)이 로그인을 시도하고, 로그인 끝나면 redirect_uri로 돌려보내고, scope 권한을 쓰겠다”는 **앱 정보 + 리다이렉트/권한 설정**.
- **사용자가 우리 사이트에서 입력한 것**: **없음.** 카카오 화면에서만 (이미 로그인됐으면 “동의” 한 번, 안 됐으면 카카오 계정으로 로그인) 하며, 그 입력은 카카오 쪽으로만 감.

---

## 2026-02-28 (KOE237 – 계정 없이도 발생하는 이유)

**Q. 카카오 KOE237이 발생하는데, 계정을 입력하지 않아도 발생하는 이유가 뭐야?**

제한이 **“어떤 계정이 로그인했는지”**가 아니라 **“우리 앱이 카카오 토큰 API를 호출한 횟수”**에 걸리기 때문입니다.

### KOE237이 나오는 시점

- 사용자가 카카오 로그인 화면에서 동의하고 **우리 서버로 돌아온 직후**
- 우리 서버가 **authorization code → access token** 을 받기 위해 **카카오 토큰 API**를 호출할 때

이때 카카오는 “어떤 카카오 계정인지”를 기준으로 세지 않고, **“이 앱(client_id)이 토큰 API를 몇 번 호출했는지”**만 셉니다.

### 카카오 쿼터(제한) 기준

[카카오 쿼터 문서](https://developers.kakao.com/docs/latest/ko/getting-started/quota#rate-limit) 기준:

- **액세스 토큰**: **10분당 20개** (토큰 발급 요청 기준)
- **리프레시 토큰**: 60분당 30개

“20개”는 **“사용자 1명당”이 아니라**, 우리 앱이 카카오에 보내는 **토큰 발급 요청 횟수**로 적용됩니다.

### 그래서 “계정 없이”도 나오는 이유

- **토큰 요청**은 “카카오 계정 입력”이 끝난 **다음 단계**에서 일어남. 사용자가 카카오에서 동의하고 우리 사이트로 리다이렉트되면, 그때 우리 서버가 카카오 토큰 API를 **한 번** 호출함.
- 제한은 **그 토큰 API 호출 횟수**에 걸려 있어서, 같은 사람이 로그인 버튼을 여러 번 눌러서 여러 번 콜백이 오거나, 로그인 → 로그아웃 → 다시 로그인을 반복하거나, 테스트로 짧은 시간에 여러 번 로그인을 시도하면 **10분 안에 20번**만 넘어도 KOE237이 남.
- 이 과정에서는 “어떤 계정으로 로그인했는지”를 **쿼터 계산에 쓰지 않기 때문에**, **계정을 입력하지 않아도**(또는 여러 계정이 섞여 있어도) **요청 횟수만 많으면** KOE237이 발생함.

### 요약

| 구분 | 설명 |
|------|------|
| **제한 대상** | “우리 앱이 카카오 **토큰 API**를 호출한 횟수” (대략 10분당 20회 수준) |
| **계정과의 관계** | 제한은 “어떤 카카오 계정인지”와 무관하게 **요청 횟수**만으로 적용됨 |
| **대응** | 10분 정도 지난 뒤에 다시 시도하거나, 로그인/로그아웃 반복을 줄이면 됨 |

---

## 2026-02-28 (카카오 로그인 – 토큰 요청 시점 정리)

**Q. 처음 로그인 시에만 토큰 요청을 하고, 이후 요청마다 토큰 요청으로 로그인 확인하는가?**

- **아니요.** 처음 로그인할 때만 카카오에 토큰 요청을 하고, 이후 요청에서는 카카오에 토큰 요청을 하지 않습니다.

### 처음 로그인할 때 (한 번만)

1. 사용자가 "카카오 로그인" 클릭 → `/oauth2/authorization/kakao` 요청
2. 서버가 카카오 로그인 페이지로 리다이렉트
3. 사용자가 카카오에서 동의 후 우리 서버로 돌아옴 (authorization code 포함)
4. **이때 한 번** 우리 서버가 카카오 **토큰 API**를 호출해 code → access token(·refresh token) 교환
5. Spring Security가 로그인 처리 후 **세션에 인증 정보(OAuth2User 등) 저장**
6. 브라우저에는 **JSESSIONID** 쿠키만 내려감

→ 여기서 말하는 "토큰 요청"은 **이 한 번**만 일어납니다.

### 그 다음 요청들 (로그인 유지 중)

- 브라우저가 요청할 때마다 **JSESSIONID** 쿠키만 보냄
- 서버는 **세션 저장소에서** 그 세션에 담긴 인증 정보를 꺼내 씀
- **카카오 토큰 API에는 요청하지 않음**
- 따라서 "매 요청마다 토큰 요청으로 로그인 확인"하는 구조가 **아닙니다.**

### 요약

| 시점           | 카카오 토큰 요청      | 우리 서버에서 하는 일     |
|----------------|------------------------|---------------------------|
| 처음 로그인 시 | 1번 (code → token)    | 세션에 인증 정보 저장     |
| 이후 요청마다  | 하지 않음              | 세션 쿠키로 인증 여부만 확인 |

429(토큰 요청 제한)는 **로그인 시도**(또는 로그인/로그아웃 반복)가 짧은 시간에 많이 있을 때만 발생하며, 한 번 로그인한 뒤 일반적으로 페이지를 돌아다닐 때는 토큰 요청이 반복되지 않습니다.

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
