-- 20260619 CHARTBOY 매수타점·종목체크 · Asia/Seoul
-- 【seoul_log_date】2026-06-19
-- 【종목 코드】104830 원익머트리얼즈, 034020 두산에너빌리티, 475480 SK이터닉스, 080220 제주반도체
-- 【제외】코스닥 일반 원칙(20·5일선 크로스·구름대·후행스팬) · 이엔에프/한솔케미칼(비교 코멘트만)
--
-- 공개 확인: stocks-ser4.onrender.com/171mggruf7/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 1행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '104830', '원익머트리얼즈', 51500, 'ABOVE', '원익머트리얼즈 / 51.500원 돌파시 / 이엔에프테크놀로지, 한솔케미칼이랑 같은 모양. 구름대에 걸려있는것들은 구름대를 넘어야 함.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 4행 · 2026-06-19
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '104830', '원익머트리얼즈', 51500, 'ABOVE', '원익머트리얼즈 / 51.500원 돌파시 / 이엔에프테크놀로지, 한솔케미칼이랑 같은 모양. 구름대에 걸려있는것들은 구름대를 넘어야 함.', DATE '2026-06-19'),
  ('CHARTBOY', 'KR', '034020', '두산에너빌리티', NULL, 'ABOVE', '두산에너빌리티 / 월봉 뚜껑 딴 껀..이미 지난달이고. 한전기술이나 두빌처럼, 년봉상 매수타점이 확실한 아이들은. 구름대 밑이라도..(월봉 뚜껑 땄어도) 년봉상 매수타점에서 시작할 수 있다고 함.', DATE '2026-06-19'),
  ('CHARTBOY', 'KR', '080220', '제주반도체', NULL, 'ABOVE', '제주반도체 / (이게 뭐가 있음? 계속 보자고 하노?) 전고점까지 갈 수 있더라도. 높아서..우린 못하니까..각자하셈. 저걸 지금 들어간다고??', DATE '2026-06-19'),
  ('CHARTBOY', 'KR', '475480', 'SK이터닉스', NULL, 'ABOVE', 'SK이터닉스 / 차트가 좋아보여서 어제 샀다는 분? 아니..화요일날 구름대를 못 넘고..무너진걸. 구름대 안에서 신규로 들어가는게 무슨 경우노? ..진짜..창조 매매 좀 하지 말자.😃', DATE '2026-06-19')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
