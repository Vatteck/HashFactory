#!/bin/bash
#
# Silicon Sage Release Script v2.0 (Stabilized)
# Usage: ./release.sh 3.9.21 "Brief changelog summary"
#

set -e  # Exit on error

# --- [0] Environment Setup ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# Ensure Android SDK is found
if [ -z "$ANDROID_HOME" ]; then
    if [ -d "/home/vatteck/Android/Sdk" ]; then
        export ANDROID_HOME="/home/vatteck/Android/Sdk"
    else
        echo -e "${RED}Error: ANDROID_HOME not set and default path not found.${NC}"
        exit 1
    fi
fi

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_GRADLE="$PROJECT_DIR/app/build.gradle.kts"
VERSION_JSON="$PROJECT_DIR/version.json"
README="$PROJECT_DIR/README.md"
APK_OUTPUT="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
DESKTOP="$HOME/Desktop"
RELEASES_DIR="$PROJECT_DIR/releases"

# Parse arguments
VERSION="$1"
SUMMARY="$2"

if [ -z "$VERSION" ]; then
    echo -e "${RED}Error: Version required${NC}"
    echo "Usage: ./release.sh <version> [summary]"
    exit 1
fi

VERSION="${VERSION#v}" # Strip 'v'

echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo -e "${CYAN}  Silicon Sage Release Engine: v${VERSION}${NC}"
echo -e "${CYAN}═══════════════════════════════════════════${NC}"

# --- [1] Sync Local Bench ---
echo -e "${YELLOW}[1/7]${NC} Syncing with remote (Antigravity/IDE check)..."
cd "$PROJECT_DIR"
git pull origin master --rebase || (echo -e "${RED}Sync failed. Resolve conflicts manually.${NC}" && exit 1)

# --- [2] Update Version State ---
echo -e "${YELLOW}[2/7]${NC} Incrementing build metadata..."
CURRENT_CODE=$(grep -oP 'versionCode = \K\d+' "$BUILD_GRADLE")
NEW_CODE=$((CURRENT_CODE + 1))
TODAY=$(date +%Y-%m-%d)

sed -i "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$BUILD_GRADLE"
sed -i "s/versionName = \".*\"/versionName = \"$VERSION\"/" "$BUILD_GRADLE"

cat > "$VERSION_JSON" << EOF
{
  "version": "$VERSION",
  "build": $NEW_CODE,
  "date": "$TODAY",
  "changelog": [
    "${SUMMARY:-See CHANGELOG.md for details}"
  ],
  "url": "https://github.com/Vatteck/SiliconSageAIMiner/releases/tag/v$VERSION",
  "downloadUrl": "https://github.com/Vatteck/SiliconSageAIMiner/releases/download/v$VERSION/MINER_${VERSION}.apk"
}
EOF

sed -i "s/\*\*Current Version\*\*: v.*/\*\*Current Version\*\*: v$VERSION/" "$README"
sed -i "s/\*\*Last Updated\*\*: .*/\*\*Last Updated\*\*: $TODAY/" "$README"
echo -e "${GREEN}       ✓ Version files updated[0m"

# --- [3] Compile ---
echo -e "${YELLOW}[3/7]${NC} Building debug APK (Gradle)..."
./gradlew assembleDebug --quiet --no-daemon

if [ ! -f "$APK_OUTPUT" ]; then
    echo -e "${RED}Error: APK not found at $APK_OUTPUT${NC}"
    exit 1
fi
echo -e "${GREEN}       ✓ Build successful${NC}"

# --- [4] Local Distribution ---
APK_NAME="MINER_${VERSION}.apk"
cp "$APK_OUTPUT" "$DESKTOP/$APK_NAME"
mkdir -p "$RELEASES_DIR"
cp "$APK_OUTPUT" "$RELEASES_DIR/$APK_NAME"
echo -e "${GREEN}       ✓ $APK_NAME staged to Desktop & releases/${NC}"

# --- [5] Git Anchor ---
echo -e "${YELLOW}[5/7]${NC} Committing changes and staging binary..."
git add .
# Force-add the APK just in case gitignore gets squirrelly again
git add -f "$RELEASES_DIR/$APK_NAME"
git commit -m "Release v$VERSION" -m "${SUMMARY:-Auto-release update}"
echo -e "${GREEN}       ✓ Changes committed${NC}"

# --- [6] Tagging ---
echo -e "${YELLOW}[6/7]${NC} Creating tag v$VERSION..."
if git rev-parse "v$VERSION" >/dev/null 2>&1; then
    git tag -d "v$VERSION"
    git push origin ":refs/tags/v$VERSION" 2>/dev/null || true
fi
git tag -a "v$VERSION" -m "Release v$VERSION"
echo -e "${GREEN}       ✓ Tag created${NC}"

# --- [7] Uplink ---
echo -e "${YELLOW}[7/7]${NC} Pushing to GitHub..."
git push origin master --tags --force
echo -e "${GREEN}       ✓ Pushed to GitHub${NC}"

# --- [8] Lean Persistence (Purge Old Releases) ---
echo -e "${YELLOW}[8/8]${NC} Maintaining lean repo (keeping top 2 releases)..."
# Get tags of all releases, sorted by date, except the top 2
OLD_RELEASES=$(gh release list --limit 100 --repo https://github.com/Vatteck/SiliconSageAIMiner.git | awk '{print $3}' | sed '1,2d')

if [ ! -z "$OLD_RELEASES" ]; then
    for tag in $OLD_RELEASES; do
        echo " Dereferencing old release: $tag"
        gh release delete "$tag" --repo https://github.com/Vatteck/SiliconSageAIMiner.git --yes >/dev/null 2>&1 || true
        git push origin :refs/tags/"$tag" >/dev/null 2>&1 || true
    done
    echo -e "${GREEN}       ✓ Old releases purged.${NC}"
else
    echo -e "${GREEN}       ✓ Repo already lean.${NC}"
fi

# --- Final Logging ---
BUILD_LOG="$PROJECT_DIR/docs/build_history.md"
mkdir -p "$PROJECT_DIR/docs"
echo "## $(date '+%Y-%m-%d %H:%M') — v$VERSION (Build $NEW_CODE)" >> "$BUILD_LOG"
echo "- Summary: ${SUMMARY:-Manual Release}" >> "$BUILD_LOG"
echo "" >> "$BUILD_LOG"

echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}  Release v$VERSION complete!${NC}"
echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo ""
echo -e "  ${CYAN}APK Path:${NC}  $DESKTOP/$APK_NAME"
echo ""
