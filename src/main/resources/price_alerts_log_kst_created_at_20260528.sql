-- =============================================================================
-- price_alerts_log.created_at · seoul_log_date — price_alerts 와 동일 KST 표시 규격
--
-- price_alerts.created_at DEFAULT: (now() AT TIME ZONE 'Asia/Seoul')
--   → Supabase 테이블 뷰에서도 한국 시각(벽시계)에 가깝게 보임.
-- price_alerts_log 는 기존 now()(UTC instant) 를 쓰고 있어 직접 조회 시 UTC 로 보였음.
--
-- 선행: price_alerts_log_seoul_daily_upsert_20260527.sql (seoul_log_date · UNIQUE)
-- Supabase SQL Editor 에서 한 번 실행.
-- =============================================================================

ALTER TABLE public.price_alerts_log
  ALTER COLUMN created_at SET DEFAULT (now() AT TIME ZONE 'Asia/Seoul');

ALTER TABLE public.price_alerts_log
  ALTER COLUMN seoul_log_date SET DEFAULT ((now() AT TIME ZONE 'Asia/Seoul')::date);

COMMENT ON COLUMN public.price_alerts_log.created_at IS
  '저장 시각(price_alerts 와 동일: now() AT TIME ZONE Asia/Seoul → timestamptz)';

CREATE OR REPLACE FUNCTION public.price_alerts_log_sync_seoul_log_date()
RETURNS trigger
LANGUAGE plpgsql AS $$
BEGIN
  IF NEW.created_at IS NULL THEN
    NEW.created_at := (now() AT TIME ZONE 'Asia/Seoul');
  END IF;
  IF NEW.seoul_log_date IS NULL THEN
    NEW.seoul_log_date := (NEW.created_at AT TIME ZONE 'UTC')::date;
  END IF;
  RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS trg_price_alerts_log_sync_seoul_log_date ON public.price_alerts_log;

CREATE TRIGGER trg_price_alerts_log_sync_seoul_log_date
BEFORE INSERT OR UPDATE OF created_at ON public.price_alerts_log
FOR EACH ROW EXECUTE PROCEDURE public.price_alerts_log_sync_seoul_log_date();

-- ─── (선택) 이미 넣은 행도 price_alerts 처럼 KST 벽시계로 보이게 재표기 ───
-- UPDATE public.price_alerts_log AS p
-- SET created_at = (timezone('Asia/Seoul', p.created_at) AT TIME ZONE 'UTC'),
--     seoul_log_date = (timezone('Asia/Seoul', p.created_at))::date
-- WHERE p.created_at IS NOT NULL;
