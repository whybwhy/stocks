-- 20261015 10월 14일(수) 멤버쉽 영상정리 · HYONYHYONY · Asia/Seoul
-- 【용도】Supabase SQL Editor 또는 scripts/load_dual_sql.py
-- 【seoul_log_date】2026-10-14
-- 【작성자 판별】원문이 🌈 로 시작 → HYONYHYONY
--   (원문이 같은 블록을 두 번 붙여 왔으나 내용 동일 — 1회만 적재)
-- 【종목 코드】레포 기존: 삼성SDI 006400, POSCO홀딩스 005490, 한온시스템 018880,
--   나노신소재 121600, 롯데에너지머티리얼즈 020150, KODEX 2차전지산업 305720,
--   유니셈 036200, HPSP 403870, 테크윙 089030, 한화솔루션 009830
--   외부 조회: DI동일 001530, 도우인시스 484120(25.07.23 코스닥 상장 —
--   26.10.14 종가 23,800원으로 25,250 타점과 정합), 미래반도체 254490
-- 【비고】DI동일만 목표가 2개(28,100/29,600) → 2행. 나머지는 각 1행. 고유 14행.
--   KODEX 2차전지산업 16,340 은 10/6·10/10 의 15,950 과 다른 값이라 신규.
--   한온시스템 4,085 도 기존 3,945/4,025 와 달라 신규.
--
-- 공개 확인: stocks-ser4.onrender.com/b9lszkew7z/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 고유 14행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '006400', '삼성SDI', 600000, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 삼성SDI (600,000원)'),
  ('KR', '005490', 'POSCO홀딩스', 351000, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / POSCO홀딩스 (351,000원)'),
  ('KR', '018880', '한온시스템', 4085, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 한온시스템 (4,085원)'),
  ('KR', '121600', '나노신소재', 66200, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 나노신소재 (66,200원)'),
  ('KR', '020150', '롯데에너지머티리얼즈', 57800, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 롯데에너지머티리얼즈 (57,800원)'),
  ('KR', '001530', 'DI동일', 28100, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / DI동일 (28,100원/29,600원)'),
  ('KR', '001530', 'DI동일', 29600, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / DI동일 (28,100원/29,600원)'),
  ('KR', '305720', 'KODEX 2차전지산업', 16340, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / KODEX 2차전지산업 (16,340원)'),
  ('KR', '484120', '도우인시스', 25250, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 도우인시스 (25,250원)'),
  ('KR', '036200', '유니셈', 11080, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 유니셈 (11,080원)'),
  ('KR', '403870', 'HPSP', 70000, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / HPSP (70,000원)'),
  ('KR', '254490', '미래반도체', 19290, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 미래반도체 (19,290원)'),
  ('KR', '089030', '테크윙', 57900, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 테크윙 (57,900원)'),
  ('KR', '009830', '한화솔루션', 37500, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 한화솔루션 (37,500원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — HYONYHYONY 10/14 14행
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '006400', '삼성SDI', 600000, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 삼성SDI (600,000원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '005490', 'POSCO홀딩스', 351000, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / POSCO홀딩스 (351,000원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '018880', '한온시스템', 4085, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 한온시스템 (4,085원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '121600', '나노신소재', 66200, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 나노신소재 (66,200원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '020150', '롯데에너지머티리얼즈', 57800, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 롯데에너지머티리얼즈 (57,800원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '001530', 'DI동일', 28100, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / DI동일 (28,100원/29,600원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '001530', 'DI동일', 29600, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / DI동일 (28,100원/29,600원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '305720', 'KODEX 2차전지산업', 16340, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / KODEX 2차전지산업 (16,340원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '484120', '도우인시스', 25250, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 도우인시스 (25,250원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '036200', '유니셈', 11080, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 유니셈 (11,080원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '403870', 'HPSP', 70000, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / HPSP (70,000원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '254490', '미래반도체', 19290, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 미래반도체 (19,290원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '089030', '테크윙', 57900, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 테크윙 (57,900원)', DATE '2026-10-14'),
  ('HYONYHYONY', 'KR', '009830', '한화솔루션', 37500, 'ABOVE', '10월 14일(수) 멤버쉽 영상정리 / 한화솔루션 (37,500원)', DATE '2026-10-14')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
