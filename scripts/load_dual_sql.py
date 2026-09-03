#!/usr/bin/env python3
"""dual SQL 파일을 Supabase REST(PostgREST)로 적재한다.

Supabase SQL Editor 에 붙여넣는 대신 CLI 로 같은 결과를 만든다.
psql 없이 동작하며, 대상은 src/main/resources/YYYYMMDD_*_dual.sql 형식 파일.

사용법:
    python3 scripts/load_dual_sql.py src/main/resources/20260903_..._dual.sql        # dry
    python3 scripts/load_dual_sql.py src/main/resources/20260903_..._dual.sql apply  # 적재

동작:
  - price_alerts     : SQL 의 NOT EXISTS(symbol, target_price, condition) 를 그대로 재현해
                       이미 있는 목표가는 건너뛴다. 파일 안 중복도 1행으로 합친다.
  - price_alerts_log : uq_price_alerts_log_day_tp
                       (posted_by, stock_code, condition, seoul_log_date, target_price_bucket)
                       기준 upsert — SQL 의 ON CONFLICT DO UPDATE 와 같다.
  두 단계 모두 멱등이라 같은 파일을 여러 번 돌려도 행이 늘지 않는다.

접속 정보:
  기본값은 application-local.yml 의 supabase.url / anon-key 를 읽는다.
  SUPABASE_URL, SUPABASE_ANON_KEY 환경변수가 있으면 그쪽이 우선.
"""
import json
import os
import re
import sys
import urllib.error
import urllib.request
from pathlib import Path

REPO = Path(__file__).resolve().parent.parent
LOCAL_YML = REPO / "src/main/resources/application-local.yml"

# ('KR', '036540', 'SFA반도체', 6480, 'ABOVE', '라벨') — price_alerts VALUES 한 줄
RE_ALERT = re.compile(
    r"\(\s*'(?P<market>[^']*)'\s*,\s*'(?P<stock_code>[^']*)'\s*,\s*'(?P<symbol>[^']*)'\s*,"
    r"\s*(?P<target_price>[\d.]+)\s*,\s*'(?P<condition>[^']*)'\s*,\s*'(?P<label>.*?)'\s*\)\s*,?\s*$"
)
# ('HYONYHYONY', 'KR', ... , DATE '2026-09-01') — price_alerts_log VALUES 한 줄
RE_LOG = re.compile(
    r"\(\s*'(?P<posted_by>CHARTBOY|HYONYHYONY)'\s*,\s*'(?P<market>[^']*)'\s*,"
    r"\s*'(?P<stock_code>[^']*)'\s*,\s*'(?P<symbol>[^']*)'\s*,\s*(?P<target_price>[\d.]+)\s*,"
    r"\s*'(?P<condition>[^']*)'\s*,\s*'(?P<label>.*?)'\s*(?:,\s*DATE\s*'(?P<day>[\d-]+)')?\s*\)\s*,?\s*$"
)


def load_conn():
    url = os.environ.get("SUPABASE_URL")
    key = os.environ.get("SUPABASE_ANON_KEY")
    if url and key:
        return url.rstrip("/") + "/rest/v1", key
    if not LOCAL_YML.exists():
        sys.exit(f"접속 정보 없음: SUPABASE_URL/SUPABASE_ANON_KEY 또는 {LOCAL_YML}")
    text = LOCAL_YML.read_text(encoding="utf-8")
    u = re.search(r"^supabase:\s*$\s*url:\s*(\S+)", text, re.M)
    k = re.search(r"^\s*anon-key:\s*(\S+)", text, re.M)
    if not (u and k):
        sys.exit(f"{LOCAL_YML} 에서 supabase.url / anon-key 를 찾지 못함")
    return u.group(1).rstrip("/") + "/rest/v1", k.group(1)


BASE, KEY = load_conn()
HDR = {"apikey": KEY, "Authorization": f"Bearer {KEY}", "Content-Type": "application/json"}


def req(method, path, body=None, extra=None):
    hdr = dict(HDR)
    if extra:
        hdr.update(extra)
    data = json.dumps(body, ensure_ascii=False).encode() if body is not None else None
    r = urllib.request.Request(f"{BASE}/{path}", data=data, headers=hdr, method=method)
    try:
        with urllib.request.urlopen(r) as resp:
            raw = resp.read().decode()
            return resp.status, (json.loads(raw) if raw.strip() else [])
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()


