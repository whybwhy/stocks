-- =============================================
-- 내가 직접 추출한 종목 (source = 'MY')
-- WHERE NOT EXISTS: symbol + target_price 중복 방지
-- 삼천리·SNT 목표가 변경 시: DB에 예전 금액 행이 있으면 is_active=false 또는 삭제 후 정리
-- =============================================

INSERT INTO public.price_alerts (market, stock_code, symbol, target_price, condition, label, source)
SELECT v.market, v.stock_code, v.symbol, v.target_price::numeric(18,2), v.condition, v.label, v.source
FROM (VALUES
  ('KR', '004690', '삼천리',         161900, 'ABOVE', 'MY / 161,900원',                                     'MY'),
  ('KR', '298020', '효성티앤씨',      421500, 'ABOVE', '컵위드핸들 / 421,500원',                              'MY'),
  ('KR', '166090', '하나머티리얼즈',   69300, 'ABOVE', '가운데자리 / 69,300원',                                'MY'),
  ('KR', '383800', 'LX홀딩스',        10420, 'ABOVE', '월봉빵빵빵 (연봉 B파) / 10,420원',                     'MY'),
  ('KR', '0008T0', 'SOL 화장품',      15160, 'ABOVE', '주봉 컵위드핸들 / 15,160원',                           'MY'),
  ('KR', '001800', '오리온홀딩스',     26350, 'ABOVE', '주봉 컵위드핸들 / 26,350원',                           'MY'),
  ('KR', '001940', 'KISCO홀딩스',     29450, 'ABOVE', '컵위드핸들 (연봉으로도 살짝보임) / 29,450원',            'MY'),
  ('KR', '178320', '서진시스템',       49000, 'ABOVE', '항시 매수 / 49,000원',                                 'MY'),
  ('KR', '053610', '프로텍',           62500, 'ABOVE', '주봉 컵위드핸들 (컵쪽이 높음) / 62,500원',              'MY'),
  ('KR', '033100', '제룡전기',         70000, 'ABOVE', '주봉 언덕 / 70,000원',                                'MY'),
  ('KR', '100840', 'SNT에너지',       54300, 'ABOVE', 'MY / 54,300원 (타점 다소 애매·보수적)',                  'MY')
) AS v(market, stock_code, symbol, target_price, condition, label, source)
WHERE NOT EXISTS (
  SELECT 1 FROM public.price_alerts pa
  WHERE pa.symbol = v.symbol AND pa.target_price = v.target_price::numeric(18,2)
);
