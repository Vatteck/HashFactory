#!/bin/bash
# Basic release smoke gate for Android APK
# Usage: scripts/release-smoke.sh [apk_path]

set -euo pipefail
APK_PATH="${1:-/home/vatteck/Projects/SiliconSageAIMiner/app/build/outputs/apk/release/app-release.apk}"
APP_ID="com.siliconsage.miner"
MAIN_ACTIVITY="com.siliconsage.miner.MainActivity"

if ! command -v adb >/dev/null 2>&1; then
  echo "SMOKE FAIL: adb not found"
  exit 1
fi

if [ ! -f "$APK_PATH" ]; then
  echo "SMOKE FAIL: APK not found at $APK_PATH"
  exit 1
fi

if [ -z "$(adb devices | awk 'NR>1 && $2=="device" {print $1}' | head -n1)" ]; then
  echo "SMOKE FAIL: no adb device connected"
  exit 1
fi

echo "SMOKE: installing $APK_PATH"
adb install -r "$APK_PATH" >/tmp/smoke_install.log 2>&1 || { cat /tmp/smoke_install.log; echo "SMOKE FAIL: install"; exit 1; }

echo "SMOKE: launching $APP_ID/$MAIN_ACTIVITY"
adb shell am start -n "$APP_ID/$MAIN_ACTIVITY" >/tmp/smoke_launch.log 2>&1 || { cat /tmp/smoke_launch.log; echo "SMOKE FAIL: launch"; exit 1; }

sleep 3
if adb shell pidof "$APP_ID" >/dev/null 2>&1; then
  echo "SMOKE PASS: app running"
  exit 0
else
  echo "SMOKE FAIL: app process not running"
  exit 1
fi
