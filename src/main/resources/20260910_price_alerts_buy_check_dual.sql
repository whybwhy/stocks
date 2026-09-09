-- 20260910 CHARTBOY 매수타점 체크 · Asia/Seoul
-- 【용도】Supabase SQL Editor 또는 scripts/load_dual_sql.py
-- 【seoul_log_date】2026-09-10
-- 【작성자 판별】✔️ 블록 → CHARTBOY
-- 【종목 코드】전부 레포 기존 배치 SQL: 대한전선 001440, 미코 059090,
--   솔브레인 357780, 심텍 222800, 두산테스나 131970
-- 【비고】각 종목 목표가 1개씩 고유 5행. 미코 19,930 · 솔브레인 354,500 · 심텍 135,200 ·
--   두산테스나 87,400 은 9/5~9/7 배치와 목표가가 같아 price_alerts 는 NOT EXISTS 로 건너뛴다.
--   대한전선 31,500 만 신규.
--
-- 공개 확인: stocks-ser4.onrender.com/n7u91bzk75/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 고유 5행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '001440', '대한전선', 31500, 'ABOVE', '매수타점 체크 / 대한전선 31.500원 돌파시.'),
  ('KR', '059090', '미코', 19930, 'ABOVE', '매수타점 체크 / 미코 19.930원 돌파시.'),
  ('KR', '357780', '솔브레인', 354500, 'ABOVE', '매수타점 체크 / 솔브레인 354.500원 돌파시.'),
  ('KR', '222800', '심텍', 135200, 'ABOVE', '매수타점 체크 / 심텍 135.200원 돌파시.'),
  ('KR', '131970', '두산테스나', 87400, 'ABOVE', '매수타점 체크 / 두산테스나 87.400원 돌파시.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY 9/10 5행
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '001440', '대한전선', 31500, 'ABOVE', '매수타점 체크 / 대한전선 31.500원 돌파시.', DATE '2026-09-10'),
  ('CHARTBOY', 'KR', '059090', '미코', 19930, 'ABOVE', '매수타점 체크 / 미코 19.930원 돌파시.', DATE '2026-09-10'),
  ('CHARTBOY', 'KR', '357780', '솔브레인', 354500, 'ABOVE', '매수타점 체크 / 솔브레인 354.500원 돌파시.', DATE '2026-09-10'),
  ('CHARTBOY', 'KR', '222800', '심텍', 135200, 'ABOVE', '매수타점 체크 / 심텍 135.200원 돌파시.', DATE '2026-09-10'),
  ('CHARTBOY', 'KR', '131970', '두산테스나', 87400, 'ABOVE', '매수타점 체크 / 두산테스나 87.400원 돌파시.', DATE '2026-09-10')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
