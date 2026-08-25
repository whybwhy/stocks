-- 20260820 8월 20일(목) CHARTBOY 매수타점 · Asia/Seoul
-- 【seoul_log_date】2026-08-20
-- 【종목 코드】GS건설 006360, LIG아큐버 073490, OCI홀딩스 010060, 서진시스템 178320(레포),
--   오름테라퓨틱 475830(토스), 우리기술 032820(네이버), 코미팜 041960(네이버), 현대건설 000720(레포)
-- 【제외】어제 멤버십 영상 참조 서두 · 인사 이모지
--
-- 공개 확인: stocks-ser4.onrender.com/mjn98nnisx/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '006360', 'GS건설', 36650, 'ABOVE', 'GS건설 / 연속캔들동일고점 / 36.650원 돌파시.'),
  ('KR', '073490', 'LIG아큐버', 33650, 'ABOVE', 'LIG아큐버 / 33.650원 돌파시'),
  ('KR', '010060', 'OCI홀딩스', 293500, 'ABOVE', 'OCI홀딩스 / 293.500원 돌파시'),
  ('KR', '178320', '서진시스템', 40200, 'ABOVE', '서진시스템 / 40.200원 돌파시'),
  ('KR', '475830', '오름테라퓨틱', 75700, 'ABOVE', '오름테라퓨틱 / 75.700원 돌파시'),
  ('KR', '032820', '우리기술', 12300, 'ABOVE', '우리기술 / 12.300원 돌파시'),
  ('KR', '041960', '코미팜', 9420, 'ABOVE', '코미팜 / 9.420원 돌파시 1차 / 9.470원 돌파시 2차'),
  ('KR', '041960', '코미팜', 9470, 'ABOVE', '코미팜 / 9.420원 돌파시 1차 / 9.470원 돌파시 2차'),
  ('KR', '000720', '현대건설', 121700, 'ABOVE', '현대건설 / 121.700원 돌파시')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '006360', 'GS건설', 36650, 'ABOVE', 'GS건설 / 연속캔들동일고점 / 36.650원 돌파시.', DATE '2026-08-20'),
  ('CHARTBOY', 'KR', '073490', 'LIG아큐버', 33650, 'ABOVE', 'LIG아큐버 / 33.650원 돌파시', DATE '2026-08-20'),
  ('CHARTBOY', 'KR', '010060', 'OCI홀딩스', 293500, 'ABOVE', 'OCI홀딩스 / 293.500원 돌파시', DATE '2026-08-20'),
  ('CHARTBOY', 'KR', '178320', '서진시스템', 40200, 'ABOVE', '서진시스템 / 40.200원 돌파시', DATE '2026-08-20'),
  ('CHARTBOY', 'KR', '475830', '오름테라퓨틱', 75700, 'ABOVE', '오름테라퓨틱 / 75.700원 돌파시', DATE '2026-08-20'),
  ('CHARTBOY', 'KR', '032820', '우리기술', 12300, 'ABOVE', '우리기술 / 12.300원 돌파시', DATE '2026-08-20'),
  ('CHARTBOY', 'KR', '041960', '코미팜', 9420, 'ABOVE', '코미팜 / 9.420원 돌파시 1차 / 9.470원 돌파시 2차', DATE '2026-08-20'),
  ('CHARTBOY', 'KR', '041960', '코미팜', 9470, 'ABOVE', '코미팜 / 9.420원 돌파시 1차 / 9.470원 돌파시 2차', DATE '2026-08-20'),
  ('CHARTBOY', 'KR', '000720', '현대건설', 121700, 'ABOVE', '현대건설 / 121.700원 돌파시', DATE '2026-08-20')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
