-- 20260609 NAVER 종목체크 · Asia/Seoul
-- 【사용자 원문】NAVER 292k/295k · 밑꼬다리 양봉 영상 안내(종목 미지정 제외)
-- 【seoul_log_date】2026-06-09
--
-- 【종목】035420 NAVER (레포)
--
-- 공개 확인: stocks-ser4.onrender.com/l2gtrypnsu/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 2행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '035420', 'NAVER', 292000, 'ABOVE', 'NAVER / 292.000원 돌파시 1차.'),
  ('KR', '035420', 'NAVER', 295000, 'ABOVE', 'NAVER / 295.000원 돌파시 2차.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 2행 · 2026-06-09
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '035420', 'NAVER', 292000, 'ABOVE', 'NAVER / 292.000원 돌파시 1차.', DATE '2026-06-09'),
  ('CHARTBOY', 'KR', '035420', 'NAVER', 295000, 'ABOVE', 'NAVER / 295.000원 돌파시 2차.', DATE '2026-06-09')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
