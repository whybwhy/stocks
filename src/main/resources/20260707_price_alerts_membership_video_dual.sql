-- 20260707 7월 7일(화) 멤버쉽 영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-07-07
-- 【종목 코드】기아 000270, 씨젠 096530, 에스티팜 237690 (레포 기존 dual)
--
-- 공개 확인: stocks-ser4.onrender.com/dfpho7i82u/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 5행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '000270', '기아', 177000, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 기아 (177,000원)'),
  ('KR', '096530', '씨젠', 32500, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 씨젠 (32,500원)'),
  ('KR', '237690', '에스티팜', 137100, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 에스티팜 (137,100원/138,600원/145,400원)'),
  ('KR', '237690', '에스티팜', 138600, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 에스티팜 (137,100원/138,600원/145,400원)'),
  ('KR', '237690', '에스티팜', 145400, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 에스티팜 (137,100원/138,600원/145,400원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — HYONYHYONY · 5행 · 2026-07-07
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '000270', '기아', 177000, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 기아 (177,000원)', DATE '2026-07-07'),
  ('HYONYHYONY', 'KR', '096530', '씨젠', 32500, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 씨젠 (32,500원)', DATE '2026-07-07'),
  ('HYONYHYONY', 'KR', '237690', '에스티팜', 137100, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 에스티팜 (137,100원/138,600원/145,400원)', DATE '2026-07-07'),
  ('HYONYHYONY', 'KR', '237690', '에스티팜', 138600, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 에스티팜 (137,100원/138,600원/145,400원)', DATE '2026-07-07'),
  ('HYONYHYONY', 'KR', '237690', '에스티팜', 145400, 'ABOVE', '7월 7일(화) 멤버쉽 영상정리 / 에스티팜 (137,100원/138,600원/145,400원)', DATE '2026-07-07')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
