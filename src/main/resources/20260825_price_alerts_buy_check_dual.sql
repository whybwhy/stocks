-- 20260825 8월 25일(화) CHARTBOY 매수타점 · Asia/Seoul
-- 【seoul_log_date】2026-08-25
-- 【블록】✅ 어제 영상 체크 · ✅ 2차전지 진입타점
-- 【종목 코드】에이피알 278470, 실리콘투 257720, GS건설 006360, 서진시스템 178320,
--   대한광통신 010170, 코웨이 021240, 한올바이오파마 009420, KODEX 2차전지산업 305720,
--   POSCO홀딩스 005490, LG에너지솔루션 373220, LG화학 051910, 에코프로비엠 247540,
--   에코프로 086520, 삼성SDI 006400  (전부 레포 기존 배치 SQL)
-- 【제외】차트보입니다 인사 서두
-- 【참고】같은 메시지의 🌈8월 24일(월) 영상정리 블록은 20260824_price_alerts_membership_video_dual.sql 로 이미 적재됨
--
-- 공개 확인: stocks-ser4.onrender.com/mjn98nnisx/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '278470', '에이피알', 415000, 'ABOVE', '에이피알 / 어제 영상 체크 / 415.000원 돌파시 1차 / 424.250원 돌파시 2차'),
  ('KR', '278470', '에이피알', 424250, 'ABOVE', '에이피알 / 어제 영상 체크 / 415.000원 돌파시 1차 / 424.250원 돌파시 2차'),
  ('KR', '257720', '실리콘투', 50500, 'ABOVE', '실리콘투 / 어제 영상 체크 / 50.500원 돌파시 1차 / 50.800원 돌파시 2차'),
  ('KR', '257720', '실리콘투', 50800, 'ABOVE', '실리콘투 / 어제 영상 체크 / 50.500원 돌파시 1차 / 50.800원 돌파시 2차'),
  ('KR', '006360', 'GS건설', 36800, 'ABOVE', 'GS건설 / 어제 영상 체크 / 36.800원 돌파시'),
  ('KR', '178320', '서진시스템', 40400, 'ABOVE', '서진시스템 / 어제 영상 체크 / 40.400원 돌파시'),
  ('KR', '010170', '대한광통신', 14220, 'ABOVE', '대한광통신 / 어제 영상 체크 / 14.220원 돌파시'),
  ('KR', '021240', '코웨이', 99100, 'ABOVE', '코웨이 / 어제 영상 체크 / 99.100원 돌파시'),
  ('KR', '009420', '한올바이오파마', 63000, 'ABOVE', '한올바이오파마 / 어제 영상 체크 / 63.000원 돌파시'),
  ('KR', '305720', 'KODEX 2차전지산업', 14885, 'ABOVE', 'KODEX 2차전지산업 / 2차전지 진입타점 / 14.885원 돌파시'),
  ('KR', '005490', 'POSCO홀딩스', 337000, 'ABOVE', 'POSCO홀딩스 / 2차전지 진입타점 / 337.000원 돌파시'),
  ('KR', '373220', 'LG에너지솔루션', 371500, 'ABOVE', 'LG에너지솔루션 / 2차전지 진입타점 / 371.500원 돌파시'),
  ('KR', '051910', 'LG화학', 282500, 'ABOVE', 'LG화학 / 2차전지 진입타점 / 282.500원 돌파시'),
  ('KR', '247540', '에코프로비엠', 120200, 'ABOVE', '에코프로비엠 / 2차전지 진입타점 / 120.200원 돌파시'),
  ('KR', '086520', '에코프로', 95800, 'ABOVE', '에코프로 / 2차전지 진입타점 / 95.800원 돌파시'),
  ('KR', '006400', '삼성SDI', 518000, 'ABOVE', '삼성SDI / 2차전지 진입타점 / 518.000원 돌파시 1차 / 522.000원 돌파시 2차'),
  ('KR', '006400', '삼성SDI', 522000, 'ABOVE', '삼성SDI / 2차전지 진입타점 / 518.000원 돌파시 1차 / 522.000원 돌파시 2차')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '278470', '에이피알', 415000, 'ABOVE', '에이피알 / 어제 영상 체크 / 415.000원 돌파시 1차 / 424.250원 돌파시 2차', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '278470', '에이피알', 424250, 'ABOVE', '에이피알 / 어제 영상 체크 / 415.000원 돌파시 1차 / 424.250원 돌파시 2차', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '257720', '실리콘투', 50500, 'ABOVE', '실리콘투 / 어제 영상 체크 / 50.500원 돌파시 1차 / 50.800원 돌파시 2차', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '257720', '실리콘투', 50800, 'ABOVE', '실리콘투 / 어제 영상 체크 / 50.500원 돌파시 1차 / 50.800원 돌파시 2차', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '006360', 'GS건설', 36800, 'ABOVE', 'GS건설 / 어제 영상 체크 / 36.800원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '178320', '서진시스템', 40400, 'ABOVE', '서진시스템 / 어제 영상 체크 / 40.400원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '010170', '대한광통신', 14220, 'ABOVE', '대한광통신 / 어제 영상 체크 / 14.220원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '021240', '코웨이', 99100, 'ABOVE', '코웨이 / 어제 영상 체크 / 99.100원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '009420', '한올바이오파마', 63000, 'ABOVE', '한올바이오파마 / 어제 영상 체크 / 63.000원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '305720', 'KODEX 2차전지산업', 14885, 'ABOVE', 'KODEX 2차전지산업 / 2차전지 진입타점 / 14.885원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '005490', 'POSCO홀딩스', 337000, 'ABOVE', 'POSCO홀딩스 / 2차전지 진입타점 / 337.000원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '373220', 'LG에너지솔루션', 371500, 'ABOVE', 'LG에너지솔루션 / 2차전지 진입타점 / 371.500원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '051910', 'LG화학', 282500, 'ABOVE', 'LG화학 / 2차전지 진입타점 / 282.500원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '247540', '에코프로비엠', 120200, 'ABOVE', '에코프로비엠 / 2차전지 진입타점 / 120.200원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '086520', '에코프로', 95800, 'ABOVE', '에코프로 / 2차전지 진입타점 / 95.800원 돌파시', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '006400', '삼성SDI', 518000, 'ABOVE', '삼성SDI / 2차전지 진입타점 / 518.000원 돌파시 1차 / 522.000원 돌파시 2차', DATE '2026-08-25'),
  ('CHARTBOY', 'KR', '006400', '삼성SDI', 522000, 'ABOVE', '삼성SDI / 2차전지 진입타점 / 518.000원 돌파시 1차 / 522.000원 돌파시 2차', DATE '2026-08-25')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
