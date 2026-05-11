#!/bin/bash
set -e

echo "=== Creating ASPCare_qa database ==="
sudo -u postgres psql -d ASPCare_prod -c 'CREATE DATABASE "ASPCare_qa" OWNER postgres;'

echo "=== Dumping schema from ASPCare_prod ==="
sudo -u postgres pg_dump -d ASPCare_prod --schema-only > /tmp/schema.sql

echo "=== Applying schema to ASPCare_qa ==="
sudo -u postgres psql -d ASPCare_qa < /tmp/schema.sql

echo "=== Seeding ASPCare_qa with reference data ==="
sudo -u postgres psql -d ASPCare_qa < /tmp/seed_data.sql

echo "=== Verifying ASPCare_qa tables ==="
sudo -u postgres psql -d ASPCare_qa -c "SELECT schemaname, tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;"

echo "=== Adding QA DB env vars to env.properties ==="
cat >> /opt/carwash/config/env.properties << 'EOF'

# ── QA / STG database (routing) ──
QA_DB_URL=jdbc:postgresql://localhost:5432/ASPCare_qa
QA_DB_USERNAME=postgres
QA_DB_PASSWORD=Chandu@2628
EOF

echo "=== Updating Nginx: qa.aspcarcare.com ==="
sudo sed -i '/proxy_set_header Host \$host;/a\        proxy_set_header X-Env qa;' /etc/nginx/sites-available/qa.aspcarcare.com

echo "=== Updating Nginx: stg.aspcarcare.com ==="
sudo sed -i '/proxy_set_header Host \$host;/a\        proxy_set_header X-Env qa;' /etc/nginx/sites-available/stg.aspcarcare.com

echo "=== Updating Nginx: aspcarcare.com (prod) ==="
sudo sed -i '/proxy_set_header Host \$host;/a\        proxy_set_header X-Env prod;' /etc/nginx/sites-available/aspcarcare.com

echo "=== Testing Nginx config ==="
sudo nginx -t

echo "=== Reloading Nginx ==="
sudo systemctl reload nginx

echo "=== Done ==="
