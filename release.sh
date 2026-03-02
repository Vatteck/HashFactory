#!/bin/bash
#
# Silicon Sage Release Script v3.0
# Usage: ./release.sh <version> [summary] [--dry-run] [--force-branch] [--keep=N] [--allow-dirty] [--yes-purge]
#

set -euo pipefail

# --- Colors ---
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
NC='\033[0m'

# --- Flags ---
DRY_RUN=false
FORCE_BRANCH=false
ALLOW_DIRTY=false
YES_PURGE=false
CI_MODE=false
KEEP_RELEASES=2
for arg in "$@"; do
    [[ "$arg" == "--dry-run" ]] && DRY_RUN=true
    [[ "$arg" == "--force-branch" ]] && FORCE_BRANCH=true
    [[ "$arg" == "--allow-dirty" ]] && ALLOW_DIRTY=true
    [[ "$arg" == "--yes-purge" ]] && YES_PURGE=true
    [[ "$arg" == "--ci" ]] && CI_MODE=true
    if [[ "$arg" =~ ^--keep=([0-9]+)$ ]]; then
        KEEP_RELEASES="${BASH_REMATCH[1]}"
    fi
done

VERSION="${1:-}"
SUMMARY="${2:-}"

if [ -z "$VERSION" ]; then
    echo -e "${RED}Error: Version required${NC}"
    echo "Usage: ./release.sh <version> [summary] [--dry-run] [--force-branch] [--keep=N] [--allow-dirty] [--yes-purge] [--ci]"
    exit 1
fi

VERSION="${VERSION#v}"

if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}[DRY RUN] No commits, tags, or pushes will be made.${NC}"
fi

if [ "$CI_MODE" = true ]; then
    YES_PURGE=true
    if [ "$ALLOW_DIRTY" = true ]; then
        echo -e "${RED}Error: --ci cannot be used with --allow-dirty.${NC}"
        exit 1
    fi
fi

# --- Environment ---
if [ -z "${ANDROID_HOME:-}" ]; then
    if [ -d "/home/vatteck/Android/Sdk" ]; then
        export ANDROID_HOME="/home/vatteck/Android/Sdk"
    else
        echo -e "${RED}Error: ANDROID_HOME not set.${NC}"; exit 1
    fi
fi

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
BUILD_GRADLE="$PROJECT_DIR/app/build.gradle.kts"
VERSION_JSON="$PROJECT_DIR/version.json"
README="$PROJECT_DIR/README.md"
APK_OUTPUT="$PROJECT_DIR/app/build/outputs/apk/release/app-release.apk"
RELEASES_DIR="$PROJECT_DIR/releases"
BUILD_LOG="$PROJECT_DIR/docs/build_history.md"
MANIFEST="$RELEASES_DIR/v${VERSION}-manifest.json"
CHANGELOG="$PROJECT_DIR/CHANGELOG.md"

# Release notes resolver (prefer CHANGELOG section for this version)
RELEASE_NOTES="${SUMMARY:-Release v$VERSION}"
if [ -f "$CHANGELOG" ]; then
    CHANGELOG_NOTES=$(awk -v ver="v$VERSION" '
      BEGIN { in_section=0 }
      /^##[[:space:]]+v/ {
        if ($2 == ver) { in_section=1; next }
        else if (in_section==1) { exit }
      }
      in_section==1 { print }
    ' "$CHANGELOG" | sed '/^[[:space:]]*$/d' || true)

    if [ -n "$CHANGELOG_NOTES" ]; then
        RELEASE_NOTES="$CHANGELOG_NOTES"
    fi
fi

# --- Branch Safety Guard ---
CURRENT_BRANCH=$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")
if [ "$CURRENT_BRANCH" != "master" ] && [ "$FORCE_BRANCH" != true ]; then
    echo -e "${RED}Error: release.sh must run on master (current: $CURRENT_BRANCH).${NC}"
    echo -e "${YELLOW}If intentional, rerun with --force-branch.${NC}"
    exit 1
fi

# Dirty-tree guard (prevents accidental release with unrelated changes)
if [ "$ALLOW_DIRTY" != true ]; then
    if ! git diff --quiet || ! git diff --cached --quiet || [ -n "$(git ls-files --others --exclude-standard)" ]; then
        echo -e "${RED}Error: working tree is dirty. Commit/stash unrelated changes first.${NC}"
        echo -e "${YELLOW}Override intentionally with --allow-dirty.${NC}"
        exit 1
    fi
