-- 20260625 CHARTBOY 매수타점 체크 · Asia/Seoul
-- 【seoul_log_date · created_at】2026-06-25 00:00 (Asia/Seoul)
-- 【종목 코드】레포·KRX: 041830 인바디, 131290 티에스이, 064290 인텍플러스,
--   196170 알테오젠, 214450 파마리서치
-- 【제외】알테오젠·파마리서치 — 가격 미기재(1주만·어제 영상 참조) → log 만

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 3행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source, created_at)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY',
       timestamptz '2026-06-25 00:00:00+09'
FROM (VALUES
  ('KR', '041830', '인바디', 48900, 'ABOVE', '인바디 / 연속캔들 동일고점 / 48.900원 돌파시'),
  ('KR', '131290', '티에스이', 250000, 'ABOVE', '티에스이 / 연속캔들 동일고점 / 250.000원 돌파시'),
  ('KR', '064290', '인텍플러스', 44900, 'ABOVE', '인텍플러스 / 이전 매수타점 / 44.900원 돌파시')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 5행 · 2026-06-25
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date, created_at)
VALUES
  ('CHARTBOY', 'KR', '041830', '인바디', 48900, 'ABOVE', '인바디 / 연속캔들 동일고점 / 48.900원 돌파시', DATE '2026-06-25', timestamptz '2026-06-25 00:00:00+09'),
  ('CHARTBOY', 'KR', '131290', '티에스이', 250000, 'ABOVE', '티에스이 / 연속캔들 동일고점 / 250.000원 돌파시', DATE '2026-06-25', timestamptz '2026-06-25 00:00:00+09'),
  ('CHARTBOY', 'KR', '064290', '인텍플러스', 44900, 'ABOVE', '인텍플러스 / 이전 매수타점 / 44.900원 돌파시', DATE '2026-06-25', timestamptz '2026-06-25 00:00:00+09'),
  ('CHARTBOY', 'KR', '196170', '알테오젠', NULL, 'ABOVE', '알테오젠 / 1주만 / 어제 영상 참조', DATE '2026-06-25', timestamptz '2026-06-25 00:00:00+09'),
  ('CHARTBOY', 'KR', '214450', '파마리서치', NULL, 'ABOVE', '파마리서치 / 1주만 / 어제 영상 참조', DATE '2026-06-25', timestamptz '2026-06-25 00:00:00+09')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label,
  created_at   = EXCLUDED.created_at;
