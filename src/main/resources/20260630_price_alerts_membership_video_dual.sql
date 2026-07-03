-- 20260630 6월 30일(화) 멤버쉽 영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-06-30

-- 1) public.price_alerts — 멱등 · 4행
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '014680', '한솔케미칼', 310500, 'ABOVE', '6월 30일(화) 멤버쉽 영상정리 / 한솔케미칼 (310,500원/325,000원/338,500원/346,500원)'),
  ('KR', '014680', '한솔케미칼', 325000, 'ABOVE', '6월 30일(화) 멤버쉽 영상정리 / 한솔케미칼 (310,500원/325,000원/338,500원/346,500원)'),
  ('KR', '014680', '한솔케미칼', 338500, 'ABOVE', '6월 30일(화) 멤버쉽 영상정리 / 한솔케미칼 (310,500원/325,000원/338,500원/346,500원)'),
  ('KR', '014680', '한솔케미칼', 346500, 'ABOVE', '6월 30일(화) 멤버쉽 영상정리 / 한솔케미칼 (310,500원/325,000원/338,500원/346,500원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- 2) public.price_alerts_log — HYONYHYONY · 4행 · 2026-06-30
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '014680', '한솔케미칼', 310500, 'ABOVE', '6월 30일(화) 멤버쉽 영상정리 / 한솔케미칼 (310,500원/325,000원/338,500원/346,500원)', DATE '2026-06-30'),
  ('HYONYHYONY', 'KR', '014680', '한솔케미칼', 325000, 'ABOVE', '6월 30일(화) 멤버쉽 영상정리 / 한솔케미칼 (310,500원/325,000원/338,500원/346,500원)', DATE '2026-06-30'),
  ('HYONYHYONY', 'KR', '014680', '한솔케미칼', 338500, 'ABOVE', '6월 30일(화) 멤버쉽 영상정리 / 한솔케미칼 (310,500원/325,000원/338,500원/346,500원)', DATE '2026-06-30'),
  ('HYONYHYONY', 'KR', '014680', '한솔케미칼', 346500, 'ABOVE', '6월 30일(화) 멤버쉽 영상정리 / 한솔케미칼 (310,500원/325,000원/338,500원/346,500원)', DATE '2026-06-30')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
