-- 20260804 8월 4일(화) CHARTBOY 매수타점 · Asia/Seoul
-- 【seoul_log_date】2026-08-04
-- 【종목 코드】LIG아큐버 073490(네이버), 네오팜 092730(네이버·FnGuide),
--   세나테크놀로지 061090(레포), 아이비김영 339950(레포)
-- 【제외】차트보입니다 인사 서두
--
-- 공개 확인: stocks-ser4.onrender.com/bd6l7wu8oa/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 4행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '073490', 'LIG아큐버', 30300, 'ABOVE', 'LIG아큐버 / (작은 삼봉언덕) / 30.300원 돌파시'),
  ('KR', '092730', '네오팜', 20250, 'ABOVE', '네오팜 / (가운데 자리) / 20.250원 돌파시'),
  ('KR', '061090', '세나테크놀로지', 48900, 'ABOVE', '세나테크놀로지 / (작은 삼봉언덕) / 48.900원 돌파시'),
  ('KR', '339950', '아이비김영', 2955, 'ABOVE', '아이비김영 / (가운데 자리) / 2.955원 돌파시')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 4행 · 2026-08-04
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '073490', 'LIG아큐버', 30300, 'ABOVE', 'LIG아큐버 / (작은 삼봉언덕) / 30.300원 돌파시', DATE '2026-08-04'),
  ('CHARTBOY', 'KR', '092730', '네오팜', 20250, 'ABOVE', '네오팜 / (가운데 자리) / 20.250원 돌파시', DATE '2026-08-04'),
  ('CHARTBOY', 'KR', '061090', '세나테크놀로지', 48900, 'ABOVE', '세나테크놀로지 / (작은 삼봉언덕) / 48.900원 돌파시', DATE '2026-08-04'),
  ('CHARTBOY', 'KR', '339950', '아이비김영', 2955, 'ABOVE', '아이비김영 / (가운데 자리) / 2.955원 돌파시', DATE '2026-08-04')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
