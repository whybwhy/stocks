-- 20260806 8월 6일(목) 멤버쉽영상 정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-08-06
-- 【종목 코드】마녀공장 439090, 실리콘투 257720, 에이피알 278470, 제닉 123330,
--   펌텍코리아 251970, GS리테일 007070, GS건설 006360, 로보티즈 108490(레포),
--   제이에스링크 127120(네이버), 코리안리 003690(핀업·KRX), 코스맥스엔비티 222040(네이버)
--
-- 공개 확인: stocks-ser4.onrender.com/m2vc32xupl/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '439090', '마녀공장', 18000, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 마녀공장 (18,000원/18,510원)'),
  ('KR', '439090', '마녀공장', 18510, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 마녀공장 (18,000원/18,510원)'),
  ('KR', '257720', '실리콘투', 40450, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 실리콘투 (40,450원/50,500원)'),
  ('KR', '257720', '실리콘투', 50500, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 실리콘투 (40,450원/50,500원)'),
  ('KR', '278470', '에이피알', 433500, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 에이피알 (433,500원)'),
  ('KR', '123330', '제닉', 29550, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 제닉 (29,550원)'),
  ('KR', '251970', '펌텍코리아', 50400, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 펌텍코리아 (50,400원)'),
  ('KR', '007070', 'GS리테일', 26750, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / GS리테일 (26,750원/27,500원)'),
  ('KR', '007070', 'GS리테일', 27500, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / GS리테일 (26,750원/27,500원)'),
  ('KR', '006360', 'GS건설', 35750, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / GS건설 (35,750원)'),
  ('KR', '108490', '로보티즈', 244500, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 로보티즈 (244,500원)'),
  ('KR', '127120', '제이에스링크', 41250, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 제이에스링크 (41,250원)'),
  ('KR', '003690', '코리안리', 14920, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 코리안리 (14,920원)'),
  ('KR', '222040', '코스맥스엔비티', 9200, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 코스맥스엔비티 (9,200원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '439090', '마녀공장', 18000, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 마녀공장 (18,000원/18,510원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '439090', '마녀공장', 18510, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 마녀공장 (18,000원/18,510원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '257720', '실리콘투', 40450, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 실리콘투 (40,450원/50,500원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '257720', '실리콘투', 50500, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 실리콘투 (40,450원/50,500원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '278470', '에이피알', 433500, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 에이피알 (433,500원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '123330', '제닉', 29550, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 제닉 (29,550원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '251970', '펌텍코리아', 50400, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 펌텍코리아 (50,400원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '007070', 'GS리테일', 26750, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / GS리테일 (26,750원/27,500원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '007070', 'GS리테일', 27500, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / GS리테일 (26,750원/27,500원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '006360', 'GS건설', 35750, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / GS건설 (35,750원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '108490', '로보티즈', 244500, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 로보티즈 (244,500원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '127120', '제이에스링크', 41250, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 제이에스링크 (41,250원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '003690', '코리안리', 14920, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 코리안리 (14,920원)', DATE '2026-08-06'),
  ('HYONYHYONY', 'KR', '222040', '코스맥스엔비티', 9200, 'ABOVE', '8월 6일(목) 멤버쉽영상 정리 / 코스맥스엔비티 (9,200원)', DATE '2026-08-06')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
