-- 20260812 8월 12일(수) CHARTBOY 신규 매수타점 체크 · Asia/Seoul
-- 【seoul_log_date】2026-08-12
-- 【종목 코드】코웨이 021240(레포), 제이에스링크 127120(레포 · 가격 없음 → log NULL),
--   SFA반도체 036540(레포)
-- 【제외】신규 매수타점 체크 서두
--
-- 공개 확인: stocks-ser4.onrender.com/4ndmtp8cb5/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '021240', '코웨이', 99100, 'ABOVE', '코웨이 / (삼봉언덕 & 가운데자리) / 99.100원 돌파시 1차 / 100.500원 돌파시 2차'),
  ('KR', '021240', '코웨이', 100500, 'ABOVE', '코웨이 / (삼봉언덕 & 가운데자리) / 99.100원 돌파시 1차 / 100.500원 돌파시 2차'),
  ('KR', '036540', 'SFA반도체', 6480, 'ABOVE', 'SFA반도체 / (음봉 또로록 진행중) / 6.480원 돌파시 / 오늘 빵빵빵으로 넘어야지?')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '021240', '코웨이', 99100, 'ABOVE', '코웨이 / (삼봉언덕 & 가운데자리) / 99.100원 돌파시 1차 / 100.500원 돌파시 2차', DATE '2026-08-12'),
  ('CHARTBOY', 'KR', '021240', '코웨이', 100500, 'ABOVE', '코웨이 / (삼봉언덕 & 가운데자리) / 99.100원 돌파시 1차 / 100.500원 돌파시 2차', DATE '2026-08-12'),
  ('CHARTBOY', 'KR', '127120', '제이에스링크', NULL, 'ABOVE', '제이에스링크 / (구름대 넘은 5일선 급등주) / 지난주에 산 사람들 있지? / 5일선 안 깨지면, / 슈팅직전 상승조정.', DATE '2026-08-12'),
  ('CHARTBOY', 'KR', '036540', 'SFA반도체', 6480, 'ABOVE', 'SFA반도체 / (음봉 또로록 진행중) / 6.480원 돌파시 / 오늘 빵빵빵으로 넘어야지?', DATE '2026-08-12')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
