#!/bin/bash
# Verify release artifact + GitHub release existence
# Usage: scripts/release-verify.sh <version>

set -euo pipefail
VER="${1:-}"
[ -z "$VER" ] && { echo "Usage: $0 <version>"; exit 1; }
VER="${VER#v}"

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
APK="$ROOT/releases/MINER_${VER}.apk"
MANIFEST="$ROOT/releases/v${VER}-manifest.json"
TAG="v${VER}"

[ -f "$APK" ] || { echo "VERIFY FAIL: missing $APK"; exit 1; }
[ -f "$MANIFEST" ] || { echo "VERIFY FAIL: missing $MANIFEST"; exit 1; }

APK_SHA=$(sha256sum "$APK" | awk '{print $1}')
MAN_SHA=$(python3 - <<PY
import json
p='$MANIFEST'
print(json.load(open(p)).get('apkSha256',''))
PY
)

if [ "$APK_SHA" != "$MAN_SHA" ]; then
  echo "VERIFY FAIL: APK sha mismatch"
  echo "  apk: $APK_SHA"
  echo "  man: $MAN_SHA"
  exit 1
fi

if ! gh release view "$TAG" >/dev/null 2>&1; then
  echo "VERIFY FAIL: GitHub release not found for $TAG"
  exit 1
fi

echo "VERIFY PASS: $TAG"
