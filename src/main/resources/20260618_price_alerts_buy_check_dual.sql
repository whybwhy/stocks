-- 20260618 CHARTBOY 매수타점 체크 · Asia/Seoul
-- 【seoul_log_date】2026-06-18
-- 【종목 코드】레포·KRX: 034020 두산에너빌리티, 052690 한전기술, 042660 한화오션, 347850 디앤디파마텍, 104830 원익머트리얼즈, 000660 SK하이닉스
--
-- 공개 확인: stocks-ser4.onrender.com/31sbo1e3wx/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 9행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '034020', '두산에너빌리티', 108630, 'ABOVE', '두산에너빌리티 / 년봉상 매수타점 / 작년 고점도 넘었고, 2008년 고점 하나 남았음 / 108.630원 돌파시'),
  ('KR', '052690', '한전기술', 142000, 'ABOVE', '한전기술 / 년봉상 매수타점 / 작년 고점도 넘었고, 2010년 고점 하나 남았음 / 142.000원 돌파시'),
  ('KR', '042660', '한화오션', 139700, 'ABOVE', '한화오션 / 139.700원 돌파시 1차'),
  ('KR', '042660', '한화오션', 141200, 'ABOVE', '한화오션 / 141.200원 돌파시 2차 / 작년 고점 돌파시도 매수타점임'),
  ('KR', '104830', '원익머트리얼즈', 51500, 'ABOVE', '원익머트리얼즈 / 51.500원 돌파시 매수타점 / (구름대 & 후행스팬 모두 올라오는 자리) / 49.750원 돌파시 매수했었던 종목.'),
  ('KR', '347850', '디앤디파마텍', 110000, 'ABOVE', '디앤디파마텍 / 110.000원 돌파시 1차'),
  ('KR', '347850', '디앤디파마텍', 113700, 'ABOVE', '디앤디파마텍 / 113.700원 돌파시 2차'),
  ('KR', '347850', '디앤디파마텍', 114700, 'ABOVE', '디앤디파마텍 / 114.700원 돌파시 3차'),
  ('KR', '347850', '디앤디파마텍', 116600, 'ABOVE', '디앤디파마텍 / 116.600원 돌파시 4차')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 10행 · 2026-06-18
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '000660', 'SK하이닉스', NULL, 'ABOVE', 'SK하이닉스 / 보유 할 마음이 있다면, 어제 매수가 미련없이 들어간 자리임.', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '034020', '두산에너빌리티', 108630, 'ABOVE', '두산에너빌리티 / 년봉상 매수타점 / 작년 고점도 넘었고, 2008년 고점 하나 남았음 / 108.630원 돌파시', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '052690', '한전기술', 142000, 'ABOVE', '한전기술 / 년봉상 매수타점 / 작년 고점도 넘었고, 2010년 고점 하나 남았음 / 142.000원 돌파시', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '042660', '한화오션', 139700, 'ABOVE', '한화오션 / 139.700원 돌파시 1차', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '042660', '한화오션', 141200, 'ABOVE', '한화오션 / 141.200원 돌파시 2차 / 작년 고점 돌파시도 매수타점임', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '104830', '원익머트리얼즈', 51500, 'ABOVE', '원익머트리얼즈 / 51.500원 돌파시 매수타점 / (구름대 & 후행스팬 모두 올라오는 자리) / 49.750원 돌파시 매수했었던 종목.', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '347850', '디앤디파마텍', 110000, 'ABOVE', '디앤디파마텍 / 110.000원 돌파시 1차', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '347850', '디앤디파마텍', 113700, 'ABOVE', '디앤디파마텍 / 113.700원 돌파시 2차', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '347850', '디앤디파마텍', 114700, 'ABOVE', '디앤디파마텍 / 114.700원 돌파시 3차', DATE '2026-06-18'),
  ('CHARTBOY', 'KR', '347850', '디앤디파마텍', 116600, 'ABOVE', '디앤디파마텍 / 116.600원 돌파시 4차', DATE '2026-06-18')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
