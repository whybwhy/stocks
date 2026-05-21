-- 목표가 일별 적재·로그 테이블 (알람 price_alerts 와 분리 · 중복 허용)
-- 동일 종목·가격이라도 줄줄이 쌓을 수 있음.
-- 업로드 주체: 차트보이(CHARTBOY) / 효니효니(HYONY)
--
-- Supabase: 본 스크립트 실행 후 PostgREST에 테이블이 보이도록 스키마 캐시 반영(보통 자동).
-- RLS를 켠 경우 anon/service_role용 INSERT·SELECT 정책을 별도로 두어야 REST로 적재 가능.

CREATE TABLE IF NOT EXISTS public.price_alerts_log (
    id bigserial PRIMARY KEY,
    posted_by text NOT NULL CHECK (posted_by IN ('CHARTBOY', 'HYONY')),
    market text NOT NULL DEFAULT 'KR' CHECK (market IN ('KR', 'NAS', 'NYS', 'AMS')),
    stock_code text NOT NULL,
    symbol text,
    target_price numeric(18, 2) NOT NULL,
    condition text NOT NULL DEFAULT 'ABOVE' CHECK (condition IN ('ABOVE', 'BELOW')),
    label text,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_posted_created_desc
    ON public.price_alerts_log (posted_by, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_stock_code
    ON public.price_alerts_log (stock_code);

COMMENT ON TABLE public.price_alerts_log IS
  '목표가 메모 적재 로그(price_alerts 는 알람 전용·멱등).';

COMMENT ON COLUMN public.price_alerts_log.posted_by IS
  'CHARTBOY=차트보이, HYONY=효니효니';

-- 기존명에서 변경 시 테이블만 이름 바꾸려면 예:
-- ALTER TABLE public.price_alert_originals RENAME TO price_alerts_log;
-- ALTER TABLE public.price_alert_daily_imports RENAME TO price_alerts_log;
