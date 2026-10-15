# App icon source

A Philippine eagle in profile against a coral sun, on a deep night-jade tile. It is flat, drawn from a handful of
shapes, and still reads at favicon size. The scene is the same one the app's header bands use.

`eagle.py` holds the geometry and colours and writes every SVG below. `export.sh` rasterises them into each
platform's icon files.

```
python3 design/icon/eagle.py design/icon
design/icon/export.sh            # needs node with playwright, and ImageMagick
```

| File | Used for |
|---|---|
| `icon.svg` | Master with a rounded tile: Android legacy icons, web icons and favicons, `docs/icon.png` |
| `icon-square.svg` | Full bleed: the iOS App Store icon, the Apple touch icon and the in-app logo, which are masked or clipped by the platform |
| `adaptive-background.svg`, `adaptive-foreground.svg` | Android adaptive icon layers; the eagle and sun sit inside the 66dp safe zone of the 108dp canvas |
| `adaptive-monochrome.svg` | Android 13 themed icon: the eagle as one shape, tinted by the system |
