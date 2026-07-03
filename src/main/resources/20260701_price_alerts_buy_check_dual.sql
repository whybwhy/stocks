-- 20260701 CHARTBOY 매수타점 체크 · Asia/Seoul
-- 【seoul_log_date】2026-07-01

-- 1) public.price_alerts — 멱등 · 2행
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '041830', '인바디', 48900, 'ABOVE', '인바디 / 오늘 빵빵빵으로 넘어야 함 / 48.900원 돌파시'),
  ('KR', '0193T0', 'KODEX SK하이닉스단일종목레버리지', 44385, 'ABOVE', 'KODEX SK하이닉스단일종목레버리지 / 44.385원 돌파시 (어제 영상 참조) / (sk스퀘어 모양의 컵위드핸들 응용형)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- 2) public.price_alerts_log — CHARTBOY · 6행 · 2026-07-01
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '041830', '인바디', 48900, 'ABOVE', '인바디 / 오늘 빵빵빵으로 넘어야 함 / 48.900원 돌파시', DATE '2026-07-01'),
  ('CHARTBOY', 'KR', '0193T0', 'KODEX SK하이닉스단일종목레버리지', 44385, 'ABOVE', 'KODEX SK하이닉스단일종목레버리지 / 44.385원 돌파시 (어제 영상 참조) / (sk스퀘어 모양의 컵위드핸들 응용형)', DATE '2026-07-01'),
  ('CHARTBOY', 'KR', '057050', '현대홈쇼핑', NULL, 'ABOVE', '현대홈쇼핑 / 언덕을 넘는 어제 사는 날이 맞음. 2월달 월봉 고점이, 단기 3파인지 5파인지 명확하지 않음. 2월달 고점 근방에서 팔 마음으로, 욕심내지 않고, 단타로 접근해볼수 있음.', DATE '2026-07-01'),
  ('CHARTBOY', 'KR', '005440', '현대지에프홀딩스', NULL, 'ABOVE', '현대지에프홀딩스 / 언덕을 넘는 어제 사는 날이 맞음. 2월달 월봉 고점이, 단기 3파인지 5파인지 명확하지 않음. 2월달 고점 근방에서 팔 마음으로, 욕심내지 않고, 단타로 접근해볼수 있음.', DATE '2026-07-01'),
  ('CHARTBOY', 'KR', '483650', '달바글로벌', NULL, 'ABOVE', '달바글로벌 / 결국 같은 메카니즘의 매수타점임. 어제 마지막 영상 참조.', DATE '2026-07-01'),
  ('CHARTBOY', 'KR', '003490', '대한항공', NULL, 'ABOVE', '대한항공 / 결국 같은 메카니즘의 매수타점임. 어제 마지막 영상 참조.', DATE '2026-07-01')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
