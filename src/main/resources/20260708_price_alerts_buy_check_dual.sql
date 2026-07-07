-- 20260708 CHARTBOY 매수타점 체크 · Asia/Seoul
-- 【seoul_log_date · created_at】2026-07-08 00:00 (Asia/Seoul)
-- 【종목 코드】레포·KRX: 161390 한국타이어앤테크놀로지
--
-- 공개 확인: stocks-ser4.onrender.com/pioc1wm2gh/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 2행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source, created_at)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY',
       timestamptz '2026-07-08 00:00:00+09'
FROM (VALUES
  ('KR', '161390', '한국타이어앤테크놀로지', 76000, 'ABOVE', '한국타이어앤테크놀로지 / 주봉 뚜껑 딴 자리 돌파하면 매수가능함. / 76.000원/78.400원 돌파시'),
  ('KR', '161390', '한국타이어앤테크놀로지', 78400, 'ABOVE', '한국타이어앤테크놀로지 / 주봉 뚜껑 딴 자리 돌파하면 매수가능함. / 76.000원/78.400원 돌파시')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 2행 · 2026-07-08
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date, created_at)
VALUES
  ('CHARTBOY', 'KR', '161390', '한국타이어앤테크놀로지', 76000, 'ABOVE', '한국타이어앤테크놀로지 / 주봉 뚜껑 딴 자리 돌파하면 매수가능함. / 76.000원/78.400원 돌파시', DATE '2026-07-08', timestamptz '2026-07-08 00:00:00+09'),
  ('CHARTBOY', 'KR', '161390', '한국타이어앤테크놀로지', 78400, 'ABOVE', '한국타이어앤테크놀로지 / 주봉 뚜껑 딴 자리 돌파하면 매수가능함. / 76.000원/78.400원 돌파시', DATE '2026-07-08', timestamptz '2026-07-08 00:00:00+09')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label,
  created_at   = EXCLUDED.created_at;