fi

echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo -e "${CYAN}  Silicon Sage Release Engine: v${VERSION}${NC}"
if [ "$DRY_RUN" = true ]; then
    echo -e "${YELLOW}  [DRY RUN MODE]${NC}"
fi
echo -e "${CYAN}═══════════════════════════════════════════${NC}"

# --- [0] Pre-flight Compile (BEFORE any version mutations) ---
echo -e "${YELLOW}[0/7]${NC} Pre-flight compile check..."
START_TIME=$(date +%s)
if ! ./gradlew assembleRelease --quiet --no-daemon; then
    echo -e "${RED}Pre-flight compile FAILED. Aborting — no version files modified.${NC}"
    exit 1
fi
END_TIME=$(date +%s)
echo -e "${GREEN}       ✓ Pre-flight passed ($(($END_TIME - $START_TIME))s)${NC}"

if [ ! -f "$APK_OUTPUT" ]; then
    echo -e "${RED}Error: Release APK not found at $APK_OUTPUT${NC}"; exit 1
fi

# --- [1] Sync ---
echo -e "${YELLOW}[1/7]${NC} Syncing with remote..."
git pull origin master --rebase || (echo -e "${RED}Sync failed. Resolve conflicts manually.${NC}" && exit 1)

# --- [2] Version Mutation (only after compile passes) ---
echo -e "${YELLOW}[2/7]${NC} Incrementing build metadata..."
CURRENT_CODE=$(grep -oP 'versionCode = \K\d+' "$BUILD_GRADLE")
NEW_CODE=$((CURRENT_CODE + 1))
TODAY=$(date +%Y-%m-%d)

# Auto-changelog from git log since last tag
LAST_TAG=$(git describe --tags --abbrev=0 2>/dev/null || echo "")
if [ -n "$LAST_TAG" ]; then
    AUTO_LOG=$(git log "${LAST_TAG}..HEAD" --oneline 2>/dev/null | head -10 | sed 's/"/\\"/g' | awk '{printf "    \"%s\",\n", $0}' | sed '$ s/,$//')
else
    AUTO_LOG="    \"${SUMMARY:-Initial release}\""
fi

if [ "$DRY_RUN" = false ]; then
    sed -i "s/versionCode = $CURRENT_CODE/versionCode = $NEW_CODE/" "$BUILD_GRADLE"
    sed -i "s/versionName = \".*\"/versionName = \"$VERSION\"/" "$BUILD_GRADLE"

    cat > "$VERSION_JSON" << EOF
{
  "version": "$VERSION",
  "build": $NEW_CODE,
  "date": "$TODAY",
  "changelog": [
$AUTO_LOG
  ],
  "url": "https://github.com/Vatteck/SiliconSageAIMiner/releases/tag/v$VERSION",
  "downloadUrl": "https://github.com/Vatteck/SiliconSageAIMiner/releases/download/v$VERSION/MINER_${VERSION}.apk"
}
EOF

    sed -i "s/\*\*Current Version\*\*: v.*/\*\*Current Version\*\*: v$VERSION/" "$README"
    sed -i "s/\*\*Last Updated\*\*: .*/\*\*Last Updated\*\*: $TODAY/" "$README"
fi
echo -e "${GREEN}       ✓ Version files updated (Build $NEW_CODE)${NC}"

# --- [3] Final Release Build (with signing) ---
echo -e "${YELLOW}[3/7]${NC} Building signed release APK..."
START_TIME=$(date +%s)
./gradlew assembleRelease --quiet --no-daemon
END_TIME=$(date +%s)
echo -e "${GREEN}       ✓ Build successful ($(($END_TIME - $START_TIME))s)${NC}"

# --- [4] Stage APK ---
echo -e "${YELLOW}[4/7]${NC} Staging APK..."
APK_NAME="MINER_${VERSION}.apk"
mkdir -p "$RELEASES_DIR"
if [ "$DRY_RUN" = false ]; then
    cp "$APK_OUTPUT" "$RELEASES_DIR/$APK_NAME"
fi
echo -e "${GREEN}       ✓ $APK_NAME staged to releases/${NC}"

