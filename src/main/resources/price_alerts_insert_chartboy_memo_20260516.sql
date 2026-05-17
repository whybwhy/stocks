-- 차트보이(CHARTBOY) 메모 — 2026-05-16
-- 종목코드: 삼성에스디에스 018260, 우주일렉트로 065680
-- 멱등: (symbol, target_price) 동일 행이 있으면 삽입 생략
-- 등록 시각 created_at: 2026-05-16 00:00 (Asia/Seoul)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source, created_at)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY',
       timestamptz '2026-05-16 00:00:00+09'
FROM (VALUES
  ('KR', '018260', '삼성에스디에스', 198800, 'ABOVE',
   '▶️ 198,800원 돌파시. (재매수 가능함)'),
  ('KR', '065680', '우주일렉트로', 46000, 'ABOVE',
   '▶️ 46,000원 돌파시 / 월봉 이동평균선 매매법')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
);
