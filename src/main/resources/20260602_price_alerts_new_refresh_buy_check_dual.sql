-- 20260602 /new(CHARTBOY) 재갱신 — 매수타점·대형주·나스닥 · Asia/Seoul
-- 【용도】Supabase SQL Editor 실행 → price_alerts_log(/{slug}/new) 오늘 배치 재적재
-- 【사용자 원문】매수타점 체크 · 대형주 체크 · 나스닥 종목 체크(팔란티어·리비안·코어위브·아이온큐)
--
-- 【필드 길이 점검】(이 배치 최대값 · DDL 한도)
--   posted_by 8 · market 3 · stock_code 6(PLTR·CRWV·IONQ·NOTE=4) · symbol 8
--   target_price 934000 / numeric(18,2) · condition 5
--   label 58 (팔란티어 NULL 행) / text — 잘림 없음
--   seoul_log_date DATE '2026-06-02' 고정
--
-- 【나스닥】코어위브 월고점: 25년10월 ~141(외부) · 26년1월 187.3(월봉 고점, stockscan)
-- 【팔란티어】영상 240일선(숫자 없음) → alert 200일선 참고 162.13(외부 May/2026) · 기존 95(레포) 유지
-- 【아이온큐】반매도만 → price_alerts 미등록 · 4절 DELETE 로 미국 알람 제거
-- 【price_alerts】나스닥(PLTR·RIVN·CRWV·IONQ) 제외 — log(2절)만 /new 표시
-- 【NOTE】stock_code=NOTE — 각주(나스닥 연휴 안내), /new 맨 아래 표시
--
-- 【종목 코드】레포 PLTR(NYS)·RIVN(NAS) + KRX/FnGuide CRWV(코어위브)·IONQ(아이온큐)
--
-- 공개 확인: stocks-ser4.onrender.com/55lk6yvy1x/new  (slug 는 application.yml 과 동일)

