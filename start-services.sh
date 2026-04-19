#!/bin/bash
set -e

SERVICES="otploginauth bookingservice paymentservice supportchatservice membership carwashrates invitation mailnotification carwasherservice uigatewayservice"

# Enable all
sudo systemctl enable $SERVICES

# Start backend services first (everything except gateway)
BACKEND="otploginauth bookingservice paymentservice supportchatservice membership carwashrates invitation mailnotification carwasherservice"
sudo systemctl start $BACKEND
echo "Backend services started, waiting 30s for startup..."
sleep 30

# Start gateway last
sudo systemctl start uigatewayservice
echo "Gateway started, waiting 10s..."
sleep 10

# Check status
echo "=== Service Status ==="
for svc in $SERVICES; do
  status=$(systemctl is-active "$svc")
  echo "$svc: $status"
done

echo ""
echo "=== Memory Usage ==="
free -h
