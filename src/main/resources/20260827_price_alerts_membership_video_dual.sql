-- 20260827 8월 27일(목) 영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-08-27
-- 【종목 코드】POSCO홀딩스 005490, LG화학 051910, LG에너지솔루션 373220, 에코프로비엠 247540,
--   에코프로 086520, 주성엔지니어링 036930, 삼성E&A 028050, HL만도 204320,
--   현대차 005380, 기아 000270, 알테오젠 196170  (전부 레포 기존 배치 SQL)
--
-- 공개 확인: stocks-ser4.onrender.com/4k87oj5ia4/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '005490', 'POSCO홀딩스', 337000, 'ABOVE', '8월 27일(목) 영상정리 / POSCO홀딩스 (337,000원/344,250원)'),
  ('KR', '005490', 'POSCO홀딩스', 344250, 'ABOVE', '8월 27일(목) 영상정리 / POSCO홀딩스 (337,000원/344,250원)'),
  ('KR', '051910', 'LG화학', 282500, 'ABOVE', '8월 27일(목) 영상정리 / LG화학 (282,500원)'),
  ('KR', '373220', 'LG에너지솔루션', 371500, 'ABOVE', '8월 27일(목) 영상정리 / LG에너지솔루션 (371,500원/372,000원)'),
  ('KR', '373220', 'LG에너지솔루션', 372000, 'ABOVE', '8월 27일(목) 영상정리 / LG에너지솔루션 (371,500원/372,000원)'),
  ('KR', '247540', '에코프로비엠', 120200, 'ABOVE', '8월 27일(목) 영상정리 / 에코프로비엠 (120,200원)'),
  ('KR', '086520', '에코프로', 95800, 'ABOVE', '8월 27일(목) 영상정리 / 에코프로 (95,800원)'),
  ('KR', '036930', '주성엔지니어링', 196200, 'ABOVE', '8월 27일(목) 영상정리 / 주성엔지니어링 (196,200원)'),
  ('KR', '028050', '삼성E&A', 53500, 'ABOVE', '8월 27일(목) 영상정리 / 삼성E&A (53,500원)'),
  ('KR', '204320', 'HL만도', 58000, 'ABOVE', '8월 27일(목) 영상정리 / HL만도 (58,000원)'),
  ('KR', '005380', '현대차', 459500, 'ABOVE', '8월 27일(목) 영상정리 / 현대차 (459,500원)'),
  ('KR', '000270', '기아', 142400, 'ABOVE', '8월 27일(목) 영상정리 / 기아 (142,400원)'),
  ('KR', '196170', '알테오젠', 344000, 'ABOVE', '8월 27일(목) 영상정리 / 알테오젠 (344,000원/356,000원)'),
  ('KR', '196170', '알테오젠', 356000, 'ABOVE', '8월 27일(목) 영상정리 / 알테오젠 (344,000원/356,000원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '005490', 'POSCO홀딩스', 337000, 'ABOVE', '8월 27일(목) 영상정리 / POSCO홀딩스 (337,000원/344,250원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '005490', 'POSCO홀딩스', 344250, 'ABOVE', '8월 27일(목) 영상정리 / POSCO홀딩스 (337,000원/344,250원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '051910', 'LG화학', 282500, 'ABOVE', '8월 27일(목) 영상정리 / LG화학 (282,500원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '373220', 'LG에너지솔루션', 371500, 'ABOVE', '8월 27일(목) 영상정리 / LG에너지솔루션 (371,500원/372,000원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '373220', 'LG에너지솔루션', 372000, 'ABOVE', '8월 27일(목) 영상정리 / LG에너지솔루션 (371,500원/372,000원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '247540', '에코프로비엠', 120200, 'ABOVE', '8월 27일(목) 영상정리 / 에코프로비엠 (120,200원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '086520', '에코프로', 95800, 'ABOVE', '8월 27일(목) 영상정리 / 에코프로 (95,800원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '036930', '주성엔지니어링', 196200, 'ABOVE', '8월 27일(목) 영상정리 / 주성엔지니어링 (196,200원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '028050', '삼성E&A', 53500, 'ABOVE', '8월 27일(목) 영상정리 / 삼성E&A (53,500원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '204320', 'HL만도', 58000, 'ABOVE', '8월 27일(목) 영상정리 / HL만도 (58,000원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '005380', '현대차', 459500, 'ABOVE', '8월 27일(목) 영상정리 / 현대차 (459,500원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '000270', '기아', 142400, 'ABOVE', '8월 27일(목) 영상정리 / 기아 (142,400원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '196170', '알테오젠', 344000, 'ABOVE', '8월 27일(목) 영상정리 / 알테오젠 (344,000원/356,000원)', DATE '2026-08-27'),
  ('HYONYHYONY', 'KR', '196170', '알테오젠', 356000, 'ABOVE', '8월 27일(목) 영상정리 / 알테오젠 (344,000원/356,000원)', DATE '2026-08-27')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
