-- 차트보이 메모 (source = CHARTBOY) — 2026-05-12
-- 멱등: (symbol, target_price) 동일 행이 있으면 삽입 생략
--
-- 종목코드: 삼성에스디에스 018260, DB 012030, 에이치브이엠 295310

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '018260', '삼성에스디에스', 198800, 'ABOVE',
   '198,800원 돌파시 / 어제 영상 참조'),
  ('KR', '012030', 'DB', 2190, 'ABOVE',
   '2,190원 돌파시 / 컵위드핸들 아래 언덕'),
  ('KR', '012030', 'DB', 2235, 'ABOVE',
   '2,235원 돌파시 / 컵위드핸들 아래 언덕'),
  ('KR', '295310', '에이치브이엠', 107900, 'ABOVE',
   '107,900원 돌파시 / 현재 보유종목'),
  ('KR', '295310', '에이치브이엠', 116600, 'ABOVE',
   '116,600원 돌파시 / 현재 보유종목')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
);
