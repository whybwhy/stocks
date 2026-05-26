-- 20260527 ✅ 매수타점 체크(차트보이) 이중 적재 — dual · Asia/Seoul
-- 【사용자 원문】✅ 매수타점 체크 블록(✔·가격 줄)
--
-- 【선행】Supabase 에서 아직 반영 안 했다면:
--         price_alerts_log_seoul_daily_upsert_20260527.sql
--       (nullable target_price·서울일 dedupe 컬럼·일 단위 UNIQUE·ON CONFLICT 사용)
--
-- 공개 확인: stocks-ser4.onrender.com/6fq1fvo9pm/new (slug 는 application.yml 과 동일)
--
-- price_alerts : 멱등 (symbol + target_price + condition)
-- price_alerts_log : 작성자 CHARTBOY · 같은 서울일·같은 키는 UPDATE(제약 준비된 DB만)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '036540', 'SFA반도체', 10471, 'ABOVE', '매수타점 ✔ 년봉 통 돌파 / 10,471원'),
  ('KR', '018260', '삼성에스디에스', 198800, 'ABOVE', '매수타점 ✔ 월봉 바닥 / 198,800원'),
  ('KR', '122640', '예스티', 30300, 'ABOVE', '매수타점 ✔ 30,300원'),
  ('KR', '122640', '예스티', 31350, 'ABOVE', '매수타점 ✔ 31,350원'),
  ('KR', '274090', '켄코아에어로스페이스', 31300, 'ABOVE', '매수타점 ✔ / 31,300원'),
  ('KR', '031980', '피에스케이홀딩스', 138700, 'ABOVE', '매수타점 ✔ / 138,700원'),
  ('KR', '031980', '피에스케이홀딩스', 139400, 'ABOVE', '매수타점 ✔ / 139,400원'),
  ('KR', '033640', '네패스', 37200, 'ABOVE', '매수타점 ✔ / 37,200원'),
  ('KR', '033640', '네패스', 37850, 'ABOVE', '매수타점 ✔ / 37,850원'),
  ('KR', '005850', '에스엘', 77300, 'ABOVE', '매수타점 ✔ / 77,300원'),
  ('KR', '005850', '에스엘', 77400, 'ABOVE', '매수타점 ✔ / 77,400원'),
  ('KR', '354320', '알멕', 100000, 'ABOVE', '매수타점 ✔ 알멕 이동평균선 / 100,000원'),
  ('KR', '354320', '알멕', 104000, 'ABOVE', '매수타점 ✔ / 104,000원'),
  ('KR', '036200', '유니셈', 12480, 'ABOVE', '매수타점 ✔ 월봉 컵위드핸들 / 12,480원')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ---------------------------------------------------------------------------
-- price_alerts_log · ON CONFLICT = 같은 서울일(posted_by, stock_code, target 가격 버킷, condition) 1줄 유지 후 라벨·가격 등 갱신
-- 【가격 미기재】아래처럼 NULL 허용(마이그레이션 선행 후). 같은 일·종목·조건 재실행 시 UPDATE.
-- -- ('CHARTBOY', 'KR', '036540', 'SFA반도체', NULL, 'ABOVE', '매수타점 블록 / 원문 줄만 …')
-- 제약 이름이 다른 DB에서는 ON CONFLICT 절 없이 순수 INSERT 만 사용할 것.
-- ---------------------------------------------------------------------------
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label)
VALUES
  ('CHARTBOY', 'KR', '036540', 'SFA반도체', 10471, 'ABOVE', '매수타점 블록 / 년봉 통 돌파 / ✔️SFA반도체 / 10,471원'),
  ('CHARTBOY', 'KR', '018260', '삼성에스디에스', 198800, 'ABOVE', '매수타점 블록 / 월봉 바닥 / ✔️삼성에스디에스 / 198,800원'),
  ('CHARTBOY', 'KR', '122640', '예스티', 30300, 'ABOVE', '매수타점 블록 / ✔️예스티'),
  ('CHARTBOY', 'KR', '122640', '예스티', 31350, 'ABOVE', '매수타점 블록 / ✔️예스티'),
  ('CHARTBOY', 'KR', '274090', '켄코아에어로스페이스', 31300, 'ABOVE', '매수타점 블록 / ✔️켄코아에어로스페이스 / 31,300원'),
  ('CHARTBOY', 'KR', '031980', '피에스케이홀딩스', 138700, 'ABOVE', '매수타점 블록 / ✔️피에스케이홀딩스 / 138,700원'),
  ('CHARTBOY', 'KR', '031980', '피에스케이홀딩스', 139400, 'ABOVE', '매수타점 블록 / ✔️피에스케이홀딩스 / 139,400원'),
  ('CHARTBOY', 'KR', '033640', '네패스', 37200, 'ABOVE', '매수타점 블록 / ✔️네패스'),
  ('CHARTBOY', 'KR', '033640', '네패스', 37850, 'ABOVE', '매수타점 블록 / ✔️네패스'),
  ('CHARTBOY', 'KR', '005850', '에스엘', 77300, 'ABOVE', '매수타점 블록 / ✔️에스엘'),
  ('CHARTBOY', 'KR', '005850', '에스엘', 77400, 'ABOVE', '매수타점 블록 / ✔️에스엘'),
  ('CHARTBOY', 'KR', '354320', '알멕', 100000, 'ABOVE', '매수타점 블록 / ✔️알멕 이동평균선'),
  ('CHARTBOY', 'KR', '354320', '알멕', 104000, 'ABOVE', '매수타점 블록 / ✔️알멕'),
  ('CHARTBOY', 'KR', '036200', '유니셈', 12480, 'ABOVE', '매수타점 블록 / ✔️유니셈 컵위드핸들 / 12,480원')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market     = EXCLUDED.market,
  symbol     = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label      = EXCLUDED.label;
