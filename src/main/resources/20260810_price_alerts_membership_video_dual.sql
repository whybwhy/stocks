-- 20260810 8월 10일(월) 멤버쉽영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-08-10
-- 【종목 코드】금호석유화학 011780(네이버), 노바렉스 194700(TradingView), 동국제약 086450(레포),
--   씨젠 096530(레포), 오리온홀딩스 001800(레포), 주성엔지니어링 036930(네이버),
--   원익IPS 240810(레포), 피에스케이홀딩스 031980(레포), 현대힘스 460930(네이버),
--   서진시스템 178320(레포), 에치에프알 230240(레포)
--
-- 공개 확인: stocks-ser4.onrender.com/t2e6jvoh56/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '011780', '금호석유화학', 167500, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 금호석유화학 (167,500원-박스권 매매)'),
  ('KR', '194700', '노바렉스', 17610, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 노바렉스 (17,610원-총쏘는모양)'),
  ('KR', '086450', '동국제약', 20850, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 동국제약 (20,850원-구름대를 넘는게 중요)'),
  ('KR', '096530', '씨젠', 31450, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 씨젠 (31,450원/32,500원)'),
  ('KR', '096530', '씨젠', 32500, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 씨젠 (31,450원/32,500원)'),
  ('KR', '001800', '오리온홀딩스', 28250, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 오리온홀딩스 (28,250원-총쏘는모양)'),
  ('KR', '036930', '주성엔지니어링', 145800, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 주성엔지니어링 (145,800원-음봉 또로록)'),
  ('KR', '240810', '원익IPS', 103400, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 원익IPS (103,400원-음봉 또로록)'),
  ('KR', '031980', '피에스케이홀딩스', 110000, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 피에스케이홀딩스 (110,000원-음봉 또로록)'),
  ('KR', '460930', '현대힘스', 16730, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 현대힘스 (16,730원)'),
  ('KR', '178320', '서진시스템', 38700, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 서진시스템 (38,700원-음봉 또로록)'),
  ('KR', '230240', '에치에프알', 16830, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 에치에프알 (16,830원-음봉 또로록/240일선 아래)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '011780', '금호석유화학', 167500, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 금호석유화학 (167,500원-박스권 매매)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '194700', '노바렉스', 17610, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 노바렉스 (17,610원-총쏘는모양)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '086450', '동국제약', 20850, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 동국제약 (20,850원-구름대를 넘는게 중요)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '096530', '씨젠', 31450, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 씨젠 (31,450원/32,500원)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '096530', '씨젠', 32500, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 씨젠 (31,450원/32,500원)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '001800', '오리온홀딩스', 28250, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 오리온홀딩스 (28,250원-총쏘는모양)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '036930', '주성엔지니어링', 145800, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 주성엔지니어링 (145,800원-음봉 또로록)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '240810', '원익IPS', 103400, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 원익IPS (103,400원-음봉 또로록)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '031980', '피에스케이홀딩스', 110000, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 피에스케이홀딩스 (110,000원-음봉 또로록)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '460930', '현대힘스', 16730, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 현대힘스 (16,730원)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '178320', '서진시스템', 38700, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 서진시스템 (38,700원-음봉 또로록)', DATE '2026-08-10'),
  ('HYONYHYONY', 'KR', '230240', '에치에프알', 16830, 'ABOVE', '8월 10일(월) 멤버쉽영상정리 / 에치에프알 (16,830원-음봉 또로록/240일선 아래)', DATE '2026-08-10')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
