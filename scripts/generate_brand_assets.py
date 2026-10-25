"""Export the approved C-and-dots icon. Requires Python 3 and Pillow.

Geometry is shared by Android vectors, SVGs, and supersampled raster exports.
This script only regenerates icons; it does not change the social card.
"""
from pathlib import Path
from math import cos, sin, radians
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
RES = ROOT / 'app/src/main/res'
ASSETS = ROOT / 'assets'
WEB = ASSETS / 'favicons'
LAYERS = ASSETS / 'android'
for folder in (WEB, LAYERS):
    folder.mkdir(parents=True, exist_ok=True)
BG, TILE, INK = '#4F40EE', '#7566F2', '#FFFFFF'
# Android's 108dp canvas includes 18dp of overscan on each edge.
TILE_PATH = 'M44,27 H64 A17,17 0 0 1 81,44 V64 A17,17 0 0 1 64,81 H44 A17,17 0 0 1 27,64 V44 A17,17 0 0 1 44,27 Z'
C_PATH = 'M61.0711,46.9289 A10,10 0 1 0 61.0711,61.0711'
DOT_PATH = 'M56.7,35 A2.7,2.7 0 1 1 51.3,35 A2.7,2.7 0 1 1 56.7,35 Z M56.7,73 A2.7,2.7 0 1 1 51.3,73 A2.7,2.7 0 1 1 56.7,73 Z'

# Extra separation keeps the dots distinct when the launcher applies one tint.
MONO_DOT_PATH = DOT_PATH.replace(",35", ",32").replace(",73", ",76")

def vector(background=False, mono=False):
    body = (f'<path android:fillColor="{BG}" android:pathData="M0,0 H108 V108 H0 Z" />' if background else '')
    if not background:
        # Themed icons keep the C and dots; an opaque tile would obscure them.
        body = '' if mono else f'<path android:fillColor="{TILE}" android:pathData="{TILE_PATH}" />'
        body += f'<path android:fillColor="@android:color/transparent" android:strokeColor="{INK}" android:strokeWidth="6" android:strokeLineCap="round" android:pathData="{C_PATH}" />'
        body += f'<path android:fillColor="{INK}" android:pathData="{MONO_DOT_PATH if mono else DOT_PATH}" />'
    return f'''<?xml version="1.0" encoding="utf-8"?>
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="108" android:viewportHeight="108">
    {body}
</vector>
'''

for name, opts in [('foreground', {}), ('background', {'background': True}), ('monochrome', {'mono': True})]:
    (RES/f'drawable/ic_launcher_{name}.xml').write_text(vector(**opts))
(RES/'values/ic_launcher_background.xml').write_text(f'<resources>\n    <color name="ic_launcher_background">{BG}</color>\n</resources>\n')
for name in ('ic_launcher', 'ic_launcher_round'):
    (RES/f'mipmap-anydpi-v26/{name}.xml').write_text('''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@drawable/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_launcher_foreground" />
    <monochrome android:drawable="@drawable/ic_launcher_monochrome" />
</adaptive-icon>
''')

def svg(background=True, foreground=True):
    body = f'<path fill="{BG}" d="M0 0h108v108H0z"/>' if background else ''
    if foreground:
        body += f'<path fill="{TILE}" d="{TILE_PATH}"/><path fill="none" stroke="{INK}" stroke-width="6" stroke-linecap="round" d="{C_PATH}"/><path fill="{INK}" d="{DOT_PATH}"/>'
    return f'<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 108 108">{body}</svg>\n'
(ASSETS/'logo.svg').write_text(svg())
(WEB/'favicon.svg').write_text(svg())
(LAYERS/'foreground.svg').write_text(svg(False))
(LAYERS/'background.svg').write_text(svg(True, False))

# Render from the same geometry at 12x, then downsample for smooth small icons.
S = 12
BASE = 108*S

