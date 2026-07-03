-- 20260702 CHARTBOY 매수타점 체크 · Asia/Seoul
-- 【seoul_log_date】2026-07-02

-- 1) public.price_alerts — 멱등 · 11행
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18,  2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '475480', 'SK이터닉스', 61900, 'ABOVE', 'SK이터닉스 / (어제영상 참조) / 61.900원/64.200원 돌파시'),
  ('KR', '475480', 'SK이터닉스', 64200, 'ABOVE', 'SK이터닉스 / (어제영상 참조) / 61.900원/64.200원 돌파시'),
  ('KR', '021240', '코웨이', 95300, 'ABOVE', '코웨이 / (어제영상 참조) / 95.300원 돌파시 정찰병'),
  ('KR', '021240', '코웨이', 99100, 'ABOVE', '코웨이 / (어제영상 참조) / 99.100원 돌파시'),
  ('KR', '131290', '티에스이', 282500, 'ABOVE', '티에스이 / (심텍이랑 같은자리) / 282.500원 돌파시 / 신고가 돌파매매는 욕심내지 마셈'),
  ('KR', '005430', '한국공항', 90000, 'ABOVE', '한국공항 / (연속캔들 동일고점) / 90.000원 돌파시부터'),
  ('KR', '161890', '한국콜마', 106200, 'ABOVE', '한국콜마 / (어제영상 참조) / 106.200원/110.700원 돌파시'),
  ('KR', '161890', '한국콜마', 110700, 'ABOVE', '한국콜마 / (어제영상 참조) / 106.200원/110.700원 돌파시'),
  ('KR', '014680', '한솔케미칼', 310500, 'ABOVE', '한솔케미칼 / 310.500원 돌파시 재매수'),
  ('KR', '005440', '현대지에프홀딩스', 16550, 'ABOVE', '현대지에프홀딩스 / 16.550원/16.940원 돌파시'),
  ('KR', '005440', '현대지에프홀딩스', 16940, 'ABOVE', '현대지에프홀딩스 / 16.550원/16.940원 돌파시')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- 2) public.price_alerts_log — CHARTBOY · 11행 · 2026-07-02
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '475480', 'SK이터닉스', 61900, 'ABOVE', 'SK이터닉스 / (어제영상 참조) / 61.900원/64.200원 돌파시', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '475480', 'SK이터닉스', 64200, 'ABOVE', 'SK이터닉스 / (어제영상 참조) / 61.900원/64.200원 돌파시', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '021240', '코웨이', 95300, 'ABOVE', '코웨이 / (어제영상 참조) / 95.300원 돌파시 정찰병', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '021240', '코웨이', 99100, 'ABOVE', '코웨이 / (어제영상 참조) / 99.100원 돌파시', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '131290', '티에스이', 282500, 'ABOVE', '티에스이 / (심텍이랑 같은자리) / 282.500원 돌파시 / 신고가 돌파매매는 욕심내지 마셈', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '005430', '한국공항', 90000, 'ABOVE', '한국공항 / (연속캔들 동일고점) / 90.000원 돌파시부터', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '161890', '한국콜마', 106200, 'ABOVE', '한국콜마 / (어제영상 참조) / 106.200원/110.700원 돌파시', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '161890', '한국콜마', 110700, 'ABOVE', '한국콜마 / (어제영상 참조) / 106.200원/110.700원 돌파시', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '014680', '한솔케미칼', 310500, 'ABOVE', '한솔케미칼 / 310.500원 돌파시 재매수', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '005440', '현대지에프홀딩스', 16550, 'ABOVE', '현대지에프홀딩스 / 16.550원/16.940원 돌파시', DATE '2026-07-02'),
  ('CHARTBOY', 'KR', '005440', '현대지에프홀딩스', 16940, 'ABOVE', '현대지에프홀딩스 / 16.550원/16.940원 돌파시', DATE '2026-07-02')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
