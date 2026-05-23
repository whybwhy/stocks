-- price_alerts_log.posted_by 값·CHECK 제약을 CHARTBOY / HYONYHYONY 로 통일
-- 과거 버전: CHARTBOY·HYONY, 또는 차트보이·효니효니 한글 등
--
-- Supabase 에서 한 번만 실행하면 됩니다.

BEGIN;

UPDATE public.price_alerts_log SET posted_by = 'CHARTBOY' WHERE posted_by IN ('차트보이', 'CHARTBOY');
UPDATE public.price_alerts_log SET posted_by = 'HYONYHYONY' WHERE posted_by IN ('효니효니', 'HYONY');

ALTER TABLE public.price_alerts_log DROP CONSTRAINT IF EXISTS price_alerts_log_posted_by_check;

ALTER TABLE public.price_alerts_log
  ADD CONSTRAINT price_alerts_log_posted_by_check
  CHECK (posted_by IN ('CHARTBOY', 'HYONYHYONY'));

COMMIT;
