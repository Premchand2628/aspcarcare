#!/bin/bash
set -e

SERVICES="otploginauth:10081 bookingservice:10082 paymentservice:10083 supportchatservice:10084 membership:10085 carwashrates:10086 invitation:10088 mailnotification:10089 carwasherservice:10090 uigatewayservice:8080"

for entry in $SERVICES; do
  name="${entry%%:*}"
  port="${entry##*:}"
  
  cat > /tmp/${name}.service << EOF
[Unit]
Description=ASPCare ${name}
After=network.target postgresql.service

[Service]
User=ubuntu
ExecStart=/usr/bin/java -jar /opt/carwash/jars/${name}.jar --spring.profiles.active=prod --spring.config.additional-location=file:/opt/carwash/config/env.properties
SuccessExitStatus=143
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal
SyslogIdentifier=${name}
Environment=JAVA_OPTS=-Xmx256m

[Install]
WantedBy=multi-user.target
EOF

  sudo mv /tmp/${name}.service /etc/systemd/system/${name}.service
  echo "Created ${name}.service"
done

sudo systemctl daemon-reload
echo "=== All services created and daemon reloaded ==="
