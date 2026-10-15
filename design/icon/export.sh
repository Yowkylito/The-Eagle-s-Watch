#!/usr/bin/env bash
# Rasterises the icon SVGs into every platform icon. Run from the repository root:
#   design/icon/export.sh
# Needs node with the playwright package, and ImageMagick.
set -euo pipefail
R="$(cd "$(dirname "$0")/../.." && pwd)"; D="$R/design/icon"; A="$R/composeApp/src"
T="$(mktemp -d)"; trap 'rm -rf "$T"' EXIT
png() { # svg out size
  node "$D/render.js" "$1" "$2" "$3"
}
for pair in mdpi:48 hdpi:72 xhdpi:96 xxhdpi:144 xxxhdpi:192; do
  png "$D/icon.svg" "$A/androidMain/res/mipmap-${pair%%:*}/ic_launcher.png" "${pair##*:}"
done
png "$D/adaptive-foreground.svg" "$A/androidMain/res/drawable-xxxhdpi/ic_launcher_foreground.png" 432
png "$D/adaptive-background.svg" "$A/androidMain/res/drawable-xxxhdpi/ic_launcher_background.png" 432
# iOS: the App Store rejects icons with an alpha channel.
png "$D/icon-square.svg" "$T/ios.png" 1024
convert "$T/ios.png" -background '#0C2B2B' -alpha remove -alpha off "$R/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png"
W="$A/wasmJsMain/resources"
png "$D/icon.svg" "$W/icon-512.png" 512
png "$D/icon.svg" "$W/icon-192.png" 192
png "$D/icon.svg" "$W/favicon.png" 32
png "$D/icon-square.svg" "$T/touch.png" 180
convert "$T/touch.png" -background '#0C2B2B' -alpha remove -alpha off "$W/apple-touch-icon.png"
for s in 16 32 48; do png "$D/icon.svg" "$T/fav-$s.png" $s; done
convert "$T/fav-16.png" "$T/fav-32.png" "$T/fav-48.png" "$W/favicon.ico"
png "$D/icon.svg" "$R/docs/icon.png" 512
png "$D/icon.svg" "$A/commonMain/composeResources/drawable/eagle_logo.png" 512
echo exported
