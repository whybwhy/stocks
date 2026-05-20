-- price_alerts: id 가 702 이상인 행의 created_at 을 실행 시점의 현재 시각(now)으로 갱신
--
-- 실행 전 확인(선택):
-- SELECT id, symbol, created_at FROM public.price_alerts WHERE id >= 702 ORDER BY id;

UPDATE public.price_alerts
SET created_at = now()
WHERE id >= 702;
