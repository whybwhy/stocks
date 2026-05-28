-- =============================================================================
-- price_alerts_log — nullable 목표가 + 서울 달력일(seoul_log_date) + 일·키 단위 UNIQUE
--   → 같은 서울일·같은 (posted_by, stock_code, target_price 버킷, condition) 재적재 시
--     INSERT 가 아니라 UPDATE(ON CONFLICT) 로 라벨·종목명·가격 등 갱신.
-- =============================================================================
--
-- ⚠ 실행 전 백업 권장. 기존 중복 행은 아래 DELETE 로 id 작은 행만 남김 후 제약 추가.
-- Supabase Postgres 14+ 에서 테스트됨(trigger: EXECUTE FUNCTION).
-- created_at KST 표시(price_alerts 와 동일): price_alerts_log_kst_created_at_20260528.sql 추가 실행.
--
-- ─── 1) 목표가 NULL 허용(가격 없이 라벨·종목 등만 적재 가능) ───
ALTER TABLE public.price_alerts_log
  ALTER COLUMN target_price DROP NOT NULL;

-- ─── 2) 서울 기준 노출·중복키용 달력일 ───
ALTER TABLE public.price_alerts_log
  ADD COLUMN IF NOT EXISTS seoul_log_date date;

UPDATE public.price_alerts_log AS p
SET seoul_log_date = (timezone('Asia/Seoul', p.created_at))::date
WHERE p.seoul_log_date IS NULL;

ALTER TABLE public.price_alerts_log
  ALTER COLUMN seoul_log_date SET DEFAULT (timezone('Asia/Seoul', now()))::date;

ALTER TABLE public.price_alerts_log
  ALTER COLUMN seoul_log_date SET NOT NULL;

COMMENT ON COLUMN public.price_alerts_log.seoul_log_date IS
  '메모 노출 배치 서울 달력일(앱 /{slug}/new 및 REST 필터 권장)';

-- ─── 3) INSERT 시 created_at · seoul_log_date 자동 보정 ───
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

-- Postgres 13–15 호환: EXECUTE PROCEDURE(trigger 함수에도 동일 구문 사용)
CREATE TRIGGER trg_price_alerts_log_sync_seoul_log_date
BEFORE INSERT OR UPDATE OF created_at ON public.price_alerts_log
FOR EACH ROW EXECUTE PROCEDURE public.price_alerts_log_sync_seoul_log_date();

-- ─── 4) 동일 키 중복 행 정리(id 더 작은 행 유지) ───
--    COALESCE 는 5)·target_price_bucket 정의와 동일해야 한다.
DELETE FROM public.price_alerts_log AS l
    USING public.price_alerts_log AS k
WHERE l.id > k.id
  AND l.posted_by = k.posted_by
  AND l.stock_code = k.stock_code
  AND l.condition = k.condition
  AND l.seoul_log_date = k.seoul_log_date
  AND COALESCE(l.target_price,
        CAST('-9172836540918273.54' AS numeric(18, 2)))
      = COALESCE(k.target_price,
        CAST('-9172836540918273.54' AS numeric(18, 2)));

-- ─── 5) 버킷(NULL 가격 sentinel) + UNIQUE ───
ALTER TABLE public.price_alerts_log DROP COLUMN IF EXISTS target_price_bucket CASCADE;
ALTER TABLE public.price_alerts_log
  ADD COLUMN target_price_bucket numeric(18, 2)
    GENERATED ALWAYS AS (
      COALESCE(
        target_price,
        CAST('-9172836540918273.54' AS numeric(18, 2))
      )
    ) STORED;

ALTER TABLE public.price_alerts_log
  DROP CONSTRAINT IF EXISTS uq_price_alerts_log_day_tp;

ALTER TABLE public.price_alerts_log
  ADD CONSTRAINT uq_price_alerts_log_day_tp
    UNIQUE (posted_by, stock_code, condition, seoul_log_date, target_price_bucket);

CREATE INDEX IF NOT EXISTS idx_price_alerts_log_seoul_log_date_posted_desc
    ON public.price_alerts_log (posted_by, seoul_log_date DESC);

COMMENT ON COLUMN public.price_alerts_log.target_price_bucket IS
  'UPSERT 분기용(원시 target_price NULL → sentinel; 적재 시 직접 채우지 않음)';
COMMENT ON TABLE public.price_alerts_log IS
  '목표가 메모 원장(public.price_alerts 는 알람)·동일 서울일 동일 라인 키는 ON CONFLICT 갱신';
