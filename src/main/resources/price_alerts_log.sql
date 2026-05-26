-- =============================================================================
-- 신규 DB 전용 초기 DDL + 트리거. 이미 예전(price_alerts_log 만 있던) 테이블이 있으면
-- 통째 실행하지 말고 price_alerts_log_seoul_daily_upsert_20260527.sql 만 적용한다.
-- =============================================================================
-- price_alerts_log — 목표가 메모 원장 전용 (알람 price_alerts 와 분리)
-- - target_price NULL 허용(가격 미기재 줄은 label·종목만 저장)
-- - seoul_log_date: 메모 배치 서울 달력일(앱 /{slug}/new 필터)
-- - 같은 (posted_by, stock_code, condition, seoul_log_date, 가격버킷) 는 UNIQUE
--    → 이중 적재 SQL 은 ON CONFLICT DO UPDATE 패턴 사용(마이그레이션 선행)
-- =============================================================================
--
-- posted_by (필수, CHECK 로 두 코드만 허용):
--   CHARTBOY     — 줄이 ✔ 또는 ✔️ 로 시작하는「영상 전 요약」(차트보이)
--   HYONYHYONY   — 줄이 🌈(무지개) 로 시작하는 멤버쉽·효니효니 블록
--
-- 과거 DDL·데이터가 한글 차트보이·효니효니 또는 HYONY 를 썼다면
--   반드시 먼저: price_alerts_log_migrate_posted_by.sql (제약 교체 · 값 정규화)
--
-- 이미 레거시 테이블이 있으면: price_alerts_log_seoul_daily_upsert_20260527.sql
--
-- Supabase: REST 게시판 스키마 반영 후 API 사용. RLS 사용 시 적절 정책 필요.
-- =============================================================================

CREATE TABLE IF NOT EXISTS public.price_alerts_log (
    id bigserial PRIMARY KEY,
    posted_by text NOT NULL,
    market text NOT NULL DEFAULT 'KR',
    stock_code text NOT NULL,
    symbol text,
    target_price numeric(18, 2),
    condition text NOT NULL DEFAULT 'ABOVE',
    label text,
    seoul_log_date date NOT NULL DEFAULT ((timezone('Asia/Seoul', now())))::date,
    target_price_bucket numeric(18, 2) GENERATED ALWAYS AS (
      COALESCE(
        target_price,
        CAST('-9172836540918273.54' AS numeric(18, 2))
      )
    ) STORED,
    created_at timestamptz NOT NULL DEFAULT now(),

    CONSTRAINT price_alerts_log_posted_by_check
      CHECK (posted_by IN ('CHARTBOY', 'HYONYHYONY')),
    CONSTRAINT price_alerts_log_market_check
      CHECK (market IN ('KR', 'NAS', 'NYS', 'AMS')),
    CONSTRAINT price_alerts_log_condition_check
      CHECK (condition IN ('ABOVE', 'BELOW')),
    CONSTRAINT uq_price_alerts_log_day_tp
      UNIQUE (posted_by, stock_code, condition, seoul_log_date, target_price_bucket)
);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_posted_created_desc
    ON public.price_alerts_log (posted_by, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_stock_code
    ON public.price_alerts_log (stock_code);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_created_at_desc
    ON public.price_alerts_log (created_at DESC);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_seoul_log_date_posted_desc
    ON public.price_alerts_log (posted_by, seoul_log_date DESC);

COMMENT ON TABLE public.price_alerts_log IS
  '목표가 메모 원장(price_alerts 는 알람용·동일 서울일 동일 라인 키는 ON CONFLICT 갱신 가능)';

COMMENT ON COLUMN public.price_alerts_log.posted_by IS
  'CHARTBOY=✔ 차트보이 요약, HYONYHYONY=🌈 멤버쉽 효니효니 블록';

COMMENT ON COLUMN public.price_alerts_log.market IS 'KR·미국 증거용 시장 코드';

COMMENT ON COLUMN public.price_alerts_log.stock_code IS '표준화된 문자열 종목 코드(예: KR 6자리)';

COMMENT ON COLUMN public.price_alerts_log.symbol IS '화면·검색에 쓰이는 종목명';

COMMENT ON COLUMN public.price_alerts_log.target_price IS '원화 기준 등 목표·참조가(NULL 허용)';

COMMENT ON COLUMN public.price_alerts_log.target_price_bucket IS
  'generated: NULL 가격은 sentinel 버킷(UPSERT 키)';

COMMENT ON COLUMN public.price_alerts_log.seoul_log_date IS
  '서울 달력 배치일(표시 페이지 필터)';

COMMENT ON COLUMN public.price_alerts_log.condition IS 'ABOVE 도달·BELOW 이탈(손절 등) 알람 규격과 동일';

COMMENT ON COLUMN public.price_alerts_log.label IS '원문 줄·메모 요약 블록(검색·표시용)';

COMMENT ON COLUMN public.price_alerts_log.created_at IS '저장 시각(서버 now(), 앱에서는 일반적으로 생략)';

CREATE OR REPLACE FUNCTION public.price_alerts_log_sync_seoul_log_date()
RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.created_at IS NULL THEN
    NEW.created_at := now();
  END IF;
  IF TG_OP = 'INSERT' AND NEW.seoul_log_date IS NULL THEN
    NEW.seoul_log_date := (timezone('Asia/Seoul', NEW.created_at))::date;
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_price_alerts_log_sync_seoul_log_date ON public.price_alerts_log;
CREATE TRIGGER trg_price_alerts_log_sync_seoul_log_date
BEFORE INSERT OR UPDATE OF created_at ON public.price_alerts_log
FOR EACH ROW EXECUTE PROCEDURE public.price_alerts_log_sync_seoul_log_date();
