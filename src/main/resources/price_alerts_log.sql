-- =============================================================================
-- price_alerts_log — 목표가 메모 원장 전용 (알람 price_alerts 와 분리)
-- - 같은 종목·같은 가격이라도 중복 행 허용(멱등 WHERE NOT 없음 INSERT 전용 테이블)
-- =============================================================================
--
-- posted_by (필수, CHECK 로 두 코드만 허용):
--   CHARTBOY     — 줄이 ✔ 또는 ✔️ 로 시작하는「영상 전 요약」(차트보이)
--   HYONYHYONY   — 줄이 🌈(무지개) 로 시작하는 멤버쉽·효니효니 블록
--
-- 과거 DDL·데이터가 한글 차트보이·효니효니 또는 HYONY 를 썼다면
--   반드시 먼저: price_alerts_log_migrate_posted_by.sql (제약 교체 · 값 정규화)
--
-- 기존 DB 에 이미 inline CHECK 로 테이블이 만들어져 있으면 이름이 자동이라
-- 마이그레이션에서 DROP 후 위 제약 이름으로 다시 거는 패턴 사용.
--
-- Supabase: REST 게시판 스키마 반영 후 API 사용. RLS 사용 시 적절 정책 필요.
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.price_alerts_log (
    id bigserial PRIMARY KEY,
    posted_by text NOT NULL,
    market text NOT NULL DEFAULT 'KR',
    stock_code text NOT NULL,
    symbol text,
    target_price numeric(18, 2) NOT NULL,
    condition text NOT NULL DEFAULT 'ABOVE',
    label text,
    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT price_alerts_log_posted_by_check
      CHECK (posted_by IN ('CHARTBOY', 'HYONYHYONY')),
    CONSTRAINT price_alerts_log_market_check
      CHECK (market IN ('KR', 'NAS', 'NYS', 'AMS')),
    CONSTRAINT price_alerts_log_condition_check
      CHECK (condition IN ('ABOVE', 'BELOW'))
);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_posted_created_desc
    ON public.price_alerts_log (posted_by, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_stock_code
    ON public.price_alerts_log (stock_code);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_created_at_desc
    ON public.price_alerts_log (created_at DESC);

COMMENT ON TABLE public.price_alerts_log IS
  '목표가 메모 적재 원장(price_alerts 는 알람용·멱등). 재실행 시 로그 줄만 누적됨';

COMMENT ON COLUMN public.price_alerts_log.posted_by IS
  'CHARTBOY=✔ 차트보이 요약, HYONYHYONY=🌈 멤버쉽 효니효니 블록';

COMMENT ON COLUMN public.price_alerts_log.market IS 'KR·미국 증거용 시장 코드';

COMMENT ON COLUMN public.price_alerts_log.stock_code IS '표준화된 문자열 종목 코드(예: KR 6자리)';

COMMENT ON COLUMN public.price_alerts_log.symbol IS '화면·검색에 쓰이는 종목명';

COMMENT ON COLUMN public.price_alerts_log.target_price IS '원화 기준 등 목표·참조가';

COMMENT ON COLUMN public.price_alerts_log.condition IS 'ABOVE 도달·BELOW 이탈(손절 등) 알람 규격과 동일';

COMMENT ON COLUMN public.price_alerts_log.label IS '원문 줄·메모 요약 블록(검색·표시용)';

COMMENT ON COLUMN public.price_alerts_log.created_at IS '저장 시각(서버 now(), 앱에서는 일반적으로 생략)';
