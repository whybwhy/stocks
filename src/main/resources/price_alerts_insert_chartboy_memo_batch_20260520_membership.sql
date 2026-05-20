-- 차트보이(CHARTBOY) 메모 일괄 — 2026-05-20 멤버십 영상 정리
-- 멱등: (symbol, target_price) 동일 행이 있으면 삽입 생략
--
-- created_at 은 명시하지 않음 → DB 기본값 적용.
--
-- 종목코드: 삼화콘덴서 001820, 우주일렉트로 065680, 인텍플러스 064290, 코나아이 052400,
--   코오롱생명과학 102940, 토모큐브 475960

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '001820', '삼화콘덴서', 81800, 'ABOVE',
   '81,800원 돌파시 / 삼봉모양 / 주말 멤버십 5·20'),
  ('KR', '065680', '우주일렉트로', 46000, 'ABOVE',
   '46,000원 돌파시 / 주말 멤버십 5·20'),
  ('KR', '064290', '인텍플러스', 35100, 'ABOVE',
   '35,100원 돌파시 / 더듬이 모양 / 주말 멤버십 5·20'),
  ('KR', '052400', '코나아이', 72500, 'ABOVE',
   '72,500원 돌파시 / 주말 멤버십 5·20'),
  ('KR', '102940', '코오롱생명과학', 69100, 'ABOVE',
   '69,100원 돌파시 / 주말 멤버십 5·20'),
  ('KR', '475960', '토모큐브', 67800, 'ABOVE',
   '67,800원 돌파시 / 주말 멤버십 5·20')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
);
