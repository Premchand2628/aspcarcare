#!/bin/bash
set -e

# Start services one at a time with health checks
SERVICES="otploginauth carwashrates bookingservice membership uigatewayservice"

for svc in $SERVICES; do
  echo "Starting $svc..."
  sudo systemctl start "$svc"
  sleep 15
  status=$(systemctl is-active "$svc")
  echo "  $svc: $status"
  if [ "$status" != "active" ]; then
    echo "  WARNING: $svc not active yet, checking logs..."
    sudo journalctl -u "$svc" --no-pager -n 5
  fi
  echo ""
done

echo "=== Final Status ==="
for svc in $SERVICES; do
  printf '%-22s %s\n' "$svc" "$(systemctl is-active $svc)"
done
echo ""
free -h
