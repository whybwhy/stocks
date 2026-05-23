-- =============================================================================
-- price_alerts_log — 목표가 메모 적재 전용 원장 테이블
-- - 알람용 price_alerts 와 분리 (여기만 중복 삽입 허용 · 멱등 제약 없음)
-- =============================================================================
--
-- posted_by (필수, 아래 두 값만):
--   CHARTBOY     — 줄이 ✔ 또는 ✔️ 로 시작하는「영상 전 요약」블록 (차트보이)
--   HYONYHYONY   — 본문이 🌈(무지개 이모지)로 시작하는 멤버쉽·효니효니 정리
--
-- (알람 테이블 price_alerts.source 의 'CHARTBOY' 문자열과 의미만 유사하지,
--    이 컬럼은 원장 작성자 표기 전용이라 값 집합이 다를 수 있음.)
--
-- Supabase REST: 테이블 스키마 반영 후 사용. RLS 사용 시 anon 등 INSERT 정책 필요.
--
-- 과거 DDL이 posted_by 에 차트보이·효니효니·HYONY 등을 썼다면
-- → price_alerts_log_migrate_posted_by.sql 먼저 실행.

CREATE TABLE IF NOT EXISTS public.price_alerts_log (
    id bigserial PRIMARY KEY,
    posted_by text NOT NULL CHECK (posted_by IN ('CHARTBOY', 'HYONYHYONY')),
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
  '목표가 텍스트 원장(price_alerts 는 알람 전용·멱등).';

COMMENT ON COLUMN public.price_alerts_log.posted_by IS
  'CHARTBOY=✔요약 차트보이 블록, HYONYHYONY=🌈로 시작하는 효니효니 멤버쉽 본문';
