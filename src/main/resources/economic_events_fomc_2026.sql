-- 2026년 FOMC 정례회의 일정 (연 8회)
-- 출처: https://federalreserve.gov/monetarypolicy/fomccalendars.htm

INSERT INTO public.economic_events (event_date, event_name, description, source)
VALUES
  ('2026-01-28', 'FOMC', '1월 FOMC 정례회의', 'MANUAL'),
  ('2026-03-18', 'FOMC', '3월 FOMC 정례회의 (점도표 + 기자회견)', 'MANUAL'),
  ('2026-04-29', 'FOMC', '4월 FOMC 정례회의', 'MANUAL'),
  ('2026-06-17', 'FOMC', '6월 FOMC 정례회의 (점도표 + 기자회견)', 'MANUAL'),
  ('2026-07-29', 'FOMC', '7월 FOMC 정례회의', 'MANUAL'),
  ('2026-09-16', 'FOMC', '9월 FOMC 정례회의 (점도표 + 기자회견)', 'MANUAL'),
  ('2026-10-28', 'FOMC', '10월 FOMC 정례회의', 'MANUAL'),
  ('2026-12-09', 'FOMC', '12월 FOMC 정례회의 (점도표 + 기자회견)', 'MANUAL')
ON CONFLICT (event_date, event_name) DO NOTHING;
