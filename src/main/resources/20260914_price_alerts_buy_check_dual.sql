-- 20260914 CHARTBOY 주말 영상 매수타점 체크 · Asia/Seoul
-- 【용도】Supabase SQL Editor 또는 scripts/load_dual_sql.py
-- 【seoul_log_date】2026-09-14
-- 【작성자 판별】✔️ 블록 → CHARTBOY
-- 【종목 코드】레포 기존: 로보티즈 108490, 빙그레 005180, 샘씨엔에스 252990, 코칩 126730
--   외부 조회: 서부T&D 006730, 유수홀딩스 000700, 자이에스앤디 317400
-- 【가격 미기재】로보티즈는 "그냥 사고 5일선 깨지면 매도" 로 돌파 목표가가 없음
--   → price_alerts(target_price NOT NULL) 에는 넣지 않고 log 에만 NULL 로 적재.
--   20260605 케이씨에스 전례와 동일. target_price_bucket 이 COALESCE 센티넬을 쓰므로
--   uq_price_alerts_log_day_tp 도 정상 동작한다.
-- 【비고】샘씨엔에스 2개·유수홀딩스 3개(6,720/6,800 진입 · 6,890 메인) → 각 다행.
--   price_alerts 고유 9행 · log 10행.
--
-- 공개 확인: stocks-ser4.onrender.com/lyf40zeqm6/new  (slug 는 application.yml 과 동일)

-- ============================================================================
-- 1) public.price_alerts — 멱등 · 고유 9행 (로보티즈 제외 — 목표가 없음)
-- ============================================================================
INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '005180', '빙그레', 96000, 'ABOVE', '주말 영상 매수타점 체크 / 빙그레 96.000원 돌파시 / 주봉으로 보면, 뚜껑+도찌 패턴이다. / https://youtu.be/vEVXlJOp36w'),
  ('KR', '006730', '서부T&D', 11720, 'ABOVE', '주말 영상 매수타점 체크 / 서부T&D 11.720원 돌파시 / 박스권 형태모양이다. 소액.단타만 접근가능'),
  ('KR', '252990', '샘씨엔에스', 16320, 'ABOVE', '주말 영상 매수타점 체크 / 샘씨엔에스 16.320원/16.960원 통으로 넘는 매매법 / 티에스이처럼 될 수도 있다..ㅋ'),
  ('KR', '252990', '샘씨엔에스', 16960, 'ABOVE', '주말 영상 매수타점 체크 / 샘씨엔에스 16.320원/16.960원 통으로 넘는 매매법 / 티에스이처럼 될 수도 있다..ㅋ'),
  ('KR', '000700', '유수홀딩스', 6720, 'ABOVE', '주말 영상 매수타점 체크 / 유수홀딩스 이동평균선 매매법 (미코와 비슷한 차트모양) / 6.720원/6.800원부터 진입가능. 6.890원 돌파시 메인 매수타점'),
  ('KR', '000700', '유수홀딩스', 6800, 'ABOVE', '주말 영상 매수타점 체크 / 유수홀딩스 이동평균선 매매법 (미코와 비슷한 차트모양) / 6.720원/6.800원부터 진입가능. 6.890원 돌파시 메인 매수타점'),
  ('KR', '000700', '유수홀딩스', 6890, 'ABOVE', '주말 영상 매수타점 체크 / 유수홀딩스 이동평균선 매매법 (미코와 비슷한 차트모양) / 6.890원 돌파시 메인 매수타점'),
  ('KR', '317400', '자이에스앤디', 11920, 'ABOVE', '주말 영상 매수타점 체크 / 자이에스앤디 11.920원 돌파시 / 이동평균선 매매법'),
  ('KR', '126730', '코칩', 23600, 'ABOVE', '주말 영상 매수타점 체크 / 코칩 23.600원 돌파시 / 꼬다리 있는 작은 삼봉고점.')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

-- ============================================================================
-- 2) public.price_alerts_log — CHARTBOY 9/14 10행 (로보티즈 target_price NULL)
-- ============================================================================
INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('CHARTBOY', 'KR', '108490', '로보티즈', NULL, 'ABOVE', '주말 영상 매수타점 체크 / 로보티즈 그냥 사고, 5일선 깨지면 매도포지션. / 언제든 20일선 찍고 다시 갈 수 있다', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '005180', '빙그레', 96000, 'ABOVE', '주말 영상 매수타점 체크 / 빙그레 96.000원 돌파시 / 주봉으로 보면, 뚜껑+도찌 패턴이다. / https://youtu.be/vEVXlJOp36w', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '006730', '서부T&D', 11720, 'ABOVE', '주말 영상 매수타점 체크 / 서부T&D 11.720원 돌파시 / 박스권 형태모양이다. 소액.단타만 접근가능', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '252990', '샘씨엔에스', 16320, 'ABOVE', '주말 영상 매수타점 체크 / 샘씨엔에스 16.320원/16.960원 통으로 넘는 매매법 / 티에스이처럼 될 수도 있다..ㅋ', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '252990', '샘씨엔에스', 16960, 'ABOVE', '주말 영상 매수타점 체크 / 샘씨엔에스 16.320원/16.960원 통으로 넘는 매매법 / 티에스이처럼 될 수도 있다..ㅋ', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '000700', '유수홀딩스', 6720, 'ABOVE', '주말 영상 매수타점 체크 / 유수홀딩스 이동평균선 매매법 (미코와 비슷한 차트모양) / 6.720원/6.800원부터 진입가능. 6.890원 돌파시 메인 매수타점', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '000700', '유수홀딩스', 6800, 'ABOVE', '주말 영상 매수타점 체크 / 유수홀딩스 이동평균선 매매법 (미코와 비슷한 차트모양) / 6.720원/6.800원부터 진입가능. 6.890원 돌파시 메인 매수타점', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '000700', '유수홀딩스', 6890, 'ABOVE', '주말 영상 매수타점 체크 / 유수홀딩스 이동평균선 매매법 (미코와 비슷한 차트모양) / 6.890원 돌파시 메인 매수타점', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '317400', '자이에스앤디', 11920, 'ABOVE', '주말 영상 매수타점 체크 / 자이에스앤디 11.920원 돌파시 / 이동평균선 매매법', DATE '2026-09-14'),
  ('CHARTBOY', 'KR', '126730', '코칩', 23600, 'ABOVE', '주말 영상 매수타점 체크 / 코칩 23.600원 돌파시 / 꼬다리 있는 작은 삼봉고점.', DATE '2026-09-14')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
