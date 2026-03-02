#!/usr/bin/env bash
# Render 무료 tier 슬립 방지: 30초마다 https://stocks-ser4.onrender.com/ 호출

URL="${1:-https://stocks-ser4.onrender.com/}"
INTERVAL=30

echo "Started: $(date '+%Y-%m-%d %H:%M:%S')"
echo "URL: $URL (every ${INTERVAL}s)"
echo "Stop with Ctrl+C"
echo "---"

while true; do
  printf "%s  " "$(date '+%H:%M:%S')"
  if curl -s -o /dev/null -w "%{http_code}" "$URL" | grep -q 200; then
    echo "OK"
  else
    echo "FAIL or non-200"
  fi
  sleep "$INTERVAL"
done
