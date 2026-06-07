-- 20260608 CHARTBOY 일일 지수·코인·종목체크 · Asia/Seoul
-- 【사용자 원문】지수·코인·테크윙·KB·신한·주말영상·삼성SDI·서진·삼화콘덴서·건설·로봇
-- 【seoul_log_date】2026-06-08
--
-- 【종목 코드】레포: 089030 테크윙, 105560 KB금융, 055550 신한지주, 031980 피에스케이홀딩스,
--   006400 삼성SDI, 178320 서진시스템, 001820 삼화콘덴서, 001440 대한전선, 047040 대우건설, 454910 두산로보틱스
--   네이버·FnGuide: 160980 싸이맥스, 170920 엘티씨, 240810 원익IPS, 084370 유진테크
-- 【지수·코인】INDEX·지수, CRYPTO·코인 (log 관례)
--
-- 공개 확인: stocks-ser4.onrender.com/0w6ztj6ir2/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 8행
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '089030', '테크윙', 59900, 'ABOVE', '테크윙 / 59.900원 돌파시 1차 (240일 작은 쌍바닥)'),
  ('KR', '089030', '테크윙', 64200, 'ABOVE', '테크윙 / 64.200원 돌파시 2차 (240일 큰 쌍바닥)'),
  ('KR', '105560', 'KB금융', 175700, 'ABOVE', 'KB금융 / 175.700원 돌파시(총쏘는 모양) / 신고가에선 항상 10프로에 걸어놓고, 큰 돈은 들어갈 수 없다.'),
  ('KR', '055550', '신한지주', 109800, 'ABOVE', '신한지주 / 109.800원 돌파시(하이웨이브캔들) / 신고가에선 항상 10프로에 걸어놓고, 큰 돈은 들어갈 수 없다.'),
  ('KR', '170920', '엘티씨', 52100, 'ABOVE', '엘티씨 / 52.100원 돌파시.'),
  ('KR', '240810', '원익IPS', 151500, 'ABOVE', '원익IPS / 151.500원 돌파시.'),
  ('KR', '084370', '유진테크', 158000, 'ABOVE', '유진테크 / 158.000원 돌파시.'),
  ('KR', '031980', '피에스케이홀딩스', 139400, 'ABOVE', '피에스케이홀딩스 / 139.400원 돌파시.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 17행 · 2026-06-08
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', 'INDEX', '지수', NULL, 'ABOVE', '지수 / 코스피가 주봉 뚜껑을 땄지만, 주봉 5선위에 있는 여전히 급등패턴. 20일선을 깨는지가 관건. (어제 지수 영상 참조) / 아침에 갭 하락하더라도 놀라지들 말고, 갭 하락하면..갭을 메꾸러 반등할것임.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', 'CRYPTO', '코인', NULL, 'ABOVE', '코인 / 조금 올라온다고 반등 시그널이 아님. 최소한, 일봉상 5일선과 20일선이 크로스는 되야함.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '089030', '테크윙', 59900, 'ABOVE', '테크윙 / 59.900원 돌파시 1차 (240일 작은 쌍바닥)', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '089030', '테크윙', 64200, 'ABOVE', '테크윙 / 64.200원 돌파시 2차 (240일 큰 쌍바닥)', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '105560', 'KB금융', 175700, 'ABOVE', 'KB금융 / 175.700원 돌파시(총쏘는 모양) / 신고가에선 항상 10프로에 걸어놓고, 큰 돈은 들어갈 수 없다.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '055550', '신한지주', 109800, 'ABOVE', '신한지주 / 109.800원 돌파시(하이웨이브캔들) / 신고가에선 항상 10프로에 걸어놓고, 큰 돈은 들어갈 수 없다.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '160980', '싸이맥스', NULL, 'ABOVE', '싸이맥스 / 지금 상승은 B파일수 있다.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '170920', '엘티씨', 52100, 'ABOVE', '엘티씨 / 52.100원 돌파시.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '240810', '원익IPS', 151500, 'ABOVE', '원익IPS / 151.500원 돌파시.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '084370', '유진테크', 158000, 'ABOVE', '유진테크 / 158.000원 돌파시.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '031980', '피에스케이홀딩스', 139400, 'ABOVE', '피에스케이홀딩스 / 139.400원 돌파시.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '006400', '삼성SDI', NULL, 'ABOVE', '삼성SDI / 주봉 20선까지는 받아들이고, 오늘 당장 구름대 상단에서 반등가능.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '178320', '서진시스템', NULL, 'ABOVE', '서진시스템 / 밑꼬다리 없는 양봉 출현. 강력한 상승시그널 캔들임. / 밑꼬다리 없는 양봉이 뭔지 모르면? https://youtu.be/s5bKpfEarWk', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '001820', '삼화콘덴서', NULL, 'ABOVE', '삼화콘덴서 / 갭을 매꾸고, 작은 쌍바닥 만들었음. 2018년 고점돌파시가 마지막 매수타점이있음. 오늘 단장 반등한다하더라도, 갈만큼 간 종목이다.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '001440', '대한전선', NULL, 'ABOVE', '대한전선 / 구름대를 지켜줘야..반등을 기대해 볼수 있음.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '047040', '대우건설', NULL, 'ABOVE', '대우건설 / 구름대를 지켜줘야..반등을 기대해 볼수 있음.', DATE '2026-06-08'),
  ('CHARTBOY', 'KR', '454910', '두산로보틱스', NULL, 'ABOVE', '두산로보틱스 / 갭을 메꾸러 반등한다하더라도, 매수타점 잡기가 애매함.', DATE '2026-06-08')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
