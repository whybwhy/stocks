-- 20260818 8월 18일(화) 영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-08-18
-- 【종목 코드】메가터치 446540(레포), S-Oil 010950(네이버), 제닉 123330(레포),
--   IPARK현대산업개발 294870(네이버)
--
-- 공개 확인: stocks-ser4.onrender.com/mjn98nnisx/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '446540', '메가터치', 9010, 'ABOVE', '8월 18일(화) 영상정리 / 메가터치 (9,010원-하이웨이브캔들)'),
  ('KR', '010950', 'S-Oil', 156500, 'ABOVE', '8월 18일(화) 영상정리 / S-Oil (156,500원)'),
  ('KR', '123330', '제닉', 33900, 'ABOVE', '8월 18일(화) 영상정리 / 제닉 (33,900원-연속캔들 동일고점)'),
  ('KR', '294870', 'IPARK현대산업개발', 23000, 'ABOVE', '8월 18일(화) 영상정리 / IPARK현대산업개발 (23,000원-연속캔들 동일고점)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '446540', '메가터치', 9010, 'ABOVE', '8월 18일(화) 영상정리 / 메가터치 (9,010원-하이웨이브캔들)', DATE '2026-08-18'),
  ('HYONYHYONY', 'KR', '010950', 'S-Oil', 156500, 'ABOVE', '8월 18일(화) 영상정리 / S-Oil (156,500원)', DATE '2026-08-18'),
  ('HYONYHYONY', 'KR', '123330', '제닉', 33900, 'ABOVE', '8월 18일(화) 영상정리 / 제닉 (33,900원-연속캔들 동일고점)', DATE '2026-08-18'),
  ('HYONYHYONY', 'KR', '294870', 'IPARK현대산업개발', 23000, 'ABOVE', '8월 18일(화) 영상정리 / IPARK현대산업개발 (23,000원-연속캔들 동일고점)', DATE '2026-08-18')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
