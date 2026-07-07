-- 20260622 CHARTBOY 대형주 정찰병 · Asia/Seoul
-- 【seoul_log_date · created_at】2026-06-22 00:00 (Asia/Seoul)
-- 【종목 코드】레포 005380 현대차

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 1행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source, created_at)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY',
       timestamptz '2026-06-22 00:00:00+09'
FROM (VALUES
  ('KR', '005380', '현대차', 659000, 'ABOVE', '현대차 / 정찰병 시작이 659.000원 돌파시인것처럼. / 대부분 대형주들이 앞에 작은 언덕을 만들었음. 거기서부터 정찰병 가능함.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 1행 · 2026-06-22
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date, created_at)
VALUES
  ('CHARTBOY', 'KR', '005380', '현대차', 659000, 'ABOVE', '현대차 / 정찰병 시작이 659.000원 돌파시인것처럼. / 대부분 대형주들이 앞에 작은 언덕을 만들었음. 거기서부터 정찰병 가능함.', DATE '2026-06-22', timestamptz '2026-06-22 00:00:00+09')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label,
  created_at   = EXCLUDED.created_at;
