-- 20260807 8월 7일(금) CHARTBOY 신규매수타점 · Asia/Seoul
-- 【seoul_log_date】2026-08-07
-- 【종목 코드】GS리테일 007070, 제이에스링크 127120, 코스맥스엔비티 222040,
--   코스맥스비티아이 044820, 펌텍코리아 251970, 휴젤 145020(네이버), 마녀공장 439090(레포)
-- 【제외】신규매수타점 서두 · 별 이모지
--
-- 공개 확인: stocks-ser4.onrender.com/m2vc32xupl/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '007070', 'GS리테일', 27500, 'ABOVE', 'GS리테일 / 네오팜 일봉모양 / 어제 고점 돌파시 1차. / 27.500원 돌파시 2차.'),
  ('KR', '127120', '제이에스링크', 41250, 'ABOVE', '제이에스링크 / 가운데자리 / 41.250원 돌파시 / 적자회사니깐, 큰 돈 들어가지 마셈.'),
  ('KR', '222040', '코스맥스엔비티', 9200, 'ABOVE', '코스맥스엔비티 / 일봉 컵위드핸들 모양 / 어제 고점 돌파시 1차. / 9.200원 돌파시 2차.'),
  ('KR', '044820', '코스맥스비티아이', 23900, 'ABOVE', '코스맥스비티아이 / 주봉 컵위드핸들모양 / 23.900원 돌파시 / 5일선 따라올라가는 돌파매매는, / 물렸다가 갈 수 있음.'),
  ('KR', '251970', '펌텍코리아', 50400, 'ABOVE', '펌텍코리아 / 240일선이 저항선 / 50.400원 돌파시'),
  ('KR', '145020', '휴젤', 290500, 'ABOVE', '휴젤 / abc의 b언덕 / 290.500원 돌파시'),
  ('KR', '439090', '마녀공장', 18510, 'ABOVE', '마녀공장 / 18.510원 돌파시 / 어제 윗꼬다리 없는 양봉이라. / 갭 뜨면..돌파매매 힘들다.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '007070', 'GS리테일', 27500, 'ABOVE', 'GS리테일 / 네오팜 일봉모양 / 어제 고점 돌파시 1차. / 27.500원 돌파시 2차.', DATE '2026-08-07'),
  ('CHARTBOY', 'KR', '127120', '제이에스링크', 41250, 'ABOVE', '제이에스링크 / 가운데자리 / 41.250원 돌파시 / 적자회사니깐, 큰 돈 들어가지 마셈.', DATE '2026-08-07'),
  ('CHARTBOY', 'KR', '222040', '코스맥스엔비티', 9200, 'ABOVE', '코스맥스엔비티 / 일봉 컵위드핸들 모양 / 어제 고점 돌파시 1차. / 9.200원 돌파시 2차.', DATE '2026-08-07'),
  ('CHARTBOY', 'KR', '044820', '코스맥스비티아이', 23900, 'ABOVE', '코스맥스비티아이 / 주봉 컵위드핸들모양 / 23.900원 돌파시 / 5일선 따라올라가는 돌파매매는, / 물렸다가 갈 수 있음.', DATE '2026-08-07'),
  ('CHARTBOY', 'KR', '251970', '펌텍코리아', 50400, 'ABOVE', '펌텍코리아 / 240일선이 저항선 / 50.400원 돌파시', DATE '2026-08-07'),
  ('CHARTBOY', 'KR', '145020', '휴젤', 290500, 'ABOVE', '휴젤 / abc의 b언덕 / 290.500원 돌파시', DATE '2026-08-07'),
  ('CHARTBOY', 'KR', '439090', '마녀공장', 18510, 'ABOVE', '마녀공장 / 18.510원 돌파시 / 어제 윗꼬다리 없는 양봉이라. / 갭 뜨면..돌파매매 힘들다.', DATE '2026-08-07')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
