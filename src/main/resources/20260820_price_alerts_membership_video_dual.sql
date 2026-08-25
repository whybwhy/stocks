-- 20260820 8월 20일(목) 영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-08-20
-- 【종목 코드】우리기술 032820, 빙그레 005180, 알테오젠 196170,
--   티엑스알로보틱스 484810(네이버), 현대코퍼레이션 011760
--
-- 공개 확인: stocks-ser4.onrender.com/mjn98nnisx/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '032820', '우리기술', 12300, 'ABOVE', '8월 20일(목) 영상정리 / 우리기술 (12,300원)'),
  ('KR', '005180', '빙그레', 87400, 'ABOVE', '8월 20일(목) 영상정리 / 빙그레 (87,400원)'),
  ('KR', '196170', '알테오젠', 356000, 'ABOVE', '8월 20일(목) 영상정리 / 알테오젠 (356,000원)'),
  ('KR', '484810', '티엑스알로보틱스', 19200, 'ABOVE', '8월 20일(목) 영상정리 / 티엑스알로보틱스 (19,200원/20,150원)'),
  ('KR', '484810', '티엑스알로보틱스', 20150, 'ABOVE', '8월 20일(목) 영상정리 / 티엑스알로보틱스 (19,200원/20,150원)'),
  ('KR', '011760', '현대코퍼레이션', 28050, 'ABOVE', '8월 20일(목) 영상정리 / 현대코퍼레이션 (28,050원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '032820', '우리기술', 12300, 'ABOVE', '8월 20일(목) 영상정리 / 우리기술 (12,300원)', DATE '2026-08-20'),
  ('HYONYHYONY', 'KR', '005180', '빙그레', 87400, 'ABOVE', '8월 20일(목) 영상정리 / 빙그레 (87,400원)', DATE '2026-08-20'),
  ('HYONYHYONY', 'KR', '196170', '알테오젠', 356000, 'ABOVE', '8월 20일(목) 영상정리 / 알테오젠 (356,000원)', DATE '2026-08-20'),
  ('HYONYHYONY', 'KR', '484810', '티엑스알로보틱스', 19200, 'ABOVE', '8월 20일(목) 영상정리 / 티엑스알로보틱스 (19,200원/20,150원)', DATE '2026-08-20'),
  ('HYONYHYONY', 'KR', '484810', '티엑스알로보틱스', 20150, 'ABOVE', '8월 20일(목) 영상정리 / 티엑스알로보틱스 (19,200원/20,150원)', DATE '2026-08-20'),
  ('HYONYHYONY', 'KR', '011760', '현대코퍼레이션', 28050, 'ABOVE', '8월 20일(목) 영상정리 / 현대코퍼레이션 (28,050원)', DATE '2026-08-20')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
