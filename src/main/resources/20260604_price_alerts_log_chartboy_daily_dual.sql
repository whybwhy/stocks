-- 20260604 CHARTBOY log 재적재 + 6/3 HYONY 멤버쉽 · Asia/Seoul
-- 【사용자 원문】2026-06-04 차트보이 일일 + 6월 3일 멤버쉽영상정리(효니효니)
-- 【용도】Supabase SQL Editor — 오늘 CHARTBOY log 삭제 후 INSERT (/{slug}/new)
-- 【seoul_log_date】DATE '2026-06-04' 고정
--
-- 【종목 코드】레포·기존 dual: NAVER 035420, 기아 000270, 현대차 005380, 현대모비스 012330,
--   현대오토에버 307950, LG에너지솔루션 373220, 노타 486990, 인텍플러스 064290, 네패스 033640,
--   씨젠 096530, 더블유게임즈 192080, 삼성전자 005930, SK하이닉스 000660
--   환율 USDKRW · 지수 INDEX (log 관례)
--
-- 【price_alerts】이 파일은 log 만 갱신 (알람 테이블 변경 없음)
--
-- 공개 확인: stocks-ser4.onrender.com/aurv08x4e2/new  (slug 는 application.yml 과 동일)

-- ── 1) 오늘 CHARTBOY log 전량 삭제 ──
DELETE FROM public.price_alerts_log
WHERE posted_by = 'CHARTBOY'
  AND seoul_log_date = DATE '2026-06-04';

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY · 20행
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', 'USDKRW', '원달러환율', 1536, 'ABOVE', '원달러 환율 / 1차목표가 1.536원', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', 'USDKRW', '원달러환율', 1598, 'ABOVE', '원달러 환율 / 2차목표가 1.598원 / 환율매매 하시는 분은, 최근영상 보셈. https://youtu.be/OBPFtNX7jQY', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', 'INDEX', '지수', NULL, 'ABOVE', '지수 / 코스피, 코스닥은 어제 지수 영상 보셈. 5일선 따라가는 코스피를, 높다고 쫄 필요는 없다. 5일선이 깨지면, 20일선을 보면 됨.', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '005930', '삼성전자', NULL, 'ABOVE', '삼성전자 / 총쏘는 모양 등장 (어제 삼성전자 영상 참조) / 오늘 총을 위로 쏠지, 아래로 쏠지 다같이 지켜보자', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '000660', 'SK하이닉스', NULL, 'ABOVE', 'SK하이닉스 / 여전히 5일선 따라가는 급등주.', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '035420', 'NAVER', 292000, 'ABOVE', 'NAVER / 292.000원 돌파시 1차', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '035420', 'NAVER', 295000, 'ABOVE', 'NAVER / 295.000원 돌파시 2차', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '000270', '기아', 177000, 'ABOVE', '기아 / 177.000원 돌파시 1차', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '000270', '기아', 184200, 'ABOVE', '기아 / 184.200원 돌파시 2차', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '005380', '현대차', NULL, 'ABOVE', '현대차 / 월봉이 양봉되면 매수할 수 있음 (이런모양의 대형주들 매매법은 똑같음)', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '012330', '현대모비스', NULL, 'ABOVE', '현대모비스 / 월봉이 양봉되면 매수할 수 있음 (이런모양의 대형주들 매매법은 똑같음)', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '307950', '현대오토에버', NULL, 'ABOVE', '현대오토에버 / 월봉이 양봉되면 매수할 수 있음 (이런모양의 대형주들 매매법은 똑같음)', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '373220', 'LG에너지솔루션', 444000, 'ABOVE', 'LG에너지솔루션 / 1차 444.000원 돌파시', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '373220', 'LG에너지솔루션', 455000, 'ABOVE', 'LG에너지솔루션 / 2차 455.000원 돌파시', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '486990', '노타', 43900, 'ABOVE', '노타 / 43.900원 돌파시(소액/단타)', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '064290', '인텍플러스', 40900, 'ABOVE', '인텍플러스 / 40.900원 돌파시(월봉 ABC언덕)', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '064290', '인텍플러스', 43600, 'ABOVE', '인텍플러스 / 43.600원 돌파시(일봉 삼봉)', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '033640', '네패스', 39350, 'ABOVE', '네패스 / 39.350원 돌파시(일봉 삼봉 후/주봉 뚜껑)', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '096530', '씨젠', 32500, 'ABOVE', '씨젠 / 32.500원 돌파시(일봉 삼봉)', DATE '2026-06-04'),
  ('CHARTBOY', 'KR', '192080', '더블유게임즈', NULL, 'ABOVE', '더블유게임즈 / 일봉 삼봉 모양맞음.', DATE '2026-06-04');

-- ── 3) 6월 3일 HYONYHYONY 멤버쉽영상정리 (seoul_log_date 2026-06-03) ──
DELETE FROM public.price_alerts_log
WHERE posted_by = 'HYONYHYONY'
  AND seoul_log_date = DATE '2026-06-03'
  AND stock_code IN (
    '005930', '011790', '005490', '006400', '051910', '278280',
    '010140', '204320', '000270', '011210'
  );

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '005930', '삼성전자', 370000, 'ABOVE', '6월 3일 멤버쉽영상정리 / 삼성전자 (370,000원-총쏘는모양)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '011790', 'SKC', 170122, 'ABOVE', '6월 3일 멤버쉽영상정리 / SKC (170,122원/187,980원-이동평균선매매법)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '011790', 'SKC', 187980, 'ABOVE', '6월 3일 멤버쉽영상정리 / SKC (170,122원/187,980원-이동평균선매매법)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '005490', 'POSCO홀딩스', 542000, 'ABOVE', '6월 3일 멤버쉽영상정리 / POSCO홀딩스 (542,000원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '006400', '삼성SDI', 723000, 'ABOVE', '6월 3일 멤버쉽영상정리 / 삼성SDI (723,000원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '051910', 'LG화학', 428500, 'ABOVE', '6월 3일 멤버쉽영상정리 / LG화학 (428,500원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '278280', '천보', 80600, 'ABOVE', '6월 3일 멤버쉽영상정리 / 천보 (80,600원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '010140', '삼성중공업', 35350, 'ABOVE', '6월 3일 멤버쉽영상정리 / 삼성중공업 (35,350원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '204320', 'HL만도', 65000, 'ABOVE', '6월 3일 멤버쉽영상정리 / HL만도 (65,000원/68,000원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '204320', 'HL만도', 68000, 'ABOVE', '6월 3일 멤버쉽영상정리 / HL만도 (65,000원/68,000원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '000270', '기아', 176300, 'ABOVE', '6월 3일 멤버쉽영상정리 / 기아 (176,300원/184,200원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '000270', '기아', 184200, 'ABOVE', '6월 3일 멤버쉽영상정리 / 기아 (176,300원/184,200원)', DATE '2026-06-03'),
  ('HYONYHYONY', 'KR', '011210', '현대위아', 115000, 'ABOVE', '6월 3일 멤버쉽영상정리 / 현대위아 (115,000원)', DATE '2026-06-03')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
