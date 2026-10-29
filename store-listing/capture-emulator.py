#!/usr/bin/env python3
"""Capture production Android screens, including the real System UI, in an isolated package.

`node store-listing/studio/recapture.cjs` builds the storeCapture build and runs this per language and
device group. Run alone after `./gradlew :androidApp:assembleStoreCapture`:
python3 store-listing/capture-emulator.py [--profile phone] [--id 02-graph] [--locale de-DE]

Scenes and device groups come from design.json; each scene is started by intent, so there is no tapping.
The emulator plumbing (display mode, size and density, demo-mode status bar, screencap checks, layout
sidecars, cleanup) is the studio's capture.py. Writes <locale>/raw/<group>/<id>.png and capture-manifest.json.
"""
import hashlib
import json
import subprocess
import sys
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
sys.path.insert(0, str(HERE / 'studio'))
from capture import Device, parser  # noqa: E402

parser = parser(__doc__)
parser.add_argument('--id', help='Capture one configured scene within the selected --profile')
args = parser.parse_args()
group_only = None if args.profile in (None, 'all') else args.profile
if args.id and not group_only:
    parser.error('--id requires --profile')
config = json.loads((HERE / 'design.json').read_text())
defaults = config['captureDefaults']
apk = ROOT / 'androidApp/build/outputs/apk/storeCapture/androidApp-storeCapture.apk'
if not apk.exists():
    raise SystemExit('Build first: ./gradlew :androidApp:assembleStoreCapture')
d = Device(args, 'com.bernaferrari.calcula.storecapture', 'com.bernaferrari.calculator.StoreCaptureActivity',
           apk=apk, tap_map=None, offline=False)
records = []
with d:
    for group, profile in config['profiles'].items():
        if group_only and group_only != group:
            continue
        d.adb('emu', 'resize-display', {'phone': 0, 'tablet7': 2, 'tablet10': 2, 'desktop': 3}[group])
        density = round(profile['density'] * 160)
        views = [s for s in config['slides'] if group in s['groups']] + config.get('captureVariants', {}).get(group, [])
        if args.id:
            views = [s for s in views if s['id'] == args.id]
            if not views:
                raise SystemExit(f'No capture {args.id} in {group}')
        size = None
        for slide in views:
            target = (slide.get('width', profile.get('captureWidth', profile['width'])),
                      slide.get('height', profile.get('captureHeight', profile['height'])))
            if size != target:  # alternate orientations get their own size and a fresh status bar
                d.use(group, *target, density)
                size = target
            palette = slide.get('palette', defaults['palette'])
            d.launch(wait=2.5, scene=slide['scene'], theme=slide.get('theme', defaults['theme']), palette=palette,
                     style=slide.get('style', defaults['style']))
            file, = d.shot(f'{group}/{slide["id"]}.png', settle=0)
            records.append({'file': str(file.relative_to(HERE)), 'sha256': hashlib.sha256(file.read_bytes()).hexdigest(),
                            'scene': slide['scene'], 'palette': palette, 'width': target[0], 'height': target[1],
                            'densityDpi': density})
    d.adb('emu', 'resize-display', 0)
if args.id:
    print('Refreshed 1 screen; the full capture manifest was not changed.')
    sys.exit(0)
capture_count = len(records)
previous_manifest = HERE / d.locale / 'capture-manifest.json'
if group_only and previous_manifest.exists():
    prior = json.loads(previous_manifest.read_text())
    # A phone-only refresh retains the provenance of untouched tablet/desktop captures.
    records += [record for record in prior.get('captures', []) if not record['file'].startswith(f'{d.locale}/raw/{group_only}/')]
manifest = {'source': 'adb exec-out screencap from the Android Resizable_Experimental emulator',
            'locale': d.locale,
            'commit': subprocess.check_output(['git', 'rev-parse', 'HEAD'], cwd=ROOT, text=True).strip(),
            'apkSha256': hashlib.sha256(apk.read_bytes()).hexdigest(), 'serial': d.serial,
            'systemUi': 'real status bar with 09:41 clock; full battery/Wi-Fi; gesture navigation pill',
            'captures': records}
previous_manifest.write_text(json.dumps(manifest, indent=2) + '\n')
print(f'Refreshed {capture_count} real Android screens; manifest tracks {len(records)} captures.')
