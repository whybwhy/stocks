-- 20260831 CHARTBOY 매수타점 · Asia/Seoul
-- 【seoul_log_date】2026-08-31
-- 【종목 코드】LG생활건강 051900 (외부 조회 · FnGuide/밸류라인 — 레포에 기존 코드 없음)
-- 【작성자 판별】원문이 1️⃣ 로 시작해 ✔·🌈 규칙에 걸리지 않음 → 말투·「돌파시」 형식 기준 CHARTBOY 로 적재
--
-- 공개 확인: stocks-ser4.onrender.com/5x2ypfo8pk/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '051900', 'LG생활건강', 321500, 'ABOVE', 'LG생활건강 / 321.500원 돌파시 / 크게 보면 우리가 선호하지 않는모양. / 단기로 보면..쌍바닥 언덕임. / 돌파하면, 누가 1주만 사서..단타해보셈.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '051900', 'LG생활건강', 321500, 'ABOVE', 'LG생활건강 / 321.500원 돌파시 / 크게 보면 우리가 선호하지 않는모양. / 단기로 보면..쌍바닥 언덕임. / 돌파하면, 누가 1주만 사서..단타해보셈.', DATE '2026-08-31')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
