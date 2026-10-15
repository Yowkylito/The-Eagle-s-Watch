"""The Eagle's Watch icon: a Philippine eagle in profile against the sun.

Writes the master SVGs used to export every platform icon. Geometry is on a 1024 canvas.
"""
import sys

def feathers(start, spikes):
    d, (px, py) = [], start
    for (tx, ty), (vx, vy) in spikes:
        c1 = ((px + tx) / 2 + 10, (py + ty) / 2 - 24)
        c2 = ((tx + vx) / 2 + 18, (ty + vy) / 2 + 8)
        d.append(f"Q {c1[0]:.0f} {c1[1]:.0f} {tx} {ty} Q {c2[0]:.0f} {c2[1]:.0f} {vx} {vy}")
        px, py = vx, vy
    return " ".join(d)

FRONT = [((236, 606), (374, 584)), ((178, 508), (382, 488)), ((186, 400), (404, 402)), ((248, 304), (450, 336)), ((350, 236), (522, 308))]
HEAD = ("M 300 910 C 296 800 306 720 352 650 L 374 640 " + feathers((374, 640), FRONT) +
        " C 592 294 664 320 708 370 C 716 420 702 520 692 600 C 670 690 672 800 702 910 Z")
BACK = [((196, 668), (332, 622)), ((134, 574), (330, 542)), ((120, 462), (346, 448)), ((160, 346), (382, 370)), ((236, 246), (432, 302)), ((352, 182), (502, 288))]
CREST = "M 360 700 L 332 680 " + feathers((332, 680), BACK) + " L 560 300 L 420 700 Z"
BEAK = ("M 700 378 C 792 366 872 430 886 540 C 894 610 872 668 836 708 "
        "C 840 664 832 632 812 614 C 770 618 720 618 676 610 C 666 530 676 446 700 378 Z")
GAPE = "M 690 566 C 730 584 772 594 808 598"
BROW = "M 538 416 C 594 380 664 376 716 402 C 678 410 640 422 600 448 Z"

PALETTE = dict(night_top="#16433F", night_bottom="#0A2426", sun_top="#FF8A63", sun_bottom="#F2603F",
               head="#F8F1E5", crest="#D8C9AF", beak_base="#EADBC2", beak_tip="#C9B28C",
               eye="#19B5A5", pupil="#0A2426", brow="#0A2426")

def glyph(p, sun=True):
    sun_el = f'<circle cx="600" cy="440" r="262" fill="url(#sun)"/>' if sun else ""
    return f'''{sun_el}
<path d="{CREST}" fill="{p['crest']}"/>
<path d="{HEAD}" fill="{p['head']}"/>
<path d="{BEAK}" fill="url(#beak)"/>
<path d="{GAPE}" fill="none" stroke="{p['beak_tip']}" stroke-width="8" stroke-linecap="round"/>
<circle cx="604" cy="456" r="33" fill="{p['eye']}"/><circle cx="612" cy="456" r="15" fill="{p['pupil']}"/>
<circle cx="619" cy="447" r="5" fill="#FFFFFF" opacity="0.9"/>
<path d="{BROW}" fill="{p['brow']}"/>'''

def defs(p):
    return f'''<linearGradient id="night" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="{p['night_top']}"/><stop offset="1" stop-color="{p['night_bottom']}"/></linearGradient>
<linearGradient id="sun" x1="0.2" y1="0" x2="0.8" y2="1"><stop offset="0" stop-color="{p['sun_top']}"/><stop offset="1" stop-color="{p['sun_bottom']}"/></linearGradient>
<linearGradient id="beak" x1="0" y1="0" x2="1" y2="1"><stop offset="0.25" stop-color="{p['beak_base']}"/><stop offset="1" stop-color="{p['beak_tip']}"/></linearGradient>'''

def icon(p, rx):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 1024 1024">
<defs>{defs(p)}<clipPath id="tile"><rect width="1024" height="1024" rx="{rx}"/></clipPath></defs>
<g clip-path="url(#tile)"><rect width="1024" height="1024" fill="url(#night)"/>{glyph(p)}</g>
</svg>
'''

# Android adaptive icons: 108dp canvas, only the central 66dp circle is guaranteed visible.
# Scale the glyph to fit that circle: 1024 * 66/108 = 626 across, centred.
def adaptive_foreground(p):
    s = 0.62
    t = 512 - 512 * s
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 1024 1024">
<defs>{defs(p)}</defs>
<g transform="translate({t:.1f} {t + 12:.1f}) scale({s})">{glyph(p)}</g>
</svg>
'''

def adaptive_background(p):
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 1024 1024">
<defs>{defs(p)}</defs><rect width="1024" height="1024" fill="url(#night)"/>
</svg>
'''

def monochrome(p):
    """Android 13 themed icon: one colour, the system tints it."""
    s = 0.62
    t = 512 - 512 * s
    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="1024" height="1024" viewBox="0 0 1024 1024">
<defs><mask id="m"><rect width="1024" height="1024" fill="black"/><path d="{CREST}" fill="white"/><path d="{HEAD}" fill="white"/><path d="{BEAK}" fill="white"/>
<circle cx="604" cy="456" r="30" fill="black"/></mask></defs>
<g transform="translate({t:.1f} {t + 12:.1f}) scale({s})"><rect width="1024" height="1024" fill="#000" mask="url(#m)"/></g>
</svg>
'''

if __name__ == "__main__":
    out = sys.argv[1] if len(sys.argv) > 1 else "."
    files = {
        "icon.svg": icon(PALETTE, 232),
        "icon-square.svg": icon(PALETTE, 0),
        "adaptive-foreground.svg": adaptive_foreground(PALETTE),
        "adaptive-background.svg": adaptive_background(PALETTE),
        "adaptive-monochrome.svg": monochrome(PALETTE),
    }
    for name, body in files.items():
        open(f"{out}/{name}", "w").write(body)
    print("wrote", ", ".join(files))