def parse(path):
    """dual SQL 을 price_alerts 행 · price_alerts_log 행으로 나눈다."""
    alerts, logs, section = [], [], None
    for raw in Path(path).read_text(encoding="utf-8").splitlines():
        line = raw.strip()
        if line.startswith("--"):
            continue
        if "INSERT INTO public.price_alerts_log" in line:
            section = "log"
            continue
        if "INSERT INTO public.price_alerts" in line:
            section = "alert"
            continue
        if line.startswith(("ON CONFLICT", "WHERE NOT EXISTS", ") AS v(")):
            section = None
            continue
        if section == "alert":
            m = RE_ALERT.match(line)
            if m:
                d = m.groupdict()
                alerts.append({
                    "market": d["market"], "stock_code": d["stock_code"],
                    "symbol": d["symbol"], "target_price": float(d["target_price"]),
                    "condition": d["condition"], "label": d["label"].replace("''", "'"),
                    "source": "CHARTBOY",
                })
        elif section == "log":
            m = RE_LOG.match(line)
            if m:
                d = m.groupdict()
                row = {
                    "posted_by": d["posted_by"], "market": d["market"],
                    "stock_code": d["stock_code"], "symbol": d["symbol"],
                    "target_price": float(d["target_price"]), "condition": d["condition"],
                    "label": d["label"].replace("''", "'"),
                }
                if d["day"]:
                    row["seoul_log_date"] = d["day"]
                logs.append(row)
    return alerts, logs


def existing_alert_keys():
    keys, offset = set(), 0
    while True:
        st, rows = req("GET", "price_alerts?select=symbol,target_price,condition"
                              f"&limit=1000&offset={offset}")
        if st != 200 or not isinstance(rows, list):
            sys.exit(f"price_alerts 조회 실패 {st}: {rows}")
        for x in rows:
            keys.add((x["symbol"], float(x["target_price"]), x["condition"] or "ABOVE"))
        if len(rows) < 1000:
            return keys
        offset += 1000


def main():
    if len(sys.argv) < 2:
        sys.exit(__doc__)
    path = sys.argv[1]
    apply = len(sys.argv) > 2 and sys.argv[2] == "apply"

    alerts, logs = parse(path)
    if not alerts and not logs:
        sys.exit(f"파싱된 행이 없음 — 파일 형식 확인: {path}")
    print(f"{path}\n  파싱: price_alerts {len(alerts)}행 · price_alerts_log {len(logs)}행\n")

    have = existing_alert_keys()
    new_alerts, seen = [], set()
    for a in alerts:
        k = (a["symbol"], a["target_price"], a["condition"])
        if k in have or k in seen:
            continue
        seen.add(k)
        new_alerts.append(a)

    print(f"price_alerts     신규 {len(new_alerts)}행 "
          f"(기존·중복 {len(alerts) - len(new_alerts)}행 건너뜀)")
    for a in new_alerts:
        print(f"  + {a['symbol']:<12} {a['target_price']:>12,.0f}")

    days = {}
    for x in logs:
        days.setdefault((x["posted_by"], x.get("seoul_log_date", "(기본값=오늘)")), 0)
        days[(x["posted_by"], x.get("seoul_log_date", "(기본값=오늘)"))] += 1
    print(f"\nprice_alerts_log {len(logs)}행 (upsert)")
    for (pb, day), n in sorted(days.items()):
        print(f"  · {pb:<11} {day}  {n}행")

    if not apply:
        print("\n[dry] 전송 안 함. 인자에 apply 를 붙여 실행.")
        return

    if new_alerts:
        st, body = req("POST", "price_alerts", new_alerts, {"Prefer": "return=representation"})
        n = len(body) if isinstance(body, list) else body
        print(f"\nprice_alerts POST → {st} ({n})")
        if st >= 300:
            sys.exit("price_alerts 적재 실패 — log 는 보내지 않음")
    else:
        print("\nprice_alerts 신규 없음 — 건너뜀")

    if logs:
        st, body = req(
            "POST",
            "price_alerts_log?on_conflict=posted_by,stock_code,condition,"
            "seoul_log_date,target_price_bucket",
            logs,
            {"Prefer": "resolution=merge-duplicates,return=representation"},
        )
        n = len(body) if isinstance(body, list) else body
        print(f"price_alerts_log POST → {st} ({n})")
        if st >= 300:
            sys.exit("price_alerts_log 적재 실패")

    print("\n적재 완료.")


if __name__ == "__main__":
    main()
