-- 20260708 7월 8일(수) 멤버쉽 영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-07-08
-- 【종목 코드】마녀공장 439090, 마키나락스 477850 (레포 기존 dual)
--
-- 공개 확인: stocks-ser4.onrender.com/dkudn7nql5/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 2행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '439090', '마녀공장', 18510, 'ABOVE', '7월 8일(수) 멤버쉽 영상정리 / 마녀공장 (18,510원)'),
  ('KR', '477850', '마키나락스', 30400, 'ABOVE', '7월 8일(수) 멤버쉽 영상정리 / 마키나락스 (30,400원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — HYONYHYONY · 2행 · 2026-07-08
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '439090', '마녀공장', 18510, 'ABOVE', '7월 8일(수) 멤버쉽 영상정리 / 마녀공장 (18,510원)', DATE '2026-07-08'),
  ('HYONYHYONY', 'KR', '477850', '마키나락스', 30400, 'ABOVE', '7월 8일(수) 멤버쉽 영상정리 / 마키나락스 (30,400원)', DATE '2026-07-08')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
