-- 가격 알람 테이블 (한국투자증권 API 형식)
-- stock_code: KIS 종목코드 6자리 (FID_INPUT_ISCD, 예: 005930)
-- symbol: 종목명 표시용 (예: 삼성전자)
-- target_price: 목표가 도달 시 알람 (정수만)
-- condition: ABOVE(이상 도달), BELOW(이하 도달)
-- 텔레그램 채팅 ID는 application.yml 에서 관리 (telegram.chat-ids)

CREATE TABLE IF NOT EXISTS public.price_alerts (
    id bigserial PRIMARY KEY,
    stock_code text NOT NULL,
    symbol text,
    target_price numeric(18, 0) NOT NULL,
    condition text NOT NULL DEFAULT 'ABOVE' CHECK (condition IN ('ABOVE', 'BELOW')),
    label text,
    is_active boolean NOT NULL DEFAULT true,
    triggered_at timestamptz,
    created_at timestamptz NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Seoul')
);

CREATE INDEX IF NOT EXISTS idx_price_alerts_active ON public.price_alerts (is_active) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_price_alerts_stock_code ON public.price_alerts (stock_code);

COMMENT ON TABLE public.price_alerts IS '한국투자증권 API 기준 국내주식 목표가 알람';
