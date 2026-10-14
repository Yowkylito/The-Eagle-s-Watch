# App icon source

`eagle-glyph.png` is the eagle cut out of the original artwork. The SVGs place it on the glossy flag-blue tile.

| File | Used for |
|---|---|
| `icon.svg` | Master (rounded tile): Android legacy icons, web favicons, in-app logo |
| `icon-square.svg` | iOS App Store icon (full bleed; iOS applies its own mask) |
| `adaptive-background.svg`, `adaptive-foreground.svg` | Android adaptive icon layers (108dp canvas, eagle inside the 66dp safe zone) |

Render with any browser engine that supports SVG lighting filters (for example headless Chromium).
