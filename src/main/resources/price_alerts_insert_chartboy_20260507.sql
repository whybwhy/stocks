-- 차트보이 메모 배치 (source = CHARTBOY) — 2026-05-07 정리
-- 멱등: (symbol, target_price) 동일 행이 있으면 삽입 생략
--
-- 종목코드 참고:
--   두산에너빌리티 034020, 한전기술 052690, 천보 278280,
--   가온그룹 078890, 롯데에너지머티리얼즈 020150, 삼성중공업 010140,
--   솔브레인 357780, 에스비비테크 389500, 에치에프알 230240, 코오롱 002020

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  -- ▶ 매수타점·고점 돌파 메모
  ('KR', '034020', '두산에너빌리티', 146498, 'ABOVE',
   '146,498원 돌파시 신규 매수타점 / 2007년 고점 목표·돌파 시 신고가 매매 (물려도 사는 자리)'),
  ('KR', '052690', '한전기술', 198000, 'ABOVE',
   '198,000원 돌파시 신규 매수타점 / 2010년 고점 돌파 후에도 신고가 매매 가능'),
  ('KR', '278280', '천보', 70700, 'ABOVE',
   '70,700원 돌파시 신규 매수타점 (기존 타점 유지)'),

  -- ✅ 5월 7일(목) 매수·매도 정리에서 언급한 매수 가격(돌파 기준으로 등록)
  ('KR', '078890', '가온그룹', 8049, 'ABOVE',
   '8,049원 돌파시 / 5·7 매수·매도 정리(매수)'),
  ('KR', '020150', '롯데에너지머티리얼즈', 75000, 'ABOVE',
   '75,000원 돌파시 / 5·7 매수·매도 정리(매수)'),
  ('KR', '010140', '삼성중공업', 32500, 'ABOVE',
   '32,500원 돌파시 / 5·7 매수·매도 정리(매수)'),
  ('KR', '357780', '솔브레인', 491500, 'ABOVE',
   '491,500원 돌파시 / 5·7 매수·매도 정리(매수)'),
  ('KR', '389500', '에스비비테크', 70400, 'ABOVE',
   '70,400원 돌파시 / 5·7 매수·매도 정리(매수)'),
  ('KR', '230240', '에치에프알', 37950, 'ABOVE',
   '37,950원 돌파시 / 5·7 매수·매도 정리(매수)'),
  ('KR', '002020', '코오롱', 72800, 'ABOVE',
   '72,800원 돌파시 / 5·7 매수·매도 정리(매수)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
);
