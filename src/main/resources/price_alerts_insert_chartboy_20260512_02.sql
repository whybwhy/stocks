-- 차트보이 메모 (source = CHARTBOY) — 2026-05-12 (#2)
-- ⭐️컵위드핸들: 로보티즈 394,000 / 에스비비테크 95,000 / DB 2,190·2,235
-- ⭐️좋은양봉: 삼성에스디에스 198,800 / 에이치브이엠 116,600 / LS에코에너지 94,300
-- 멱등: (symbol, target_price) 동일 행이 있으면 삽입 생략
-- 종목코드: 로보티즈 108490, 에스비비테크 389500, DB 012030,
--          삼성에스디에스 018260, 에이치브이엠 295310, LS에코에너지 229640

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '108490', '로보티즈', 394000, 'ABOVE',
   '394,000원 돌파시 / ⭐️컵위드핸들'),
  ('KR', '389500', '에스비비테크', 95000, 'ABOVE',
   '95,000원 돌파시 / ⭐️컵위드핸들'),
  ('KR', '012030', 'DB', 2190, 'ABOVE',
   '2,190원 돌파시 / ⭐️컵위드핸들'),
  ('KR', '012030', 'DB', 2235, 'ABOVE',
   '2,235원 돌파시 / ⭐️컵위드핸들'),
  ('KR', '018260', '삼성에스디에스', 198800, 'ABOVE',
   '198,800원 돌파시 / ⭐️좋은양봉'),
  ('KR', '295310', '에이치브이엠', 116600, 'ABOVE',
   '116,600원 돌파시 / ⭐️좋은양봉'),
  ('KR', '229640', 'LS에코에너지', 94300, 'ABOVE',
   '94,300원 돌파시 / ⭐️좋은양봉')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
);
