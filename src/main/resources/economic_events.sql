-- 경제 이벤트 캘린더 테이블
-- FOMC(수동), CPI/PPI/고용지표(FRED API 자동) 발표일 관리
-- 텔레그램 D-1/당일 알림 발송 추적

CREATE TABLE IF NOT EXISTS public.economic_events (
    id bigserial PRIMARY KEY,
    event_date date NOT NULL,
    event_name text NOT NULL,
    description text,
    source text NOT NULL DEFAULT 'MANUAL' CHECK (source IN ('MANUAL', 'FRED')),
    fred_release_id integer,
    notified_d1 boolean NOT NULL DEFAULT false,
    notified_d0 boolean NOT NULL DEFAULT false,
    created_at timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_economic_events_date ON public.economic_events (event_date);
CREATE UNIQUE INDEX IF NOT EXISTS idx_economic_events_unique
    ON public.economic_events (event_date, event_name);

COMMENT ON TABLE public.economic_events IS 'FOMC/CPI/PPI/고용지표 발표일 캘린더 (D-1/당일 텔레그램 알림)';
