-- 20260804 8월 4일(화) 멤버쉽영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-08-04
-- 【종목 코드】실리콘투 257720(레포), 아모레퍼시픽 090430(네이버·FnGuide),
--   코스맥스비티아이 044820(네이버), 티앤엘 340570(핀업·KRX), 펌텍코리아 251970(네이버),
--   코스메카코리아 241710(레포 · 가격 없음 → log NULL)
--
-- 공개 확인: stocks-ser4.onrender.com/2mh5zun2f3/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 7행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '257720', '실리콘투', 39000, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 실리콘투 (39,000원/40,450원)'),
  ('KR', '257720', '실리콘투', 40450, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 실리콘투 (39,000원/40,450원)'),
  ('KR', '090430', '아모레퍼시픽', 134000, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 아모레퍼시픽 (134,000원-각자 알아서)'),
  ('KR', '044820', '코스맥스비티아이', 21700, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 코스맥스비티아이 (21,700원)'),
  ('KR', '340570', '티앤엘', 64000, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 티앤엘 (64,000원/65,200원)'),
  ('KR', '340570', '티앤엘', 65200, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 티앤엘 (64,000원/65,200원)'),
  ('KR', '251970', '펌텍코리아', 50400, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 펌텍코리아 (50,400원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — HYONYHYONY · 8행 · 2026-08-04
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '257720', '실리콘투', 39000, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 실리콘투 (39,000원/40,450원)', DATE '2026-08-04'),
  ('HYONYHYONY', 'KR', '257720', '실리콘투', 40450, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 실리콘투 (39,000원/40,450원)', DATE '2026-08-04'),
  ('HYONYHYONY', 'KR', '090430', '아모레퍼시픽', 134000, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 아모레퍼시픽 (134,000원-각자 알아서)', DATE '2026-08-04'),
  ('HYONYHYONY', 'KR', '044820', '코스맥스비티아이', 21700, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 코스맥스비티아이 (21,700원)', DATE '2026-08-04'),
  ('HYONYHYONY', 'KR', '340570', '티앤엘', 64000, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 티앤엘 (64,000원/65,200원)', DATE '2026-08-04'),
  ('HYONYHYONY', 'KR', '340570', '티앤엘', 65200, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 티앤엘 (64,000원/65,200원)', DATE '2026-08-04'),
  ('HYONYHYONY', 'KR', '251970', '펌텍코리아', 50400, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 펌텍코리아 (50,400원)', DATE '2026-08-04'),
  ('HYONYHYONY', 'KR', '241710', '코스메카코리아', NULL, 'ABOVE', '8월 4일(화) 멤버쉽영상정리 / 코스메카코리아 (60분봉상으로 상승조정모양)', DATE '2026-08-04')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
