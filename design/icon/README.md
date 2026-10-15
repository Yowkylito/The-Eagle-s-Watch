# App icon source

`eagle-glyph.png` is the eagle cut out of the original artwork, with its eyes and outline shifted from blue to the
app's jade. The SVGs place it on the glossy jade tile with the flag's sun, tone on tone.

| File | Used for |
|---|---|
| `icon.svg` | Master (rounded tile): Android legacy icons, web favicons, in-app logo |
| `icon-square.svg` | iOS App Store icon (full bleed; iOS applies its own mask) |
| `adaptive-background.svg`, `adaptive-foreground.svg` | Android adaptive icon layers (108dp canvas, eagle inside the 66dp safe zone) |

`export.sh` renders every platform size from these files with headless Chromium, which supports the SVG
lighting filters the gloss uses:

```
design/icon/export.sh            # needs node with playwright, and ImageMagick
```