# --- Build Log + Manifest ---
if [ "$DRY_RUN" = false ]; then
    mkdir -p "$PROJECT_DIR/docs"
    echo "## $(date '+%Y-%m-%d %H:%M') — v$VERSION (Build $NEW_CODE)" >> "$BUILD_LOG"
    echo "- Summary: ${SUMMARY:-Manual Release}" >> "$BUILD_LOG"
    echo "" >> "$BUILD_LOG"

    APK_SHA256=$(sha256sum "$RELEASES_DIR/$APK_NAME" | awk '{print $1}')
    cat > "$MANIFEST" << EOF
{
  "version": "$VERSION",
  "tag": "v$VERSION",
  "branch": "$CURRENT_BRANCH",
  "commit": "$(git rev-parse --short HEAD)",
  "buildCode": $NEW_CODE,
  "date": "$(date -Iseconds)",
  "apk": "$RELEASES_DIR/$APK_NAME",
  "apkSha256": "$APK_SHA256"
}
EOF
fi

# --- [5] Git Commit ---
echo -e "${YELLOW}[5/7]${NC} Committing changes..."
if [ "$DRY_RUN" = false ]; then
    git add "$BUILD_GRADLE" "$VERSION_JSON" "$README" "$BUILD_LOG" "$MANIFEST"
    git add -f "$RELEASES_DIR/$APK_NAME"
    git commit -m "Release v$VERSION" -m "${SUMMARY:-Auto-release}"
fi
echo -e "${GREEN}       ✓ Changes committed${NC}"

# --- [6] Tag ---
echo -e "${YELLOW}[6/7]${NC} Creating tag v$VERSION..."
if [ "$DRY_RUN" = false ]; then
    if git rev-parse "v$VERSION" >/dev/null 2>&1; then
        git tag -d "v$VERSION"
        git push origin ":refs/tags/v$VERSION" 2>/dev/null || true
    fi
    git tag -a "v$VERSION" -m "Release v$VERSION"
fi
echo -e "${GREEN}       ✓ Tag created${NC}"

# --- [7] Push & Release ---
echo -e "${YELLOW}[7/7]${NC} Pushing to GitHub..."
if [ "$DRY_RUN" = false ]; then
    git push origin master
    git push origin "v$VERSION"

    # Clobber existing release if it exists
    if gh release view "v$VERSION" >/dev/null 2>&1; then
        gh release delete "v$VERSION" --yes
    fi
    gh release create "v$VERSION" \
        --title "v$VERSION" \
        --notes "$RELEASE_NOTES" \
        "$RELEASES_DIR/$APK_NAME"
fi
echo -e "${GREEN}       ✓ Pushed and released${NC}"

# --- [8] Lean Repo (retain N latest releases) ---
echo -e "${YELLOW}[8/8]${NC} Maintaining lean repo (keeping top $KEEP_RELEASES releases)..."
if [ "$DRY_RUN" = false ]; then
    OLD_RELEASES=$(gh release list --limit 100 --json tagName --jq '.['"$KEEP_RELEASES"':][].tagName' 2>/dev/null || true)
    if [ -n "$OLD_RELEASES" ]; then
        echo -e "${YELLOW}       Purge candidates:${NC}"
        echo "$OLD_RELEASES" | sed 's/^/         - /'

        SHOULD_PURGE=false
        if [ "$YES_PURGE" = true ]; then
            SHOULD_PURGE=true
        elif [ -t 0 ]; then
            read -r -p "Proceed with purge? [y/N] " reply
            [[ "$reply" =~ ^[Yy]$ ]] && SHOULD_PURGE=true
        else
            echo -e "${YELLOW}       Non-interactive mode detected; skipping purge (use --yes-purge to enable).${NC}"
        fi

        if [ "$SHOULD_PURGE" = true ]; then
            while IFS= read -r tag; do
                [ -z "$tag" ] && continue
                echo "  Dereferencing: $tag"
                gh release delete "$tag" --yes >/dev/null 2>&1 || true
                git push origin ":refs/tags/$tag" >/dev/null 2>&1 || true
            done <<< "$OLD_RELEASES"
            echo -e "${GREEN}       ✓ Old releases purged${NC}"
        else
            echo -e "${YELLOW}       Purge skipped.${NC}"
        fi
    else
        echo -e "${GREEN}       ✓ Repo already lean${NC}"
    fi
fi

echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo -e "${GREEN}  Release v$VERSION complete!${NC}"
echo -e "${CYAN}═══════════════════════════════════════════${NC}"
echo ""
echo -e "  ${CYAN}APK:${NC} $RELEASES_DIR/$APK_NAME"
echo -e "  ${CYAN}Tag:${NC} v$VERSION"
echo ""
