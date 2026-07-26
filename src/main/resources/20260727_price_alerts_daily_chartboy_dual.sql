-- 20260727 CHARTBOY 일일 지수·종목체크 · Asia/Seoul
-- 【seoul_log_date】2026-07-27
-- 【종목 코드】SK오션플랜트 100090(레포), 지엔씨에너지 119850(네이버·KRX)
-- 【지수】0001 코스피, 1001 코스닥 (KIS 지수코드 · log NULL)
--
-- 공개 확인: stocks-ser4.onrender.com/l5bfd3n9ww/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '100090', 'SK오션플랜트', 21800, 'ABOVE', 'SK오션플랜트 / 21.800원 돌파시 가능하다. / 240일선도 넘으면서 나오는 첫번째 언덕.'),
  ('KR', '119850', '지엔씨에너지', 46750, 'ABOVE', '지엔씨에너지 / 46.750원 신고가 돌파매매 물어보신 분?? / 43.450원(2월고점) 깨지면, / 손절하겠다는 마음으론 도전해 볼 수 있음. / 여태 뭐하다가..이제와서 들어가겠다고 하노? ..에휴..'),
  ('KR', '119850', '지엔씨에너지', 43450, 'BELOW', '지엔씨에너지 / 46.750원 신고가 돌파매매 물어보신 분?? / 43.450원(2월고점) 깨지면, / 손절하겠다는 마음으론 도전해 볼 수 있음. / 여태 뭐하다가..이제와서 들어가겠다고 하노? ..에휴..')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '0001', '코스피', NULL, 'ABOVE', '코스피 / 7월14일 21일 쌍바닥을 깨지 않아야 한다. / 다음달부터 상승을 바란다면, / C가 한번 뙇! 한번 나와야 할 듯. / C가 안 보인다.', DATE '2026-07-27'),
  ('CHARTBOY', 'KR', '1001', '코스닥', NULL, 'ABOVE', '코스닥 / 이번달은 음봉이라고 생각하면 편하다.', DATE '2026-07-27'),
  ('CHARTBOY', 'KR', '100090', 'SK오션플랜트', 21800, 'ABOVE', 'SK오션플랜트 / 21.800원 돌파시 가능하다. / 240일선도 넘으면서 나오는 첫번째 언덕.', DATE '2026-07-27'),
  ('CHARTBOY', 'KR', '119850', '지엔씨에너지', 46750, 'ABOVE', '지엔씨에너지 / 46.750원 신고가 돌파매매 물어보신 분?? / 43.450원(2월고점) 깨지면, / 손절하겠다는 마음으론 도전해 볼 수 있음. / 여태 뭐하다가..이제와서 들어가겠다고 하노? ..에휴..', DATE '2026-07-27'),
  ('CHARTBOY', 'KR', '119850', '지엔씨에너지', 43450, 'BELOW', '지엔씨에너지 / 46.750원 신고가 돌파매매 물어보신 분?? / 43.450원(2월고점) 깨지면, / 손절하겠다는 마음으론 도전해 볼 수 있음. / 여태 뭐하다가..이제와서 들어가겠다고 하노? ..에휴..', DATE '2026-07-27')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
