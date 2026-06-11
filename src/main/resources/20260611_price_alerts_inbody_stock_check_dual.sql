-- 20260611 인바디 종목체크 · Asia/Seoul
-- 【사용자 원문】연속캔들 동일고점(주봉)·48.9k·2018년 3월 고점 저항·단타 차트
-- 【seoul_log_date】2026-06-11
--
-- 【종목】041830 인바디 (네이버·KRX)
-- 【제외】잘 찾노 감탄·이모지
--
-- 공개 확인: stocks-ser4.onrender.com/4tbtpuw7q2/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 1행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '041830', '인바디', 48900, 'ABOVE',
   '인바디 / 연속캔들 동일고점(주봉) / 48.900원 돌파시 / (2018년 3월 고점이 저항대 언덕) / 어제 여러분이 찾아 온 종목 중 최고임. 지수만 받쳐주면, "단타"하기 딱 좋은 차트 맞음.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 1행 · 2026-06-11
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '041830', '인바디', 48900, 'ABOVE',
   '인바디 / 연속캔들 동일고점(주봉) / 48.900원 돌파시 / (2018년 3월 고점이 저항대 언덕) / 어제 여러분이 찾아 온 종목 중 최고임. 지수만 받쳐주면, "단타"하기 딱 좋은 차트 맞음.',
   DATE '2026-06-11')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
