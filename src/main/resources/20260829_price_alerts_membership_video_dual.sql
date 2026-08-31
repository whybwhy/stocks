-- 20260829 8월 29일(토)~30일(일) 영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-08-29  (범위 정리는 시작일 기준 — 20260815 전례)
-- 【작성자 판별】원문이 🌈 로 시작 → HYONYHYONY
-- 【종목 코드】DL이앤씨 375500, 농심 004370, 대주전자재료 078600, 코미팜 041960,
--   실리콘투 257720, 에이피알 278470, HK이노엔 195940, 펌텍코리아 251970  (레포 기존 배치 SQL)
--   LS마린솔루션 060370(舊 KT서브마린·edaily/밸류라인), 롯데렌탈 089860(FnGuide),
--   성일하이텍 365340(FnGuide/다음금융), 아세아제지 002310(FnGuide — 26.08.26 종가 8,480원으로 8,580원 정합),
--   한국화장품제조 003350(FnGuide)  — 레포에 기존 코드 없어 외부 조회
-- 【비고】실리콘투만 목표가 2개(56,100원/63,400원) → 2행. 나머지는 각 1행. 고유 14행.
--
-- 공개 확인: stocks-ser4.onrender.com/rrxu8cn99o/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '375500', 'DL이앤씨', 76500, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / DL이앤씨 (76,500원)'),
  ('KR', '004370', '농심', 460000, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 농심 (460,000원)'),
  ('KR', '060370', 'LS마린솔루션', 34800, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / LS마린솔루션 (34,800원)'),
  ('KR', '078600', '대주전자재료', 99100, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 대주전자재료 (99,100원)'),
  ('KR', '089860', '롯데렌탈', 48150, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 롯데렌탈 (48,150원)'),
  ('KR', '365340', '성일하이텍', 85500, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 성일하이텍 (85,500원)'),
  ('KR', '002310', '아세아제지', 8580, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 아세아제지 (8,580원)'),
  ('KR', '041960', '코미팜', 9940, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 코미팜 (9,940원)'),
  ('KR', '257720', '실리콘투', 56100, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 실리콘투 (56,100원/63,400원)'),
  ('KR', '257720', '실리콘투', 63400, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 실리콘투 (56,100원/63,400원)'),
  ('KR', '278470', '에이피알', 473000, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 에이피알 (473,000원)'),
  ('KR', '195940', 'HK이노엔', 58600, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / HK이노엔 (58,600원)'),
  ('KR', '251970', '펌텍코리아', 70200, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 펌텍코리아 (70,200원)'),
  ('KR', '003350', '한국화장품제조', 17880, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 한국화장품제조 (17,880원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '375500', 'DL이앤씨', 76500, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / DL이앤씨 (76,500원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '004370', '농심', 460000, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 농심 (460,000원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '060370', 'LS마린솔루션', 34800, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / LS마린솔루션 (34,800원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '078600', '대주전자재료', 99100, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 대주전자재료 (99,100원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '089860', '롯데렌탈', 48150, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 롯데렌탈 (48,150원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '365340', '성일하이텍', 85500, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 성일하이텍 (85,500원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '002310', '아세아제지', 8580, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 아세아제지 (8,580원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '041960', '코미팜', 9940, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 코미팜 (9,940원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '257720', '실리콘투', 56100, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 실리콘투 (56,100원/63,400원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '257720', '실리콘투', 63400, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 실리콘투 (56,100원/63,400원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '278470', '에이피알', 473000, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 에이피알 (473,000원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '195940', 'HK이노엔', 58600, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / HK이노엔 (58,600원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '251970', '펌텍코리아', 70200, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 펌텍코리아 (70,200원)', DATE '2026-08-29'),
  ('HYONYHYONY', 'KR', '003350', '한국화장품제조', 17880, 'ABOVE', '8월 29일(토)~30일(일) 영상정리 / 한국화장품제조 (17,880원)', DATE '2026-08-29')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
