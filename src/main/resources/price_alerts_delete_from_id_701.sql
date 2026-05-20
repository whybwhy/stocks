-- price_alerts: id 가 701 이상인 행 삭제
-- 참고: price_alert_triggers.alert_id 는 ON DELETE SET NULL 이라 돌파 로그 행은 남고 alert_id 만 NULL 처리됨
--
-- 실행 전 확인(선택):
-- SELECT id, symbol, target_price FROM public.price_alerts WHERE id >= 701 ORDER BY id;

DELETE FROM public.price_alerts
WHERE id >= 701;
