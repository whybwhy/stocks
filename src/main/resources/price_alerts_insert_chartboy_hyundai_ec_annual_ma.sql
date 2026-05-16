-- 현대건설(000720) — 연봉 메모 (배치본과 동일 라벨)
-- 전체 일괄: price_alerts_insert_chartboy_memo_batch_20260515.sql
-- 멱등: (symbol, target_price) 동일 행이 있으면 삽입 생략
-- 등록 시각 created_at: 2026-05-15 00:00 (Asia/Seoul)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source, created_at)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY',
       timestamptz '2026-05-15 00:00:00+09'
FROM (VALUES
  ('KR', '000720', '현대건설', 198400, 'ABOVE',
   '198,400원 돌파시 / 삼봉모양 · ✔️절대 미리 매수하지 마세요!'),
  ('KR', '000720', '현대건설', 204815, 'ABOVE',
   '204,815원 돌파시 / 메인 매수타점(연봉 전 음봉 시가)·연봉')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
);
