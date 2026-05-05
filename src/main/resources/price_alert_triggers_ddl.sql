-- 목표가 돌파 시점 스냅샷 로그 (AlertService 가 INSERT)
-- Supabase PostgREST 로 조회 시 테이블·컬럼 노출 필요

CREATE TABLE IF NOT EXISTS public.price_alert_triggers (
    id bigserial PRIMARY KEY,
    alert_id bigint REFERENCES public.price_alerts (id) ON DELETE SET NULL,
    market text NOT NULL DEFAULT 'KR',
    stock_code text NOT NULL,
    symbol text,
    target_price numeric(18, 2) NOT NULL,
    condition text NOT NULL DEFAULT 'ABOVE',
    trigger_price numeric(18, 2) NOT NULL,
    label text,
    source text NOT NULL DEFAULT 'MY',
    triggered_at timestamptz NOT NULL DEFAULT (now() AT TIME ZONE 'Asia/Seoul')
);

CREATE INDEX IF NOT EXISTS idx_price_alert_triggers_triggered
    ON public.price_alert_triggers (triggered_at DESC);

CREATE INDEX IF NOT EXISTS idx_price_alert_triggers_source
    ON public.price_alert_triggers (source);

CREATE INDEX IF NOT EXISTS idx_price_alert_triggers_stock_code
    ON public.price_alert_triggers (stock_code);

COMMENT ON TABLE public.price_alert_triggers IS 'price_alerts 목표가 돌파 시 스냅샷 (텔레그램 발송 시점)';
