#!/bin/bash
# Roll back to previous release tag or explicit target.
# Usage: scripts/release-rollback.sh [tag] [--yes]

set -euo pipefail
TARGET_TAG="${1:-}"
YES=false
for arg in "$@"; do
  [[ "$arg" == "--yes" ]] && YES=true
done

cd "$(dirname "$0")/.."

if [ -z "$TARGET_TAG" ]; then
  TARGET_TAG=$(git tag --sort=-creatordate | sed -n '2p')
fi

if [ -z "$TARGET_TAG" ]; then
  echo "Rollback FAIL: no prior tag found"
  exit 1
fi

if ! git rev-parse "$TARGET_TAG" >/dev/null 2>&1; then
  echo "Rollback FAIL: tag not found: $TARGET_TAG"
  exit 1
fi

CURRENT=$(git rev-parse --short HEAD)
TARGET_SHA=$(git rev-parse --short "$TARGET_TAG")

echo "Rollback plan:"
echo "  current: $CURRENT"
echo "  target : $TARGET_TAG ($TARGET_SHA)"

if [ "$YES" != true ]; then
  read -r -p "Proceed with hard reset + force-with-lease push to master? [y/N] " reply
  [[ "$reply" =~ ^[Yy]$ ]] || { echo "Aborted"; exit 1; }
fi

git checkout master
git reset --hard "$TARGET_TAG"
git push --force-with-lease origin master

echo "Rollback complete: master -> $TARGET_TAG ($TARGET_SHA)"
