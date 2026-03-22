-- 가격 알람 테이블 (한국투자증권 API · 국내+해외)
-- market: KR=국내(stock_code 6자리), NAS/NYS/AMS=미국(stock_code 티커심볼)
-- stock_code: KR→005930, US→AAPL
-- target_price: KR=원화, US=달러 (소수점 2자리)
-- condition: ABOVE(이상 도달), BELOW(이하 도달)

CREATE TABLE IF NOT EXISTS public.price_alerts (
    id bigserial PRIMARY KEY,
    market text NOT NULL DEFAULT 'KR' CHECK (market IN ('KR', 'NAS', 'NYS', 'AMS')),
    stock_code text NOT NULL,
    symbol text,
    target_price numeric(18, 2) NOT NULL,
    condition text NOT NULL DEFAULT 'ABOVE' CHECK (condition IN ('ABOVE', 'BELOW')),
    label text,
    is_active boolean NOT NULL DEFAULT true,
    triggered_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Seoul')
);

CREATE INDEX IF NOT EXISTS idx_price_alerts_active ON public.price_alerts (is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_price_alerts_stock_code ON public.price_alerts (stock_code);
CREATE INDEX IF NOT EXISTS idx_price_alerts_market ON public.price_alerts (market);

COMMENT ON TABLE public.price_alerts IS '국내+해외 주식 목표가 알람 (KR=국내6자리, NAS/NYS/AMS=미국티커)';

-- price_alerts에 source 컬럼 추가 (출처 구분)
-- CHARTBOY: 차트보이/팬딩 영상, MY: 본인 분석, MANUAL: 기타 수동
ALTER TABLE public.price_alerts
  ADD COLUMN IF NOT EXISTS source text NOT NULL DEFAULT 'CHARTBOY'
  CHECK (source IN ('CHARTBOY', 'MY', 'MANUAL'));

CREATE INDEX IF NOT EXISTS idx_price_alerts_source ON public.price_alerts (source);

