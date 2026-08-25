-- 20260819 8월 19일(수) CHARTBOY 단타종목 매수타점 · Asia/Seoul
-- 【seoul_log_date】2026-08-19
-- 【종목 코드】IPARK현대산업개발 294870(네이버·구 HDC현대산업개발), 팬오션 028670(레포)
-- 【제외】단타종목 매수타점 서두 · KRX 차트기준 안내 · 인사 이모지
-- 【비고】원문 「23.000월」→ 23.000원
--
-- 공개 확인: stocks-ser4.onrender.com/mjn98nnisx/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '294870', 'IPARK현대산업개발', 23000, 'ABOVE', 'IPARK현대산업개발 / 연속캔들 동일고점 / 23.000원 돌파시'),
  ('KR', '028670', '팬오션', 6060, 'ABOVE', '팬오션 / 삼봉 언덕까진 가겠지? / 6.060원 돌파시 1차 / 6.130원 돌파시 2차'),
  ('KR', '028670', '팬오션', 6130, 'ABOVE', '팬오션 / 삼봉 언덕까진 가겠지? / 6.060원 돌파시 1차 / 6.130원 돌파시 2차')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '294870', 'IPARK현대산업개발', 23000, 'ABOVE', 'IPARK현대산업개발 / 연속캔들 동일고점 / 23.000원 돌파시', DATE '2026-08-19'),
  ('CHARTBOY', 'KR', '028670', '팬오션', 6060, 'ABOVE', '팬오션 / 삼봉 언덕까진 가겠지? / 6.060원 돌파시 1차 / 6.130원 돌파시 2차', DATE '2026-08-19'),
  ('CHARTBOY', 'KR', '028670', '팬오션', 6130, 'ABOVE', '팬오션 / 삼봉 언덕까진 가겠지? / 6.060원 돌파시 1차 / 6.130원 돌파시 2차', DATE '2026-08-19')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
