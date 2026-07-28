-- 20260728 7월 28일(화) CHARTBOY 매수타점·종목체크 · Asia/Seoul
-- 【seoul_log_date】2026-07-28
-- 【종목 코드】우리금융지주 316140(네이버·FnGuide), JB금융지주 175330(네이버),
--   달바글로벌 483650(레포), 코스맥스 192820(레포), 코스메카코리아 241710(네이버),
--   마키나락스 477850(레포), 동양생명 082640(미래에셋·KRX)
-- 【제외】어제 영상 가정 서두 · 같은 모양으로는 · 별 구분선 · 매수한 사람 없음 잔소리 · 동아지질 보유자 질문
--
-- 공개 확인: stocks-ser4.onrender.com/mrwceufkih/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 7행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '316140', '우리금융지주', 32900, 'ABOVE', '우리금융지주 / (연속캔들동일고점) / 32.900원 돌파시. / 이전 영상에서도 말했지만, / B파일수도 있다는 가정하에 들어갔어야 함.'),
  ('KR', '175330', 'JB금융지주', 28800, 'ABOVE', 'JB금융지주 / (작은 삼봉 언덕) / 28.800원/29.150원 돌파시 분할매수.'),
  ('KR', '175330', 'JB금융지주', 29150, 'ABOVE', 'JB금융지주 / (작은 삼봉 언덕) / 28.800원/29.150원 돌파시 분할매수.'),
  ('KR', '483650', '달바글로벌', 260000, 'ABOVE', '달바글로벌 / 260.000원 돌파시 우당탕 매수가능 / 60분봉상 상승조정 중 / (월봉 뚜껑 딴 종목)'),
  ('KR', '192820', '코스맥스', 189500, 'ABOVE', '코스맥스 / 어제 고점 돌파시(구름대 & 240일선) / 189.500원 돌파시가 삼봉언덕임.'),
  ('KR', '241710', '코스메카코리아', 93600, 'ABOVE', '코스메카코리아 / 93.600원이 메인타점 / 어제 고점 돌파시 정찰병 1주 가능.'),
  ('KR', '082640', '동양생명', 8250, 'ABOVE', '동양생명 / 코스맥스처럼 작은 삼봉 언덕도 되고, / 8.250원이 연속캔들 동일고점 맞는 듯.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 8행 · 2026-07-28
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '316140', '우리금융지주', 32900, 'ABOVE', '우리금융지주 / (연속캔들동일고점) / 32.900원 돌파시. / 이전 영상에서도 말했지만, / B파일수도 있다는 가정하에 들어갔어야 함.', DATE '2026-07-28'),
  ('CHARTBOY', 'KR', '175330', 'JB금융지주', 28800, 'ABOVE', 'JB금융지주 / (작은 삼봉 언덕) / 28.800원/29.150원 돌파시 분할매수.', DATE '2026-07-28'),
  ('CHARTBOY', 'KR', '175330', 'JB금융지주', 29150, 'ABOVE', 'JB금융지주 / (작은 삼봉 언덕) / 28.800원/29.150원 돌파시 분할매수.', DATE '2026-07-28'),
  ('CHARTBOY', 'KR', '483650', '달바글로벌', 260000, 'ABOVE', '달바글로벌 / 260.000원 돌파시 우당탕 매수가능 / 60분봉상 상승조정 중 / (월봉 뚜껑 딴 종목)', DATE '2026-07-28'),
  ('CHARTBOY', 'KR', '192820', '코스맥스', 189500, 'ABOVE', '코스맥스 / 어제 고점 돌파시(구름대 & 240일선) / 189.500원 돌파시가 삼봉언덕임.', DATE '2026-07-28'),
  ('CHARTBOY', 'KR', '241710', '코스메카코리아', 93600, 'ABOVE', '코스메카코리아 / 93.600원이 메인타점 / 어제 고점 돌파시 정찰병 1주 가능.', DATE '2026-07-28'),
  ('CHARTBOY', 'KR', '477850', '마키나락스', NULL, 'ABOVE', '마키나락스 / 물려있는 사람들은, / 오늘 깨작깨작 상승하면..다시 사더라도 익절하고~', DATE '2026-07-28'),
  ('CHARTBOY', 'KR', '082640', '동양생명', 8250, 'ABOVE', '동양생명 / 코스맥스처럼 작은 삼봉 언덕도 되고, / 8.250원이 연속캔들 동일고점 맞는 듯.', DATE '2026-07-28')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
