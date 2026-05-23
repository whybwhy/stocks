-- 5·21 배치 오기입 교정: 나노는 나노팀(417010)이 아니라 나노(187790) · 목표가 7,260원 행만
--
-- 교정 전 확인(선택):
-- SELECT id, stock_code, symbol, target_price, label FROM public.price_alerts WHERE stock_code = '417010' AND symbol = '나노팀' AND target_price = 7260;
-- SELECT id, stock_code, symbol, target_price, label FROM public.price_alerts_log WHERE stock_code = '417010' AND symbol = '나노팀' AND target_price = 7260;

BEGIN;

UPDATE public.price_alerts
SET stock_code = '187790',
    symbol      = '나노',
    label       = REPLACE(REPLACE(label, '나노(영상명)', '나노'), '나노팀', '나노')
WHERE stock_code = '417010'
  AND symbol = '나노팀'
  AND target_price = 7260
  AND condition = 'ABOVE';

UPDATE public.price_alerts_log
SET stock_code = '187790',
    symbol      = '나노',
    label       = REPLACE(REPLACE(label, '나노(영상명)', '나노'), '나노팀', '나노')
WHERE stock_code = '417010'
  AND symbol = '나노팀'
  AND target_price = 7260
  AND condition = 'ABOVE';

COMMIT;