def render(background=True, foreground=True, mono=False):
    im = Image.new('RGBA', (BASE, BASE), BG if background else (0,0,0,0))
    d = ImageDraw.Draw(im)
    box = lambda a,b,c,e: tuple(round(v*S) for v in (a,b,c,e))
    if foreground:
        if not mono:
            d.rounded_rectangle(box(27,27,81,81), radius=17*S, fill=TILE)
        # Trace the centerline so raster and Android/SVG stroke geometry match.
        points = [(round((54+10*cos(radians(t)))*S),round((54+10*sin(radians(t)))*S)) for t in range(45,316)]
        d.line(points, fill=INK, width=6*S, joint='curve')
        for x,y in (points[0],points[-1]):
            d.ellipse((x-3*S,y-3*S,x+3*S,y+3*S),fill=INK)
        for y in ((32,76) if mono else (35,73)):
            d.ellipse(box(51.3,y-2.7,56.7,y+2.7),fill=INK)
    return im

master = render()
foreground = render(False)
for name, im in [('foreground',foreground), ('background',render(True,False)), ('monochrome',render(False,True,True))]:
    im.resize((432,432),Image.Resampling.LANCZOS).save(LAYERS/f'{name}.png')

def export(im, size, destination):
    im.resize((size,size),Image.Resampling.LANCZOS).save(destination)

# Legacy icons match the normal 72dp adaptive viewport, not its overscan.
legacy = master.crop((18*S,18*S,90*S,90*S))
for density,n in [('mdpi',48),('hdpi',72),('xhdpi',96),('xxhdpi',144),('xxxhdpi',192)]:
    folder = RES/f'mipmap-{density}'
    export(legacy,n,folder/'ic_launcher.png')
    rounded = legacy.copy()
    mask = Image.new('L',rounded.size); ImageDraw.Draw(mask).ellipse((0,0,rounded.width-1,rounded.height-1),fill=255)
    rounded.putalpha(mask)
    export(rounded,n,folder/'ic_launcher_round.png')
export(master,512,ASSETS/'logo.png')
export(master.convert('RGB'),512,ROOT/'app/src/main/ic_launcher-web.png')
for n in (16,32,48,180,192,512):
    name = 'apple-touch-icon.png' if n==180 else f'favicon-{n}x{n}.png'
    export(master,n,WEB/name)
master.save(WEB/'favicon.ico',sizes=[(16,16),(32,32),(48,48)])
(WEB/'site.webmanifest').write_text('''{
  "name": "Calcula",
  "short_name": "Calcula",
  "icons": [
    {"src": "favicon-192x192.png", "sizes": "192x192", "type": "image/png", "purpose": "any maskable"},
    {"src": "favicon-512x512.png", "sizes": "512x512", "type": "image/png", "purpose": "any maskable"}
  ],
  "theme_color": "#4F40EE",
  "background_color": "#4F40EE",
  "display": "standalone"
}
''')
# Review sheet: actual Android crop/masks and the monochrome mark.
sheet = Image.new('RGB',(1000,300),'#171720')
for i,shape in enumerate(('circle','squircle','square','themed')):
    icon = (render(True,True) if shape!='themed' else render(False,True,True)).crop((18*S,18*S,90*S,90*S)).resize((200,200),Image.Resampling.LANCZOS)
    if shape=='themed':
        bg=Image.new('RGBA',icon.size,'#494356'); bg.alpha_composite(icon); icon=bg
    mask = Image.new('L',icon.size)
    m=ImageDraw.Draw(mask)
    if shape in ('circle','themed'): m.ellipse((0,0,199,199),fill=255)
    else: m.rounded_rectangle((0,0,199,199),radius=45 if shape=='squircle' else 0,fill=255)
    sheet.paste(icon,(25+i*250,30),mask)
    ImageDraw.Draw(sheet).text((25+i*250,250),shape,fill='white')
sheet.save(LAYERS/'launcher-preview.png')
print('Exported Android vectors/layers, all launcher densities, logo, and favicon bundle.')
