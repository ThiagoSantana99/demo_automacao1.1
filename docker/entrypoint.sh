#!/usr/bin/env bash
set -euo pipefail

export DISPLAY="${DISPLAY:-:99}"
export SCREEN_WIDTH="${SCREEN_WIDTH:-1920}"
export SCREEN_HEIGHT="${SCREEN_HEIGHT:-1080}"
export SCREEN_DEPTH="${SCREEN_DEPTH:-24}"
export BROWSER="${BROWSER:-chrome}"
export CUCUMBER_TAG="${CUCUMBER_TAG:-@AGI}"
export PARALLEL_COUNT="${PARALLEL_COUNT:-1}"
export HEADLESS="${HEADLESS:-false}"
export CHROME_BIN="${CHROME_BIN:-/usr/bin/chromium}"
export CHROMIUM_BIN="${CHROMIUM_BIN:-/usr/bin/chromium}"
export CHROMEDRIVER_PATH="${CHROMEDRIVER_PATH:-/usr/bin/chromedriver}"

mkdir -p /tmp/.X11-unix

Xvfb "${DISPLAY}" -screen 0 "${SCREEN_WIDTH}x${SCREEN_HEIGHT}x${SCREEN_DEPTH}" &
fluxbox >/tmp/fluxbox.log 2>&1 &
x11vnc -display "${DISPLAY}" -forever -shared -nopw -rfbport 5900 >/tmp/x11vnc.log 2>&1 &
websockify --web=/usr/share/novnc/ 6080 localhost:5900 >/tmp/novnc.log 2>&1 &

until xdpyinfo -display "${DISPLAY}" >/dev/null 2>&1; do
  sleep 1
done

exec mvn clean test \
  -DLOG_LEVEL=INFO \
  "-Dcucumber.filter.tags=${CUCUMBER_TAG}" \
  "-Dbrowser=${BROWSER}" \
  "-Dwebdriver.chrome.driver=${CHROMEDRIVER_PATH}" \
  "-Dchrome.binary=${CHROME_BIN}" \
  "-Ddataproviderthreadcount=${PARALLEL_COUNT}" \
  "-Dscreen.width=${SCREEN_WIDTH}" \
  "-Dscreen.height=${SCREEN_HEIGHT}" \
  "-Dheadless=${HEADLESS}"
