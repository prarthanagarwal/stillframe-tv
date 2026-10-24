#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
SERIAL="${TV_SERIAL:-${1:-}}"
[ -n "$SERIAL" ] || { echo 'Usage: TV_SERIAL=TV_IP:5555 ./build-and-install.sh'; exit 1; }
command -v adb >/dev/null || { echo 'Install Android platform-tools (adb) first.' >&2; exit 1; }
./gradlew --no-daemon assembleRelease --console=plain
adb -s "$SERIAL" install -r app/build/outputs/apk/release/app-release.apk
if [[ "${GRANT_ADB_CONTROL:-0}" == "1" ]]; then
  adb -s "$SERIAL" shell pm grant com.gathin.tvhome android.permission.WRITE_SECURE_SETTINGS
fi
adb -s "$SERIAL" shell am force-stop com.gathin.tvhome
START="$(adb -s "$SERIAL" shell am start -W -n com.gathin.tvhome/.MainActivity)"
printf '%s\n' "$START"
[[ "$START" == *'Status: ok'* && "$START" != *'Error:'* ]] || { echo 'Launcher did not start. The old Home packages were not disabled.' >&2; exit 1; }
adb -s "$SERIAL" shell cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity
OWNER="$(adb -s "$SERIAL" shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME)"
[[ "$OWNER" == *'com.gathin.tvhome/.MainActivity'* ]] || { echo 'The firmware still selects its original Home. Follow setup/README.md; this script disabled no packages.' >&2; exit 1; }
echo 'Stillframe installed. Press Home on your remote.'
