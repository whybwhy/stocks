-- 20260813 8월 13일(목) 영상정리 · HYONYHYONY · Asia/Seoul
-- 【seoul_log_date】2026-08-13
-- 【종목 코드】삼성전기 009150(레포), SK스퀘어 402340(위키·KRX), SK이터닉스 475480(레포),
--   SFA반도체 036540(레포), NAVER 035420(레포), 제이에스링크 127120(레포),
--   GS건설 006360(레포), OCI홀딩스 010060(레포), DN오토모티브 007340(토스),
--   금호타이어 073240(검색), 이수화학 005950(레포)
--
-- 공개 확인: stocks-ser4.onrender.com/2a56vcce0y/new  (slug 는 application.yml 과 동일)

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18, 2), v.condition, v.label, 'CHARTBOY'
FROM (VALUES
  ('KR', '009150', '삼성전기', 1479000, 'ABOVE', '8월 13일(목) 영상정리 / 삼성전기 (1,479,000원)'),
  ('KR', '402340', 'SK스퀘어', 1157000, 'ABOVE', '8월 13일(목) 영상정리 / SK스퀘어 (1,157,000원)'),
  ('KR', '475480', 'SK이터닉스', 61800, 'ABOVE', '8월 13일(목) 영상정리 / SK이터닉스 (61,800원)'),
  ('KR', '036540', 'SFA반도체', 6480, 'ABOVE', '8월 13일(목) 영상정리 / SFA반도체 (6,480원)'),
  ('KR', '035420', 'NAVER', 234500, 'ABOVE', '8월 13일(목) 영상정리 / NAVER (234,500원)'),
  ('KR', '127120', '제이에스링크', 41250, 'ABOVE', '8월 13일(목) 영상정리 / 제이에스링크 (41,250원/43,100원/47,350원)'),
  ('KR', '127120', '제이에스링크', 43100, 'ABOVE', '8월 13일(목) 영상정리 / 제이에스링크 (41,250원/43,100원/47,350원)'),
  ('KR', '127120', '제이에스링크', 47350, 'ABOVE', '8월 13일(목) 영상정리 / 제이에스링크 (41,250원/43,100원/47,350원)'),
  ('KR', '006360', 'GS건설', 35750, 'ABOVE', '8월 13일(목) 영상정리 / GS건설 (35,750원)'),
  ('KR', '010060', 'OCI홀딩스', 293500, 'ABOVE', '8월 13일(목) 영상정리 / OCI홀딩스 (293,500원)'),
  ('KR', '007340', 'DN오토모티브', 50500, 'ABOVE', '8월 13일(목) 영상정리 / DN오토모티브 (50,500원)'),
  ('KR', '073240', '금호타이어', 8360, 'ABOVE', '8월 13일(목) 영상정리 / 금호타이어 (8,360원)'),
  ('KR', '005950', '이수화학', 15770, 'ABOVE', '8월 13일(목) 영상정리 / 이수화학 (15,770원)')
) AS v(market, stock_code, symbol, target_price, condition, label)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18, 2)
    AND COALESCE(pa.condition, 'ABOVE') = COALESCE(v.condition, 'ABOVE')
);

INSERT INTO public.price_alerts_log (posted_by, market, stock_code, symbol, target_price, condition, label, seoul_log_date)
VALUES
  ('HYONYHYONY', 'KR', '009150', '삼성전기', 1479000, 'ABOVE', '8월 13일(목) 영상정리 / 삼성전기 (1,479,000원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '402340', 'SK스퀘어', 1157000, 'ABOVE', '8월 13일(목) 영상정리 / SK스퀘어 (1,157,000원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '475480', 'SK이터닉스', 61800, 'ABOVE', '8월 13일(목) 영상정리 / SK이터닉스 (61,800원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '036540', 'SFA반도체', 6480, 'ABOVE', '8월 13일(목) 영상정리 / SFA반도체 (6,480원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '035420', 'NAVER', 234500, 'ABOVE', '8월 13일(목) 영상정리 / NAVER (234,500원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '127120', '제이에스링크', 41250, 'ABOVE', '8월 13일(목) 영상정리 / 제이에스링크 (41,250원/43,100원/47,350원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '127120', '제이에스링크', 43100, 'ABOVE', '8월 13일(목) 영상정리 / 제이에스링크 (41,250원/43,100원/47,350원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '127120', '제이에스링크', 47350, 'ABOVE', '8월 13일(목) 영상정리 / 제이에스링크 (41,250원/43,100원/47,350원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '006360', 'GS건설', 35750, 'ABOVE', '8월 13일(목) 영상정리 / GS건설 (35,750원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '010060', 'OCI홀딩스', 293500, 'ABOVE', '8월 13일(목) 영상정리 / OCI홀딩스 (293,500원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '007340', 'DN오토모티브', 50500, 'ABOVE', '8월 13일(목) 영상정리 / DN오토모티브 (50,500원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '073240', '금호타이어', 8360, 'ABOVE', '8월 13일(목) 영상정리 / 금호타이어 (8,360원)', DATE '2026-08-13'),
  ('HYONYHYONY', 'KR', '005950', '이수화학', 15770, 'ABOVE', '8월 13일(목) 영상정리 / 이수화학 (15,770원)', DATE '2026-08-13')
ON CONFLICT ON CONSTRAINT uq_price_alerts_log_day_tp
DO UPDATE SET
  market       = EXCLUDED.market,
  symbol       = EXCLUDED.symbol,
  target_price = EXCLUDED.target_price,
  label        = EXCLUDED.label;
