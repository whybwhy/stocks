-- 20260909 일일 통합 — CHARTBOY 9/9 매수타점 + HYONYHYONY 9/8 멤버쉽 영상정리 · Asia/Seoul
-- 【용도】Supabase SQL Editor 또는 scripts/load_dual_sql.py
-- 【seoul_log_date】CHARTBOY = 2026-09-09 · HYONYHYONY = 2026-09-08
-- 【작성자 판별】✔️ 블록 → CHARTBOY · 🌈 블록 → HYONYHYONY
-- 【종목 코드】레포 기존: 매드업 0039P0, 대우건설 047040, 성광벤드 014620, 티엘비 356860
--   외부 조회: 하림지주 003380, 이구산업 025820
-- 【비고】하림지주 11,750 은 9/9 차트보이·9/8 멤버쉽 양쪽에 등장 → price_alerts 는 1행,
--   log 는 작성자·날짜가 달라 각각 적재. 매드업·대우건설은 목표가 2개 → 각 2행. 고유 8행.
--   매드업 10,390/10,670 · 티엘비 42,700 은 기존 배치와 같아 NOT EXISTS 로 건너뛴다.
--
-- 공개 확인: stocks-ser4.onrender.com/obgn689dgi/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 고유 8행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '003380', '하림지주', 11750, 'ABOVE', '하림지주 11.750원 돌파시..매수종목 맞다'),
  ('KR', '0039P0', '매드업', 10390, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 매드업 (10,390원/10,670원)'),
  ('KR', '0039P0', '매드업', 10670, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 매드업 (10,390원/10,670원)'),
  ('KR', '047040', '대우건설', 21300, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 대우건설 (21,300원/21,950원)'),
  ('KR', '047040', '대우건설', 21950, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 대우건설 (21,300원/21,950원)'),
  ('KR', '014620', '성광벤드', 33450, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 성광벤드 (33,450원-ft.하이웨이브캔들)'),
  ('KR', '025820', '이구산업', 4695, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 이구산업 (4,695원-240일선 아래)'),
  ('KR', '356860', '티엘비', 42700, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 티엘비 (42,700원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY 9/9 1행 + HYONYHYONY 9/8 8행
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '003380', '하림지주', 11750, 'ABOVE', '하림지주 11.750원 돌파시..매수종목 맞다', DATE '2026-09-09'),
  ('HYONYHYONY', 'KR', '0039P0', '매드업', 10390, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 매드업 (10,390원/10,670원)', DATE '2026-09-08'),
  ('HYONYHYONY', 'KR', '0039P0', '매드업', 10670, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 매드업 (10,390원/10,670원)', DATE '2026-09-08'),
  ('HYONYHYONY', 'KR', '047040', '대우건설', 21300, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 대우건설 (21,300원/21,950원)', DATE '2026-09-08'),
  ('HYONYHYONY', 'KR', '047040', '대우건설', 21950, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 대우건설 (21,300원/21,950원)', DATE '2026-09-08'),
  ('HYONYHYONY', 'KR', '014620', '성광벤드', 33450, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 성광벤드 (33,450원-ft.하이웨이브캔들)', DATE '2026-09-08'),
  ('HYONYHYONY', 'KR', '025820', '이구산업', 4695, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 이구산업 (4,695원-240일선 아래)', DATE '2026-09-08'),
  ('HYONYHYONY', 'KR', '356860', '티엘비', 42700, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 티엘비 (42,700원)', DATE '2026-09-08'),
  ('HYONYHYONY', 'KR', '003380', '하림지주', 11750, 'ABOVE', '9월 8일(화) 멤버쉽 영상정리 / 하림지주 (11,750원)', DATE '2026-09-08')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
