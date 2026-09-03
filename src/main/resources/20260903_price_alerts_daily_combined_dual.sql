-- 20260903 일일 통합 — HYONYHYONY 9/1 멤버쉽 영상정리 + CHARTBOY 9/3 매수타점 · Asia/Seoul
-- 【용도】Supabase SQL Editor 또는 scripts/load_dual_sql.py
-- 【seoul_log_date】HYONYHYONY = 2026-09-01 · CHARTBOY = 2026-09-03
-- 【작성자 판별】🌈 블록 → HYONYHYONY · ▶️ 블록 → CHARTBOY
-- 【종목 코드】전부 레포 기존 배치 SQL: SFA반도체 036540, SK이노베이션 096770,
--   세아메카닉스 396300, 매드업 0039P0, 세방전지 004490, 에스엘 005850,
--   비츠로셀 082920, 우리금융지주 316140, 코칩 126730, 티엘비 356860
-- 【비고】매드업(9,600/10,670)은 9/1 멤버쉽·9/3 차트보이 양쪽에 등장 → price_alerts 는 1행씩,
--   log 는 작성자·날짜가 달라 각각 적재. 세아메카닉스·세방전지·매드업은 9/2 배치와 목표가가
--   같아 price_alerts 에서는 NOT EXISTS 로 건너뛴다.
--
-- 공개 확인: stocks-ser4.onrender.com/5lfbhc4mjd/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 고유 12행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '036540', 'SFA반도체', 6480, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / SFA반도체 (6,480원)'),
  ('KR', '096770', 'SK이노베이션', 135800, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / SK이노베이션 (135,800원)'),
  ('KR', '396300', '세아메카닉스', 5330, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 세아메카닉스 (5,330원)'),
  ('KR', '0039P0', '매드업', 9600, 'ABOVE', '매드업 / 9.600원 돌파시 1차 / 10.670원 돌파시 2차 / 안 넘으면 안 사면 그만임.'),
  ('KR', '0039P0', '매드업', 10670, 'ABOVE', '매드업 / 9.600원 돌파시 1차 / 10.670원 돌파시 2차 / 안 넘으면 안 사면 그만임.'),
  ('KR', '004490', '세방전지', 68600, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 세방전지 (68,600원)'),
  ('KR', '005850', '에스엘', 63400, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 에스엘 (63,400원-년봉 높다)'),
  ('KR', '082920', '비츠로셀', 36100, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 비츠로셀 (36,100원-월봉 뚜껑)'),
  ('KR', '316140', '우리금융지주', 34850, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 우리금융지주 (34,850원)'),
  ('KR', '126730', '코칩', 23250, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 코칩 (23,250원)'),
  ('KR', '356860', '티엘비', 42700, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 티엘비 (42,700원/44,603원)'),
  ('KR', '356860', '티엘비', 44603, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 티엘비 (42,700원/44,603원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — HYONYHYONY 9/1 12행 + CHARTBOY 9/3 2행
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '036540', 'SFA반도체', 6480, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / SFA반도체 (6,480원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '096770', 'SK이노베이션', 135800, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / SK이노베이션 (135,800원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '396300', '세아메카닉스', 5330, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 세아메카닉스 (5,330원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '0039P0', '매드업', 9600, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 매드업 (9,600원/10,670원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '0039P0', '매드업', 10670, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 매드업 (9,600원/10,670원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '004490', '세방전지', 68600, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 세방전지 (68,600원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '005850', '에스엘', 63400, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 에스엘 (63,400원-년봉 높다)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '082920', '비츠로셀', 36100, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 비츠로셀 (36,100원-월봉 뚜껑)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '316140', '우리금융지주', 34850, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 우리금융지주 (34,850원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '126730', '코칩', 23250, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 코칩 (23,250원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '356860', '티엘비', 42700, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 티엘비 (42,700원/44,603원)', DATE '2026-09-01'),
  ('HYONYHYONY', 'KR', '356860', '티엘비', 44603, 'ABOVE', '9월 1일(화) 멤버쉽 영상정리 / 티엘비 (42,700원/44,603원)', DATE '2026-09-01'),
  ('CHARTBOY', 'KR', '0039P0', '매드업', 9600, 'ABOVE', '매드업 / 9.600원 돌파시 1차 / 10.670원 돌파시 2차 / 안 넘으면 안 사면 그만임.', DATE '2026-09-03'),
  ('CHARTBOY', 'KR', '0039P0', '매드업', 10670, 'ABOVE', '매드업 / 9.600원 돌파시 1차 / 10.670원 돌파시 2차 / 안 넘으면 안 사면 그만임.', DATE '2026-09-03')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
