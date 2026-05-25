---
description: 목표가 텍스트 → price_alerts·price_alerts_log 이중 SQL 생성(기본). ‘갱신해줘’ 시에만 Supabase 적재
argument-hint: "[종목·가격 블록] 선택: 끝에 «갱신해줘» 있으면 DB 반영까지"
---

# /dd — 일일 목표가 이중 적재

## 실행 모드 분기 (**반드시 먼저 판별**)

| 사용자 표현 예 | 하는 일 |
|----------------|--------|
| `/dd` + 종목 블록 **만** 또는 `/dd …` 에 **갱신·반영·Supabase 적용** 요청이 **없음** | **`YYYYMMDD_price_alerts_*_dual.sql` 파일 하나만 생성**한다. **`price_alerts` / `price_alerts_log` 테이블에 실제 INSERT·REST 호출 금지.** |
| 동일 종목 블록에 대해 **「갱신해줘」「Supabase 에 반영」「DB 에 넣어줘」「실제 적용」** 등 **명시적인 DB 적재 의사**가 있음 *(같은 턴부터 직후 턴까지 이어져도 됨)* | (1) 먼저 또는 이미 만들어진 `*_dual.sql` 을 확정하고, (2) 그 SQL이 의미하는 **모든 행**을 순서대로 **`public.price_alerts`**(멱등 규칙 준수)와 **`public.price_alerts_log`**(멱등 없음, 중복 허용) 에 **실제 적재**한다. |

- **`/dd {종목정보}`** 처럼 **블록만** 주고 **갱신 문구가 없으면 Supabase·DB에 적용하지 않는다.**
- **갱신 모드일 때만** `application-local.yml` 등 사용자 환경의 Supabase 설정을 사용할 수 있다. 적재 결과(성공 건수·SKIP·오류)를 짧게 요약한다.

---

## 출력 파일 이름

`src/main/resources/YYYYMMDD_price_alerts_<영문설명>_dual.sql`

- **`YYYYMMDD`** = 오늘이 속한 로컬(**Asia/Seoul**) 달력 날짜 8자리.
- **`_dual`** = 한 파일 안에 **`price_alerts` 멱등 블록** + **`price_alerts_log` INSERT 블록** 순서로 둘 다 포함.

파일 상단 주석에 **근거가 된 사용자 원문**을 짧게 남긴다.

---

## 블록·작성자 (`price_alerts_log.posted_by` 만)

| posted_by 값 | 규칙 |
|----------------|------|
| **`CHARTBOY`** | 블록의 첫 유의미 줄이 **`✔`·`✔️`(U+2714)·`✔︎`** 등으로 시작. |
| **`HYONYHYONY`** | 블록이 **`🌈`(U+1F308)** 무지개로 **시작**하고, 동시에 맨 앞이 ✔ 계열이 **아님**. |

- 줄 맨 앞이 ✔ 이면 **CHARTBOY 우선**(같은 줄에 무지개가 있어도 ✔ 블록).
- 위에 해당 안 하면 해당 줄은 작성자 블록에 넣지 말고 검토 목록 처리.

**`price_alerts.source`** 는 **항상** `'CHARTBOY'` 문자열 고정（앱 규격）.

---

## SQL 규칙

### 1) `public.price_alerts`

- `INSERT … SELECT … FROM (VALUES …) WHERE NOT EXISTS` — 멱등 키: **`symbol` + `target_price` + `condition`**.
- 컬럼: `market, stock_code, symbol, target_price, condition, label, source`
- **`created_at` 포함 금지**（DB 기본값）.
- `source = 'CHARTBOY'` 고정.
- 손절 등은 원문 근거 있을 때만 **`BELOW`**.

### 2) `public.price_alerts_log`

- `INSERT … VALUES` 다중 행. **`created_at` 없음**.
- 각 행 `posted_by` 는 `CHARTBOY` 또는 `HYONYHYONY`.
- **`WHERE NOT EXISTS` 사용 안 함**（항상 새 로그 줄로 쌓임）.

### 3) 종목 매핑

1. 레포 `src/main/resources` 기존 배치 **주석의 종목코드** 우선.
2. 없을 때만 **확실한 외부 근거**.
3. **유사 이름으로 대체 금지**（예: 나노 → 나노팀 임의 매핑 금지）.
4. 미확정은 SQL에 넣지 않고 응답 **`### 미확정·미매칭 종목`** 에만 목록.

---

## 갱신 모드에서의 적재 구현 메모

- **`price_alerts`**: 기존 멱등과 동일하게 `symbol`·`target_price`·`condition` 기준으로 이미 있으면 SKIP.
- **`price_alerts_log`**: 생성된 VALUES 마다 POST (중복 허용).
- 스크립트에서 직접 실행할 때는 민감정보를 채팅에 노출하지 말 것.

## 공개 확인 (`price_alerts_log`)

적재 후 사용자용 블록 복사 뷰(작성자별 최신 적재일·서울): `/{slug}/log/new`

---

`application.yml` 의 `price-alert-public-slug` 기본값으로 `{slug}` 를 바꿔 출력.

법적이슈 or 내부분란 or 차트보이 or 효니효니님이 정리 부분 내리라하면 내립니다.  
https://stocks-ser4.onrender.com/{slug}  

❤️ 불미스러운 일이 발생하지 않도록 여러가지 방향으로 개선했습니다. 다소 불편해졌더라도 양해부탁드려요. 사용하시는 분들도 불편한 펌에 대해 주의 부탁드려요 멤버십안에서만 봤으면 좋겠습니다 🙏
