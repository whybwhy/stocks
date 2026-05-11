-- price_alerts·price_alert_triggers egress / 응답 시간 최적화 인덱스
-- Supabase SQL Editor 에서 한 번 실행. 모두 IF NOT EXISTS 로 멱등.
--
-- 1) 공개 목록 페이지(source=CHARTBOY + id DESC 정렬) → 복합 인덱스
-- 2) 공개 돌파 로그(source=CHARTBOY + triggered_at DESC 정렬) → 복합 인덱스
-- 3) 자동완성용 ilike '%...%' (symbol, stock_code) → pg_trgm GIN 인덱스
--    pg_trgm 가 없으면 ilike 양옆 와일드카드는 풀스캔이라 외부 폭격에 매우 취약.

-- pg_trgm 확장 (Supabase 기본 제공, 멱등)
CREATE EXTENSION IF NOT EXISTS pg_trgm;

-- ─── price_alerts ────────────────────────────────────────────────
-- 공개 목록 /{slug}/list 핫쿼리:
--   select ... from price_alerts where source = 'CHARTBOY' order by id desc limit 50
-- 단일 (source) + 단일 (id PK) 만 있으면 정렬 시 정렬 비용 발생. 복합으로 정렬까지 인덱스 처리.
CREATE INDEX IF NOT EXISTS idx_price_alerts_source_id_desc
    ON public.price_alerts (source, id DESC);

-- 자동완성 /{slug}/list/suggest:
--   symbol ilike '%foo%' or stock_code ilike '%foo%'
CREATE INDEX IF NOT EXISTS idx_price_alerts_symbol_trgm
    ON public.price_alerts USING gin (symbol gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_price_alerts_stock_code_trgm
    ON public.price_alerts USING gin (stock_code gin_trgm_ops);

-- ─── price_alert_triggers ────────────────────────────────────────
-- 공개 돌파 로그 /{slug}/log 핫쿼리:
--   select ... from price_alert_triggers where source = 'CHARTBOY' order by triggered_at desc limit 50
CREATE INDEX IF NOT EXISTS idx_price_alert_triggers_source_triggered_desc
    ON public.price_alert_triggers (source, triggered_at DESC);

-- 자동완성 /{slug}/log/suggest
CREATE INDEX IF NOT EXISTS idx_price_alert_triggers_symbol_trgm
    ON public.price_alert_triggers USING gin (symbol gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_price_alert_triggers_stock_code_trgm
    ON public.price_alert_triggers USING gin (stock_code gin_trgm_ops);

-- 통계 즉시 갱신 (옵션, 인덱스 사용 결정 보조)
ANALYZE public.price_alerts;
ANALYZE public.price_alert_triggers;
