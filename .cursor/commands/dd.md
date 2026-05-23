---
description: 목표가 텍스트 → price_alerts + price_alerts_log 이중 INSERT SQL (멱등 로그 허용)
argument-hint: "[붙여넣은 종목·가격·메모 블록 전체]"
---

# /dd — 일일 목표가 이중 적재 스크립트 생성

사용자가 채팅에 붙여 넣은 **원문 블록**(`$ARGUMENTS` 또는 이 메시지 아래 줄 전체)을 해석하여, 새 SQL 파일 **한 개**만 생성한다.

## 출력 파일 이름

`src/main/resources/YYYYMMDD_price_alerts_<영문설명>_dual.sql`

- **`YYYYMMDD`** = 오늘이 속한 로컬(Asia/Seoul) 달력 날짜 8자리.
- **`_dual`** = `price_alerts` 멱등 블록 + `price_alerts_log` 순차 블록 둘 다 포함한다는 뜻.
- 과거 패턴 참고: `20260513_price_alerts_chartboy_batch.sql` 과 같은 **날짜 prefix** 규약.

파일 안 상단 주석으로 **근거가 된 사용자 원문 줄들** 요약 또는 인용 블록을 짧게 남긴다.

---

## 블록·작성자 판별 (`price_alerts_log.posted_by`만 해당)

각 **논리적 블록**을 나누고, 줄의 **처음 문자**로 구분한다.

| posted_by 값 | 규칙 |
|----------------|------|
| **`CHARTBOY`** | 해당 줄 또는 블록의 첫 유의미 줄이 **`✔` 또는 `✔️`(U+2714)·`✔︎`** 등으로 시작. |
| **`HYONYHYONY`** | 블록이 **`🌈`(U+1F308)** 무지개 이모지로 시작하는 줄로 시작하면서, 위 CHARTBOY 규칙에 해당하지 않음. 멤버쉽 정리 헤더 등이 여기 포함. |

- 한 줄에 무지개가 있더라도 **맨 앞이 ✔ 라면 그 줄은 CHARTBOY 블록**으로 본다(우선 ✔ 블록).
- 위 둘에 안 맞는 줄은 작성자별로 들어가지 말고, 별도 검토용으로 처리한다.

`price_alerts.source` 알람 행에는 **항상** `'CHARTBOY'` 문자열만 사용한다（기존 앱 규격).

---

## SQL 생성 규칙

### 1) `public.price_alerts`

- 패턴은 `price_alerts_insert_chartboy_memo_batch_*.sql` 계열과 동일.
- `INSERT INTO … SELECT … FROM (VALUES …) AS v … WHERE NOT EXISTS (symbol + target_price + condition 매칭)`.
  - 같은 금액에 `ABOVE`/`BELOW`가 다를 수 있으므로 **condition 포함**해서 멱등한다.
- **컬럼**: `market, stock_code, symbol, target_price, condition, label, source`
- **`created_at` 은 절대 넣지 않는다**（DB 기본값·현재 시각）。
- **`source`** = 문자열 **`'CHARTBOY'`** 고정。
- 라벨: 원문·메모 줄을 한글로 간결하게 정리. 날짜 태그는 관례에 맞춤（예 `주말 멤버십 5·21`）。
- 금액은 **실수 채 번호·쉼표** 정규화。
- **`BELOW`**(손절라인 등): 원문에 손절·이탈 명시 시에만 사용.

### 2) `public.price_alerts_log`

- 순수 `INSERT … VALUES (...), (...);`
- **`created_at` 없음**（기본값 `now()`）。
- **행마다 반드시** `posted_by` 를 `CHARTBOY` 또는 **`HYONYHYONY`** 로 지정한다.
- **중복 검사 금지**（멱등 `WHERE NOT EXISTS` 사용 안 함）.

### 3) 종목 매핑 — **추측 금지**

1. 이름이 들어오면 **`src/main/resources`** 아래 과거 배치 파일·종목 헤더 주석을 **우선** 검색한다.
2. 레포에 없으면 **확실한 근거(거래소/공시)**가 있을 때만 코드 매핑.
3. **비슷한 이름으로 바꿔 넣기 금지**（예:「나노」→「나노팀」임의 매핑 금지）。
4. 미확정 종목은 SQL에 넣지 않고, 응답에 **`### 미확정·미매칭 종목`** 섹션으로만 나열.

---

## 응답 본문 끝 (고정 문구·slug 치환)

작업 후 아래 블록을 출력한다.`{slug}` 는 `application.yml`의 `price-alert-public-slug` 기본값 문자열로 바꿔 채운다.

법적이슈 or 내부분란 or 차트보이 or 효니효니님이 정리 부분 내리라하면 내립니다.  
https://stocks-ser4.onrender.com/{slug}  

❤️ 불미스러운 일이 발생하지 않도록 여러가지 방향으로 개선했습니다. 다소 불편해졌더라도 양해부탁드려요. 사용하시는 분들도 불편한 펌에 대해 주의 부탁드려요 멤버십안에서만 봤으면 좋겠습니다 🙏
