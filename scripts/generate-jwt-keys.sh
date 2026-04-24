#!/usr/bin/env bash
# Generate an RSA 2048 key pair for JWT RS256 signing.
#
#   ./scripts/generate-jwt-keys.sh [output-dir]
#
# Produces two files in output-dir (default ./jwt-keys/):
#   jwt-private.pem  — keep ONLY on otploginauth (env var JWT_PRIVATE_KEY)
#   jwt-public.pem   — deploy to every service       (env var JWT_PUBLIC_KEY)
#
# After generation:
#   1. Set JWT_PUBLIC_KEY on ALL services (and restart) BEFORE flipping alg.
#   2. Set JWT_PRIVATE_KEY + JWT_ALGORITHM=RS256 on otploginauth, restart.
#   3. In-flight HS256 tokens still verify until they expire (20 min / 7 days).
#   4. Once confident, remove JWT_SECRET from non-issuing services.
#
# The private key MUST NOT be committed or checked into CI logs.

set -euo pipefail

OUT_DIR="${1:-./jwt-keys}"
mkdir -p "$OUT_DIR"

# PKCS#8 private key (format expected by JwtTokenService.rsaPrivateKey()).
openssl genpkey -algorithm RSA \
    -pkeyopt rsa_keygen_bits:2048 \
    -out "$OUT_DIR/jwt-private.pem"

# X.509 SubjectPublicKeyInfo (format expected by JwtTokenService.rsaPublicKey()).
openssl rsa -in "$OUT_DIR/jwt-private.pem" \
    -pubout \
    -out "$OUT_DIR/jwt-public.pem"

chmod 600 "$OUT_DIR/jwt-private.pem"
chmod 644 "$OUT_DIR/jwt-public.pem"

echo
echo "Generated:"
echo "  $OUT_DIR/jwt-private.pem  (otploginauth only — KEEP SECRET)"
echo "  $OUT_DIR/jwt-public.pem   (all services — safe to distribute)"
echo
echo "To load into an env var for systemd, escape newlines:"
echo "  JWT_PRIVATE_KEY=\"\$(cat $OUT_DIR/jwt-private.pem)\""
echo "  JWT_PUBLIC_KEY=\"\$(cat $OUT_DIR/jwt-public.pem)\""
