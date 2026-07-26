-- 20260724 7월 24일(금) CHARTBOY 일일 지수·종목체크 · Asia/Seoul
-- 【seoul_log_date】2026-07-24
-- 【종목 코드】GS건설 006360(레포·KRX)
-- 【지수】0001 코스피 (KIS 지수코드 · log NULL)
-- 【제외】손절하지마라 일반 원칙 · 이모지 구분선
--
-- 공개 확인: stocks-ser4.onrender.com/l5bfd3n9ww/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '006360', 'GS건설', 35750, 'ABOVE', 'GS건설 / 갭으로만 뜨지 않으면, / 오늘도 매수할 수 있음. / 35.750원 돌파시 매수타점. / (어제 영상 참조하셈)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '0001', '코스피', NULL, 'ABOVE', '코스피 / 이번주 주봉이, / 지난주 주봉 음봉을 반 이상 잡아먹어야 베스튼데. / 오늘 어떻게 될 지 지켜보자고~ / 안되면..뭐..좀 더 기다려야지.', DATE '2026-07-24'),
  ('CHARTBOY', 'KR', '006360', 'GS건설', 35750, 'ABOVE', 'GS건설 / 갭으로만 뜨지 않으면, / 오늘도 매수할 수 있음. / 35.750원 돌파시 매수타점. / (어제 영상 참조하셈)', DATE '2026-07-24')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
