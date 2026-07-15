-- 20260713 7월 13일(월) 멤버쉽 영상 정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-07-13
-- 【종목 코드】GS건설 006360, 마키나락스 477850, 세나테크놀로지 061090, 파세코 037070
--
-- 공개 확인: stocks-ser4.onrender.com/uk59dgc0wn/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 5행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '006360', 'GS건설', 34350, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / GS건설 (34,350원/56,500원)'),
  ('KR', '006360', 'GS건설', 56500, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / GS건설 (34,350원/56,500원)'),
  ('KR', '477850', '마키나락스', 32350, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / 마키나락스 (32,350원)'),
  ('KR', '061090', '세나테크놀로지', 54400, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / 세나테크놀로지 (54,400원)'),
  ('KR', '037070', '파세코', 9980, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / 파세코 (9,980원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — HYONYHYONY · 5행 · 2026-07-13
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '006360', 'GS건설', 34350, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / GS건설 (34,350원/56,500원)', DATE '2026-07-13'),
  ('HYONYHYONY', 'KR', '006360', 'GS건설', 56500, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / GS건설 (34,350원/56,500원)', DATE '2026-07-13'),
  ('HYONYHYONY', 'KR', '477850', '마키나락스', 32350, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / 마키나락스 (32,350원)', DATE '2026-07-13'),
  ('HYONYHYONY', 'KR', '061090', '세나테크놀로지', 54400, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / 세나테크놀로지 (54,400원)', DATE '2026-07-13'),
  ('HYONYHYONY', 'KR', '037070', '파세코', 9980, 'ABOVE', '7월 13일(월) 멤버쉽 영상 정리 / 파세코 (9,980원)', DATE '2026-07-13')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