-- ── 1) 오늘 CHARTBOY 이 배치 전량 삭제 후 재삽입 ──
DELETE FROM public.price_alerts_log
WHERE posted_by = 'CHARTBOY'
  AND seoul_log_date = DATE '2026-06-02'
  AND stock_code IN (
    '286940', '011790', '277810', '454910', '455900', '388720',
    '035420', '128940', '018260',
    'PLTR', 'RIVN', 'CRWV', 'IONQ', 'NOTE'
  );

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 24행(KR 17 + 미국 6 + NOTE 1)
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '286940', '롯데이노베이트', 24150, 'ABOVE', '롯데이노베이트 일봉 이평선 매매 / 24.150원/25.400원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '286940', '롯데이노베이트', 25400, 'ABOVE', '롯데이노베이트 일봉 이평선 매매 / 24.150원/25.400원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '011790', 'SKC', 170122, 'ABOVE', 'SKC 월봉 이평선 매매 / 170.122원/184.400원/187.980원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '011790', 'SKC', 184400, 'ABOVE', 'SKC 월봉 이평선 매매 / 170.122원/184.400원/187.980원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '011790', 'SKC', 187980, 'ABOVE', 'SKC 월봉 이평선 매매 / 170.122원/184.400원/187.980원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '277810', '레인보우로보틱스', 824000, 'ABOVE', '레인보우로보틱스 / 824.000원/915.000원/934.000원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '277810', '레인보우로보틱스', 915000, 'ABOVE', '레인보우로보틱스 / 824.000원/915.000원/934.000원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '277810', '레인보우로보틱스', 934000, 'ABOVE', '레인보우로보틱스 / 824.000원/915.000원/934.000원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '454910', '두산로보틱스', 138800, 'ABOVE', '두산로보틱스 월봉 뚜껑 / 138.800원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '455900', '엔젤로보틱스', 32300, 'ABOVE', '엔젤로보틱스 / 32.300원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '388720', '유일로보틱스', 110900, 'ABOVE', '유일로보틱스 손절매매도 가능함 / 110.900원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '035420', 'NAVER', 292000, 'ABOVE', 'NAVER / 292.000원/295.000원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '035420', 'NAVER', 295000, 'ABOVE', 'NAVER / 292.000원/295.000원 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '128940', '한미약품', 571000, 'ABOVE', '한미약품 / 571.000원 돌파시 정찰병 가능', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '128940', '한미약품', 563326, 'ABOVE', '한미약품 / 563.326원 돌파시(2018년 고점)', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '128940', '한미약품', 648778, 'ABOVE', '한미약품 / 648.778원 돌파시(2016년 고점)', DATE '2026-06-02'),
  ('CHARTBOY', 'KR', '018260', '삼성에스디에스', 429500, 'ABOVE', '삼성에스디에스 / 모든 매수타점을 넘었음. 목표가는 429.500원임.', DATE '2026-06-02'),
  ('CHARTBOY', 'NYS', 'PLTR', '팔란티어', NULL, 'ABOVE', '팔란티어 / 년봉 빵빵빵 훌륭하신 분. 240일선 넘으면 들어 갈 수 있다니깐? (오라클이랑 비슷하잖아?)', DATE '2026-06-02'),
  ('CHARTBOY', 'NAS', 'RIVN', '리비안', 18.855, 'ABOVE', '리비안 / 월봉 빵빵빵 고점인, 18.855돌파시 매수가능.', DATE '2026-06-02'),
  ('CHARTBOY', 'NAS', 'CRWV', '코어위브', 141, 'ABOVE', '코어위브 / 25년 10월 고점 돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'NAS', 'CRWV', '코어위브', 187.3, 'ABOVE', '코어위브 / 26년 1월 고점돌파시', DATE '2026-06-02'),
  ('CHARTBOY', 'NAS', 'IONQ', '아이온큐', NULL, 'BELOW', '아이온큐 / 5일선 깨지면 반매도.', DATE '2026-06-02');

-- ============================================================================
-- 3) public.price_alerts — 멱등 (KR만 17행 · 나스닥은 /new용 log 만 유지)
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '286940', '롯데이노베이트', 24150, 'ABOVE', '롯데이노베이트 일봉 이평선 매매 / 24.150원/25.400원 돌파시'),
  ('KR', '286940', '롯데이노베이트', 25400, 'ABOVE', '롯데이노베이트 일봉 이평선 매매 / 24.150원/25.400원 돌파시'),
  ('KR', '011790', 'SKC', 170122, 'ABOVE', 'SKC 월봉 이평선 매매 / 170.122원/184.400원/187.980원 돌파시'),
  ('KR', '011790', 'SKC', 184400, 'ABOVE', 'SKC 월봉 이평선 매매 / 170.122원/184.400원/187.980원 돌파시'),
  ('KR', '011790', 'SKC', 187980, 'ABOVE', 'SKC 월봉 이평선 매매 / 170.122원/184.400원/187.980원 돌파시'),
  ('KR', '277810', '레인보우로보틱스', 824000, 'ABOVE', '레인보우로보틱스 / 824.000원/915.000원/934.000원 돌파시'),
  ('KR', '277810', '레인보우로보틱스', 915000, 'ABOVE', '레인보우로보틱스 / 824.000원/915.000원/934.000원 돌파시'),
  ('KR', '277810', '레인보우로보틱스', 934000, 'ABOVE', '레인보우로보틱스 / 824.000원/915.000원/934.000원 돌파시'),
  ('KR', '454910', '두산로보틱스', 138800, 'ABOVE', '두산로보틱스 월봉 뚜껑 / 138.800원 돌파시'),
  ('KR', '455900', '엔젤로보틱스', 32300, 'ABOVE', '엔젤로보틱스 / 32.300원 돌파시'),
  ('KR', '388720', '유일로보틱스', 110900, 'ABOVE', '유일로보틱스 손절매매도 가능함 / 110.900원 돌파시'),
  ('KR', '035420', 'NAVER', 292000, 'ABOVE', 'NAVER / 292.000원/295.000원 돌파시'),
  ('KR', '035420', 'NAVER', 295000, 'ABOVE', 'NAVER / 292.000원/295.000원 돌파시'),
  ('KR', '128940', '한미약품', 571000, 'ABOVE', '한미약품 / 571.000원 돌파시 정찰병 가능'),
  ('KR', '128940', '한미약품', 563326, 'ABOVE', '한미약품 / 563.326원 돌파시(2018년 고점)'),
  ('KR', '128940', '한미약품', 648778, 'ABOVE', '한미약품 / 648.778원 돌파시(2016년 고점)'),
  ('KR', '018260', '삼성에스디에스', 429500, 'ABOVE', '삼성에스디에스 / 모든 매수타점을 넘었음. 목표가는 429.500원임.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 4) 오늘(2026-06-02) 나스닥·미국 알람 제거 — price_alerts_log(/{slug}/new) 는 유지
--    PLTR·RIVN·CRWV·IONQ (CHARTBOY · NAS/NYS/AMS)
-- ============================================================================
DELETE FROM public.price_alerts
WHERE source = 'CHARTBOY'
  AND market IN ('NAS', 'NYS', 'AMS')
  AND stock_code IN ('PLTR', 'RIVN', 'CRWV', 'IONQ');
