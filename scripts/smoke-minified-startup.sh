#!/usr/bin/env bash
# Run against a disposable emulator with a locally signed, minified release APK.
set -euo pipefail
serial=${1:?Usage: smoke-minified-startup.sh emulator-PORT signed-release.apk}
apk=${2:?Usage: smoke-minified-startup.sh emulator-PORT signed-release.apk}
[[ $serial == emulator-* ]] || { echo 'Use a disposable emulator.' >&2; exit 1; }
package=com.bernaferrari.calcula
activity=$package/com.bernaferrari.calculator.MainActivity
adb -s "$serial" install -r "$apk"
for attempt in 1 2 3; do
  adb -s "$serial" shell am force-stop "$package"
  adb -s "$serial" shell am start -W -n "$activity"
  sleep 5
  if ! adb -s "$serial" shell pidof "$package" >/dev/null; then
    echo "Cold launch $attempt failed; inspect logcat for the exception." >&2
    exit 1
  fi
  echo "Cold launch $attempt: process alive"
done
