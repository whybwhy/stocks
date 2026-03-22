# 경제 이벤트 캘린더 알림 시스템

FOMC 일정, CPI, PPI, 고용지표 등 미국 주요 경제 이벤트 발표일을 관리하고,  
**D-1(전일)**, **D-0(당일)** 에 텔레그램으로 사전 알림을 발송하는 기능.

---

## 아키텍처

```
┌─────────────────┐     ┌──────────────────┐
│  FOMC (수동)     │     │  FRED API (자동)  │
│  연 8회 INSERT   │     │  CPI / PPI / NFP │
└────────┬────────┘     └────────┬─────────┘
         │                       │
         ▼                       ▼
   ┌─────────────────────────────────┐
   │   Supabase economic_events     │
   │   (event_date, event_name,     │
   │    notified_d1, notified_d0)   │
   └──────────────┬──────────────────┘
                  │
    ┌─────────────┴──────────────┐
    │  EconomicEventScheduler    │
    │  매일 08:00 KST            │
    │  매주 월 08:05 KST (FRED)  │
    └─────────────┬──────────────┘
                  │
                  ▼
          ┌──────────────┐
          │  텔레그램 알림  │
          │  D-1 / D-0   │
          └──────────────┘
```

---

## 데이터 소스

### 1. FOMC (수동 등록)

- 연초에 연준이 공개하는 8회 일정을 직접 INSERT
- 출처: https://federalreserve.gov/monetarypolicy/fomccalendars.htm
- SQL 파일: `src/main/resources/economic_events_fomc_2026.sql`

| 날짜 | 비고 |
|---|---|
| 01/28 | |
| 03/18 | 점도표 + 기자회견 |
| 04/29 | |
| 06/17 | 점도표 + 기자회견 |
| 07/29 | |
| 09/16 | 점도표 + 기자회견 |
| 10/28 | |
| 12/09 | 점도표 + 기자회견 |

### 2. CPI / PPI / 고용지표 (FRED API 자동)

FRED API `fred/release/dates` 엔드포인트로 미래 발표일을 자동 조회.

| 지표 | FRED release_id | 설명 |
|---|---|---|
| CPI | 10 | 소비자물가지수 |
| PPI | 46 | 생산자물가지수 |
| 고용지표 | 50 | 비농업 고용 (NFP) |

- API 문서: https://fred.stlouisfed.org/docs/api/fred/release_dates.html
- 핵심 파라미터: `include_release_dates_with_no_data=true` (미래 날짜 포함)
- 갱신 주기: **매주 월요일 08:05 KST** 자동 실행

---

## 스케줄 동작

| 시각 (KST) | 주기 | 동작 |
|---|---|---|
| 매일 08:00 | 매일 | D-1(내일), D-0(오늘) 이벤트 텔레그램 알림 발송 |
| 매주 월 08:05 | 주 1회 | FRED API에서 CPI/PPI/고용지표 미래 발표일 동기화 |

---

## 텔레그램 메시지 형식

```
[내일] FOMC 정례회의 (점도표 + 기자회견)
날짜 : 2026-03-18 (수)
```

```
[오늘] CPI 소비자물가지수 발표
날짜 : 2026-04-10 (목)
소비자물가지수 (CPI) 발표
```

---

## DB 테이블 구조

테이블: `public.economic_events`

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | bigserial | PK |
| event_date | date | 이벤트 날짜 |
| event_name | text | FOMC, CPI, PPI, 고용지표 |
| description | text | 상세 설명 |
| source | text | MANUAL / FRED |
| fred_release_id | integer | FRED release_id (자동 갱신용) |
| notified_d1 | boolean | D-1 알림 발송 완료 여부 |
| notified_d0 | boolean | D-0 알림 발송 완료 여부 |
| created_at | timestamptz | 생성일시 |

UNIQUE 제약: `(event_date, event_name)` — 같은 날짜+이벤트 중복 방지.

---

## 파일 구조

```
src/main/resources/
├── economic_events.sql                  # 테이블 DDL
├── economic_events_fomc_2026.sql        # 2026 FOMC 초기 데이터

src/main/java/com/example/stocks/fred/
├── FredApiProperties.java               # FRED API key 설정
├── FredApiConfig.java                   # RestClient Bean
├── FredReleaseFetcher.java              # FRED API 호출
├── EconomicEventDto.java                # Supabase DTO
├── EconomicEventService.java            # CRUD + 갱신 + 알림
└── EconomicEventScheduler.java          # cron 스케줄러
```

---

## 설정

### application.yml

```yaml
fred:
  api-key: ${FRED_API_KEY:}

economic-event:
  enabled: ${ECONOMIC_EVENT_ENABLED:true}
```

### 환경변수 (Render 등)

| 키 | 값 | 필수 |
|---|---|---|
| FRED_API_KEY | FRED API key | O (자동 갱신 사용 시) |
| ECONOMIC_EVENT_ENABLED | true/false | X (기본 true) |

---

## 초기 설정 절차

1. Supabase SQL Editor에서 `economic_events.sql` 실행 (테이블 생성)
2. Supabase SQL Editor에서 `economic_events_fomc_2026.sql` 실행 (FOMC 초기 데이터)
3. FRED API Key 발급: https://fred.stlouisfed.org/docs/api/api_key.html (무료)
4. 환경변수 `FRED_API_KEY` 설정
5. 앱 배포/재시작 → 매주 월요일 자동으로 CPI/PPI/고용지표 발표일 동기화

---

## FRED API Key 발급

1. https://fred.stlouisfed.org/docs/api/api_key.html 접속
2. 무료 계정 생성 후 API Key 발급
3. 일 120건 호출 제한 (주 1회 3건 호출이면 충분)
