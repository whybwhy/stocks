-- 차트보이 소스 (source = CHARTBOY) — 2026-02-28 배치
-- 멱등: symbol + target_price 중복 시 미삽입
-- 종목코드: HDC현대산업개발 294870, TIGER차이나전기차 371460, 효성티앤씨 298020,
--   계룡건설 013580, 삼호개발 010960, 코스맥스 192820, 한국콜마 161890, 제룡전기 033100

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18,2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '294870', 'HDC현대산업개발',              24800, 'ABOVE', '차트보이 / 24,800원 돌파'),
  ('KR', '371460', 'TIGER 차이나전기차SOLACTIVE', 13663, 'ABOVE', '차트보이 / 13,663원 돌파'),
  ('KR', '298020', '효성티앤씨',                  421500, 'ABOVE', '차트보이 / 421,500원 돌파'),
  ('KR', '013580', '계룡건설',                    31750, 'ABOVE', '차트보이 / 31,750원 돌파'),
  ('KR', '010960', '삼호개발',                     4315, 'ABOVE', '차트보이 / 4,315원 돌파'),
  ('KR', '192820', '코스맥스',                   210000, 'ABOVE', '차트보이 / 210,000원 돌파'),
  ('KR', '161890', '한국콜마',                    77900, 'ABOVE', '차트보이 / 77,900원 돌파'),
  ('KR', '033100', '제룡전기',                    62200, 'ABOVE', '차트보이 / 62,200원 돌파')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18,2)
);
