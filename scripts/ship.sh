#!/bin/bash
# One-shot ship wrapper
# Usage: scripts/ship.sh <version> [summary] [--keep=N] [--allow-dirty] [--yes-purge] [--skip-smoke]

set -euo pipefail
VERSION="${1:-}"
SUMMARY="${2:-}"
shift 2 || true

SKIP_SMOKE=false
EXTRA_ARGS=()
for arg in "$@"; do
  [[ "$arg" == "--skip-smoke" ]] && SKIP_SMOKE=true && continue
  EXTRA_ARGS+=("$arg")
done

if [ -z "$VERSION" ]; then
  echo "Usage: $0 <version> [summary] [--keep=N] [--allow-dirty] [--yes-purge] [--skip-smoke]"
  exit 1
fi

ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT"

echo "[SHIP] Preflight build"
./gradlew assembleRelease --quiet --no-daemon

if [ "$SKIP_SMOKE" != true ]; then
  echo "[SHIP] Smoke gate"
  "$ROOT/scripts/release-smoke.sh" "$ROOT/app/build/outputs/apk/release/app-release.apk"
else
  echo "[SHIP] Smoke gate skipped"
fi

echo "[SHIP] Release"
"$ROOT/release.sh" "$VERSION" "$SUMMARY" "${EXTRA_ARGS[@]}"
