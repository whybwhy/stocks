# Git 연동 및 Render 배포 가이드

이 문서는 이 프로젝트를 Git 저장소에 올리고, Render에서 Docker 기반으로 배포하는 방법을 단계별로 설명합니다.

---

## 1. 사전 준비

- **Git** 설치 및 사용자 설정 (`git config user.name`, `user.email`)
- **GitHub** 계정
- **Render** 계정 (https://render.com, GitHub 로그인 가능)
- 로컬에서 **Docker** 설치 (선택, 로컬에서 이미지 빌드 확인용)

---

## 2. Git 저장소 초기화 및 GitHub 연동

### 2.1 프로젝트 디렉터리에서 Git 초기화

```bash
cd /Users/crush/Documents/stocks
git init
```

### 2.2 로컬 설정 적용 (최초 1회)

시크릿이 포함된 파일은 커밋되지 않도록 이미 `.gitignore`에 포함되어 있습니다.

- `application-local.yml` (로컬용 설정) — 커밋되지 않음
- `build/`, `.gradle/`, IDE 설정 등 — 제외됨

### 2.3 첫 커밋

```bash
git add .
git status   # application-local.yml 등이 빠져 있는지 확인
git commit -m "Initial commit: Spring Boot + Supabase + Kakao OAuth + Thymeleaf"
```

### 2.4 GitHub에 저장소 생성

1. GitHub → **New repository**
2. Repository name: 예) `stocks`
3. Public 선택, **Create repository** (README, .gitignore 추가 안 함)

### 2.5 원격 저장소 연결 및 푸시

```bash
git remote add origin https://github.com/YOUR_USERNAME/stocks.git
git branch -M main
git push -u origin main
```

`YOUR_USERNAME`을 본인 GitHub 사용자명으로 바꾸세요.

---

## 3. Render 배포 준비

### 3.1 환경 변수 정리

배포 시 다음 값들은 **Render 대시보드의 Environment**에서 설정합니다. (코드에 넣지 마세요.)

| 변수명 | 설명 | 예시 |
|--------|------|------|
| `SUPABASE_URL` | Supabase 프로젝트 URL | `https://xxxx.supabase.co` |
| `SUPABASE_ANON_KEY` | Supabase anon key | `eyJ...` |
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 | |
| `KAKAO_CLIENT_SECRET` | 카카오 시크릿 | |
| `KAKAO_REDIRECT_URI` | (선택) 배포 URL 기준 리다이렉트 URI | `https://your-app.onrender.com/login/oauth2/code/kakao` |

### 3.2 카카오 개발자 콘솔 설정

1. https://developers.kakao.com → **내 애플리케이션**
2. 해당 앱 → **앱 설정** → **플랫폼** → **Web**
   - **사이트 도메인**: `https://your-app.onrender.com` (Render 배포 후 확정된 URL로 변경)
3. **카카오 로그인** → **Redirect URI**에 다음 추가:
   - `https://your-app.onrender.com/login/oauth2/code/kakao`

배포 전에는 Render에서 할당될 URL을 모를 수 있으므로, 첫 배포 후 URL을 확인한 뒤 위 항목을 수정해도 됩니다.

---

## 4. Render에서 Web Service 생성 (Docker 배포)

### 4.1 새 서비스 생성

1. https://dashboard.render.com 로그인
2. **New +** → **Web Service**
3. **Connect a repository**에서 방금 푸시한 GitHub 저장소(`stocks`) 선택 후 **Connect**

### 4.2 서비스 설정

- **Name**: 예) `stocks` (원하는 이름)
- **Region**: 원하는 지역 (예: Singapore)
- **Branch**: `main`
- **Runtime**: **Docker**
- **Instance Type**: Free tier 선택 가능 (무료 플랜은 슬립 시 콜드 스타트 있음)

### 4.3 환경 변수 등록

**Environment** 탭에서 다음을 추가합니다.

| Key | Value |
|-----|--------|
| `SUPABASE_URL` | Supabase 대시보드의 Project URL |
| `SUPABASE_ANON_KEY` | Supabase Project Settings → API → anon public |
| `KAKAO_CLIENT_ID` | 카카오 REST API 키 |
| `KAKAO_CLIENT_SECRET` | 카카오 Client Secret |
| `KAKAO_REDIRECT_URI` | `https://<서비스이름>.onrender.com/login/oauth2/code/kakao` |

서비스 이름을 `stocks`로 만들었다면:

- `KAKAO_REDIRECT_URI` = `https://stocks.onrender.com/login/oauth2/code/kakao`

### 4.4 빌드·실행 설정 (Docker 사용 시)

- **Docker** 런타임을 선택했으므로 **Dockerfile**을 자동으로 사용합니다.
- Build Command / Start Command는 비워 두면 됩니다 (Dockerfile의 `ENTRYPOINT` 사용).

### 4.5 생성 완료

**Create Web Service**를 누르면 Render가 다음을 수행합니다.

1. 저장소 클론
2. `Dockerfile` 기준 이미지 빌드
3. 컨테이너 실행
4. `https://<서비스이름>.onrender.com` URL 부여

빌드 로그에서 에러가 없으면, 배포 URL로 접속해 `/health`, `/stocks` 등을 확인할 수 있습니다.

---

## 5. 배포 후 확인

- **헬스 체크**: `https://your-app.onrender.com/health`
- **주식 목록**: `https://your-app.onrender.com/stocks` (로그인 정책에 따라 리다이렉트될 수 있음)
- **카카오 로그인**: 배포 URL을 카카오 Redirect URI에 등록한 뒤 위 URL에서 로그인 플로우 테스트

---

## 6. 이후 배포 (자동 배포)

- **Auto-Deploy**: 기본적으로 `main` 브랜치에 푸시할 때마다 Render가 자동으로 빌드·배포합니다.
- 수동 배포: Render 대시보드 → 해당 서비스 → **Manual Deploy** → **Deploy latest commit**

```bash
git add .
git commit -m "기능 수정 또는 버그 수정"
git push origin main
```

---

## 7. 로컬 개발 시 설정 (참고)

배포용 설정은 환경 변수로만 하고, 로컬에서는 커밋되지 않는 `application-local.yml`을 사용할 수 있습니다.

1. `src/main/resources/application-local.example.yml`을 복사하여 `application-local.yml` 생성
2. Supabase URL/키, 카카오 client id/secret 등을 실제 값으로 채움
3. 실행 시 프로파일 지정:  
   `--spring.profiles.active=local`  
   또는 IDE Run Configuration에서 **Active profiles**에 `local` 입력

이렇게 하면 로컬에서는 `application-local.yml`이 적용되고, Render에서는 환경 변수만으로 동작합니다.
