# 알람·스케줄 정리

앱에서 텔레그램·폴링이 동작하는 시각과 조건을 코드 기준으로 정리했습니다.

**시간대:** Cron에 `zone = "Asia/Seoul"`이 지정된 작업은 **한국 표준시(KST)** 기준입니다.  
`fixedDelay`만 사용하는 작업은 **서버 JVM 타임존**에 따라 트리거 시각이 달라질 수 있으나, **장 시간 판단·메시지 내용**은 코드에서 **KST**를 사용합니다.

---

## 1. 텔레그램 고정 시각 (Cron, KST)

| 구분 | 시각 (KST) | 요일 | 내용 | 설정 |
|------|------------|------|------|------|
| 경제 이벤트 | **매일 08:00** | 매일 | FOMC/CPI/PPI/고용 등 D-1·당일 알림 | `economic-event.enabled` (기본 `true`) |
| 경제 이벤트 | **매주 월 08:05** | 월요일만 | FRED API 발표일 동기화 | 위와 동일 |
| 테마 | **08:30** | 평일 | 장 시작 전 브리핑 (전일 기준 테마) | `theme.enabled` (기본 `true`) |
| 테마 | **10:00** | 평일 | 오전장 테마 (실시간) | 위와 동일 |
| 테마 | **15:40** | 평일 | 마감 테마 정리 (당일 최종) | 위와 동일 |

**관련 클래스**

- `fred/EconomicEventScheduler.java`
- `theme/ThemeScheduler.java`

---

## 2. 주기 폴링 (`fixedDelay`)

| 구분 | 간격 (기본값) | 활성 조건 | 동작 |
|------|---------------|-----------|------|
| 가격 알람 (REST) | 이전 실행 종료 후 **30초** (`kis.rest-check-interval-seconds`) | `kis.mode=rest` | `price_alerts` + 보유종목(`holdings`) 체크 → 조건 충족 시 텔레그램 |
| 시장 지수 | 이전 실행 종료 후 **120초** (`kis.market-index.interval-seconds`) | `kis.market-index.enabled=true` (기본) | 코스피/코스닥/야간선물/나스닥100/S&P500 시장현황 텔레그램 (**운영 시간 내**만 전송) |
| KIS WebSocket 관리 | 이전 실행 종료 후 **60초** | `kis.mode=websocket` (기본) | 장중 WebSocket 연결·구독 유지, 실시간 체결가로 목표가 알람 |

**관련 클래스**

- `alert/AlertScheduler.java`
- `kis/MarketIndexScheduler.java`
- `kis/KisWebSocketClient.java`

---

## 3. 가격·지수가 실제로 도는 장시간 (코드 기준 KST)

### 가격 알람 (`AlertService`)

| 시장 | 장시간 (KST) |
|------|----------------|
| 국내 (KR) | 평일 **08:50 ~ 15:35** |
| 미국 (US) | 대략 **22:00 ~ 익일 07:00** (토요일은 **00:00 ~ 07:00**만), **일요일 제외** |

### 시장 지수 (`MarketIndexScheduler.isOperatingTime`)

| 구간 | KST |
|------|-----|
| 정규장 | 평일 **09:00 ~ 15:35** |
| 야간 | 평일 **18:00 ~ 23:59**, **00:00 ~ 05:00** |
| 금→토 | 토요일 **00:00 ~ 05:00** |
| 일요일 | 없음 |

### WebSocket 실시간 알람 (`KisWebSocketClient`)

- 평일 **08:50 ~ 15:35** (국내 장 기준 연결·구독)

---

## 4. 모드 요약

| `kis.mode` | 가격 알람 방식 |
|------------|----------------|
| `websocket` (기본) | `KisWebSocketClient` — 장중 실시간 체결가, `AlertScheduler`는 **비활성** |
| `rest` | `AlertScheduler` — 주기적 REST 폴링 + `HoldingService.checkHoldings()` |

---

## 5. `application.yml` 주요 키

```yaml
kis:
  mode: ${KIS_MODE:rest}                    # websocket | rest
  rest-check-interval-seconds: 30             # REST 폴링 간격
  market-index:
    enabled: true
    interval-seconds: 120                   # 지수 알림 간격

economic-event:
  enabled: true

theme:
  enabled: true
```

환경변수 예: `KIS_MODE`, `KIS_REST_CHECK_INTERVAL_SECONDS`, `KIS_MARKET_INDEX_ENABLED`, `KIS_MARKET_INDEX_INTERVAL_SECONDS`, `ECONOMIC_EVENT_ENABLED`, `THEME_ENABLED`.

---

## 6. 한 줄 요약

- **고정 시각:** 경제 **08:00**·**월 08:05**, 테마 **평일 08:30·10:00·15:40** (KST).
- **반복:** REST면 **~30초마다** 목표가·보유종목, 지수는 **~2분마다** (장·설정 조건 충족 시).
- **WebSocket(기본):** **~60초마다** 연결 관리 + **장중 실시간** 목표가 체크.

---

*문서 갱신 시 코드와 대조: `AlertScheduler`, `MarketIndexScheduler`, `EconomicEventScheduler`, `ThemeScheduler`, `KisWebSocketClient`, `AlertService`.*
