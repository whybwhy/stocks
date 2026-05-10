-- 차트보이: 자동차주 매수타점 리체크 (source = CHARTBOY)
-- 멱등: (symbol, target_price) 동일 행이 있으면 삽입 생략
--
-- 종목코드: 현대모비스 012330, 현대위아 011210, 현대차 005380, 기아 000270, HL만도 204320

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '012330', '현대모비스', 531000, 'ABOVE',
   '531,000원 돌파시 / 신고가 삼봉 돌파매매'),
  ('KR', '011210', '현대위아', 98000, 'ABOVE',
   '98,000원 돌파시 / 금요일 고점'),
  ('KR', '005380', '현대차', 647000, 'ABOVE',
   '647,000원 돌파시 / 금요일 고점'),
  ('KR', '000270', '기아', 170300, 'ABOVE',
   '170,300원 돌파시 / 금요일 고점 (구름대 돌파 필요)'),
  ('KR', '204320', 'HL만도', 62700, 'ABOVE',
   '62,700원 돌파시 / 금요일 고점')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
);
