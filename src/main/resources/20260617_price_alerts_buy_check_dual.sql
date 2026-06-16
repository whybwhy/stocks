-- 20260617 CHARTBOY 매수타점 체크 · Asia/Seoul
-- 【seoul_log_date】2026-06-17
-- 【종목 코드】레포·KRX: 443060 HD현대마린솔루션, 475480 SK이터닉스, 041830 인바디, 011210 현대위아, 001450 현대해상
--
-- 공개 확인: stocks-ser4.onrender.com/97x99mgwah/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 9행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '443060', 'HD현대마린솔루션', 260500, 'ABOVE', 'HD현대마린솔루션 / 오늘 가야함 / 260.500원 돌파시 1차'),
  ('KR', '443060', 'HD현대마린솔루션', 262000, 'ABOVE', 'HD현대마린솔루션 / 오늘 가야함 / 262.000원 돌파시 2차'),
  ('KR', '475480', 'SK이터닉스', 61900, 'ABOVE', 'SK이터닉스 / 어제 영상 참조 / 61.900원 돌파시 1차'),
  ('KR', '475480', 'SK이터닉스', 64200, 'ABOVE', 'SK이터닉스 / 어제 영상 참조 / 64.200원 돌파시 2차'),
  ('KR', '041830', '인바디', 48900, 'ABOVE', '인바디 / 주봉 연속캔들 동일고점 / 48.900원 돌파시'),
  ('KR', '011210', '현대위아', 95000, 'ABOVE', '현대위아 / 일봉 삼봉언덕 / 95.000원 돌파시 1차'),
  ('KR', '011210', '현대위아', 103500, 'ABOVE', '현대위아 / 일봉 삼봉언덕 / 103.500원 돌파시 2차'),
  ('KR', '001450', '현대해상', 38700, 'ABOVE', '현대해상 / 38.700원 1차 / 40.150원 2차로 매수한 우리종목임. 5일선 따라가는 급등주 매매법 가능함.'),
  ('KR', '001450', '현대해상', 40150, 'ABOVE', '현대해상 / 38.700원 1차 / 40.150원 2차로 매수한 우리종목임. 5일선 따라가는 급등주 매매법 가능함.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 9행 · 2026-06-17
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '443060', 'HD현대마린솔루션', 260500, 'ABOVE', 'HD현대마린솔루션 / 오늘 가야함 / 260.500원 돌파시 1차', DATE '2026-06-17'),
  ('CHARTBOY', 'KR', '443060', 'HD현대마린솔루션', 262000, 'ABOVE', 'HD현대마린솔루션 / 오늘 가야함 / 262.000원 돌파시 2차', DATE '2026-06-17'),
  ('CHARTBOY', 'KR', '475480', 'SK이터닉스', 61900, 'ABOVE', 'SK이터닉스 / 어제 영상 참조 / 61.900원 돌파시 1차', DATE '2026-06-17'),
  ('CHARTBOY', 'KR', '475480', 'SK이터닉스', 64200, 'ABOVE', 'SK이터닉스 / 어제 영상 참조 / 64.200원 돌파시 2차', DATE '2026-06-17'),
  ('CHARTBOY', 'KR', '041830', '인바디', 48900, 'ABOVE', '인바디 / 주봉 연속캔들 동일고점 / 48.900원 돌파시', DATE '2026-06-17'),
  ('CHARTBOY', 'KR', '011210', '현대위아', 95000, 'ABOVE', '현대위아 / 일봉 삼봉언덕 / 95.000원 돌파시 1차', DATE '2026-06-17'),
  ('CHARTBOY', 'KR', '011210', '현대위아', 103500, 'ABOVE', '현대위아 / 일봉 삼봉언덕 / 103.500원 돌파시 2차', DATE '2026-06-17'),
  ('CHARTBOY', 'KR', '001450', '현대해상', 38700, 'ABOVE', '현대해상 / 38.700원 1차 / 40.150원 2차로 매수한 우리종목임. 5일선 따라가는 급등주 매매법 가능함.', DATE '2026-06-17'),
  ('CHARTBOY', 'KR', '001450', '현대해상', 40150, 'ABOVE', '현대해상 / 38.700원 1차 / 40.150원 2차로 매수한 우리종목임. 5일선 따라가는 급등주 매매법 가능함.', DATE '2026-06-17')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
