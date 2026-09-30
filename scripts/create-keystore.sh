#!/usr/bin/env bash
# Create a local release keystore for ARSAM and print GitHub secret values.
# Run once on a trusted machine. Never commit the .jks file.
set -euo pipefail

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
OUT_JKS="${ROOT}/arsam-release.jks"
ALIAS="${ARSAM_KEY_ALIAS:-arsam}"
VALIDITY_DAYS="${ARSAM_VALIDITY_DAYS:-10000}"

if [[ -f "$OUT_JKS" ]]; then
  echo "Keystore already exists: $OUT_JKS"
  echo "Delete it first if you want to regenerate."
  exit 1
fi

echo "=== ARSAM release keystore generator ==="
read -r -s -p "Keystore password: " STORE_PASS; echo
read -r -s -p "Key password (often same): " KEY_PASS; echo
read -r -p "Your name / org (CN): " CN
CN="${CN:-ARSAM}"

keytool -genkeypair \
  -keystore "$OUT_JKS" \
  -alias "$ALIAS" \
  -keyalg RSA \
  -keysize 2048 \
  -validity "$VALIDITY_DAYS" \
  -storepass "$STORE_PASS" \
  -keypass "$KEY_PASS" \
  -dname "CN=$CN, OU=ARSAM, O=ARSAM, L=Tehran, ST=Tehran, C=IR"

# Local properties file (gitignored pattern via keystore.properties)
cat > "$ROOT/keystore.properties" <<EOF
storeFile=arsam-release.jks
storePassword=$STORE_PASS
keyAlias=$ALIAS
keyPassword=$KEY_PASS
EOF

echo ""
echo "Created: $OUT_JKS"
echo "Created: $ROOT/keystore.properties  (do NOT commit)"
echo ""
echo "=== GitHub Secrets to add (Settings → Secrets and variables → Actions) ==="
echo "ARSAM_KEYSTORE_BASE64 = (run next command and paste output)"
echo "  base64 -w0 \"$OUT_JKS\"   # Linux"
echo "  base64 -i \"$OUT_JKS\" | tr -d '\\n'   # macOS"
echo "ARSAM_KEYSTORE_PASSWORD = (the keystore password you typed)"
echo "ARSAM_KEY_ALIAS = $ALIAS"
echo "ARSAM_KEY_PASSWORD = (the key password you typed)"
echo ""
echo "Then run workflow: Build ARSAM Release (Signed)"
