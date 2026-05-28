-- 20260528 5월 28일(목) 멤버쉽영상정리 — dual · Asia/Seoul
-- 【사용자 원문】5월 28일(목) 멤버쉽영상정리
--   LG에너지솔루션 (444,000원/455,000원/527,000원)
--   나노팀 (15,250원-월봉 빵빵빵) · 삼성SDI (723,000원/784,179원)
--   디앤디파마텍 (110,000원) · 에스엘 (77,400원) · 펩트론 (359,000원)
--   LG씨엔에스 (99,400원/100,800원) · 라온로보틱스 (23,950원/24,400원)
--   현대위아 (115,000원)
--
-- 【선행】Supabase: price_alerts_log_seoul_daily_upsert_20260527.sql
--         price_alerts_log_kst_created_at_20260528.sql
--
-- 【종목 코드】레포: price_alerts_insert_chartboy_memo_batch_20260518_spacex.sql,
--   price_alerts_insert_chartboy_20260510_memo.sql, price_alerts_all.sql,
--   20260527_price_alerts_stock_check_bio_dual.sql 등
--   LG에너지솔루션 373220, 나노팀 417010, 삼성SDI 006400, 디앤디파마텍 347850,
--   에스엘 005850, 펩트론 087010, LG씨엔에스 064400, 라온로보틱스 232680, 현대위아 011210
--
-- 공개 확인: stocks-ser4.onrender.com/tnglo8t0cf/new  (slug 는 application.yml 과 동일)
--
-- price_alerts_log.posted_by: HYONYHYONY (블록 🌈 시작)

-- ============================================================================
-- 1) public.price_alerts — 멱등
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '373220', 'LG에너지솔루션', 444000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG에너지솔루션 (444,000원/455,000원/527,000원)'),
  ('KR', '373220', 'LG에너지솔루션', 455000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG에너지솔루션 (444,000원/455,000원/527,000원)'),
  ('KR', '373220', 'LG에너지솔루션', 527000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG에너지솔루션 (444,000원/455,000원/527,000원)'),
  ('KR', '417010', '나노팀', 15250, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 나노팀 (15,250원-월봉 빵빵빵)'),
  ('KR', '006400', '삼성SDI', 723000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 삼성SDI (723,000원/784,179원)'),
  ('KR', '006400', '삼성SDI', 784179, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 삼성SDI (723,000원/784,179원)'),
  ('KR', '347850', '디앤디파마텍', 110000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 디앤디파마텍 (110,000원)'),
  ('KR', '005850', '에스엘', 77400, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 에스엘 (77,400원)'),
  ('KR', '087010', '펩트론', 359000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 펩트론 (359,000원)'),
  ('KR', '064400', 'LG씨엔에스', 99400, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG씨엔에스 (99,400원/100,800원)'),
  ('KR', '064400', 'LG씨엔에스', 100800, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG씨엔에스 (99,400원/100,800원)'),
  ('KR', '232680', '라온로보틱스', 23950, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 라온로보틱스 (23,950원/24,400원)'),
  ('KR', '232680', '라온로보틱스', 24400, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 라온로보틱스 (23,950원/24,400원)'),
  ('KR', '011210', '현대위아', 115000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 현대위아 (115,000원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — HYONYHYONY · 14행
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label)
VALUES
  ('HYONYHYONY', 'KR', '373220', 'LG에너지솔루션', 444000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG에너지솔루션 (444,000원/455,000원/527,000원)'),
  ('HYONYHYONY', 'KR', '373220', 'LG에너지솔루션', 455000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG에너지솔루션 (444,000원/455,000원/527,000원)'),
  ('HYONYHYONY', 'KR', '373220', 'LG에너지솔루션', 527000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG에너지솔루션 (444,000원/455,000원/527,000원)'),
  ('HYONYHYONY', 'KR', '417010', '나노팀', 15250, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 나노팀 (15,250원-월봉 빵빵빵)'),
  ('HYONYHYONY', 'KR', '006400', '삼성SDI', 723000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 삼성SDI (723,000원/784,179원)'),
  ('HYONYHYONY', 'KR', '006400', '삼성SDI', 784179, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 삼성SDI (723,000원/784,179원)'),
  ('HYONYHYONY', 'KR', '347850', '디앤디파마텍', 110000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 디앤디파마텍 (110,000원)'),
  ('HYONYHYONY', 'KR', '005850', '에스엘', 77400, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 에스엘 (77,400원)'),
  ('HYONYHYONY', 'KR', '087010', '펩트론', 359000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 펩트론 (359,000원)'),
  ('HYONYHYONY', 'KR', '064400', 'LG씨엔에스', 99400, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG씨엔에스 (99,400원/100,800원)'),
  ('HYONYHYONY', 'KR', '064400', 'LG씨엔에스', 100800, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / LG씨엔에스 (99,400원/100,800원)'),
  ('HYONYHYONY', 'KR', '232680', '라온로보틱스', 23950, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 라온로보틱스 (23,950원/24,400원)'),
  ('HYONYHYONY', 'KR', '232680', '라온로보틱스', 24400, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 라온로보틱스 (23,950원/24,400원)'),
  ('HYONYHYONY', 'KR', '011210', '현대위아', 115000, 'ABOVE', '5월 28일(목) 멤버쉽영상정리 / 현대위아 (115,000원)')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
