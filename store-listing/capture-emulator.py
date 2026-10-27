#!/usr/bin/env python3
"""Capture production Android screens, including the real System UI, in an isolated package."""
import argparse
import hashlib
import json
import os
import re
from pathlib import Path
import shutil
import subprocess
import time
import sys

HERE = Path(__file__).resolve().parent
ROOT = HERE.parent
parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True, help='Resizable emulator serial, e.g. emulator-5560')
parser.add_argument('--locale', default='en-US', help='Per-app language and output folder, e.g. de-DE')
parser.add_argument('--group', choices=['phone', 'tablet7', 'tablet10', 'desktop'])
parser.add_argument('--id', help='Capture one configured scene within the selected group')
args = parser.parse_args()
if args.id and not args.group:
    parser.error('--id requires --group')
if not re.fullmatch(r'[a-z]{2,3}(?:-[A-Za-z0-9]{2,8})*', args.locale):
    parser.error('Invalid locale')
sys.path.insert(0, str(HERE / 'studio'))
from uitree import nodes_from_xml, set_app_locale, write_sidecar
if not args.serial.startswith('emulator-'):
    parser.error('Use a disposable emulator, not a personal device.')
adb = shutil.which('adb') or str(Path(os.environ.get('ANDROID_HOME', Path.home() / 'Library/Android/sdk')) / 'platform-tools/adb')
def run(*words):
    return subprocess.check_output([adb, '-s', args.serial, *map(str, words)], stderr=subprocess.STDOUT)
def shell(*words):
    return run('shell', *words)
def demo(command, **extras):
    words=['am','broadcast','-a','com.android.systemui.demo','-e','command',command]
    for key,value in extras.items(): words += ['-e',key,str(value)]
    shell(*words)

config=json.loads((HERE/'design.json').read_text())
apk=ROOT/'androidApp/build/outputs/apk/storeCapture/androidApp-storeCapture.apk'
if not apk.exists(): raise SystemExit('Build first: ./gradlew :androidApp:assembleStoreCapture')
assert 'Override' not in (shell('wm','size') + shell('wm','density')).decode(), 'Start with no display overrides.'
run('install','-r',apk)
package='com.bernaferrari.calcula.storecapture'
shell('input','keyevent','KEYCODE_WAKEUP')
shell('wm','dismiss-keyguard')
shell('cmd','overlay','enable-exclusive','--category','com.android.internal.systemui.navbar.gestural')
shell('settings','put','system','font_scale','1.0')
shell('settings','put','global','sysui_demo_allowed','1')
records=[]
try:
    set_app_locale(run, package, args.locale)
    for group,profile in config['profiles'].items():
        if args.group and args.group!=group: continue
        mode={'phone':0,'tablet7':2,'tablet10':2,'desktop':3}[group]
        demo('exit')
        run('emu','resize-display',mode)
        density=round(profile['density']*160)
        views=[s for s in config['slides'] if group in s['groups']]+config.get('captureVariants',{}).get(group,[])
        if args.id:
            views=[s for s in views if s['id'] == args.id]
            if not views: raise ValueError(f'No capture {args.id} in {group}')
        dimensions=None
        for slide in views:
            target=(slide.get('width',profile.get('captureWidth',profile['width'])),slide.get('height',profile.get('captureHeight',profile['height'])))
            if dimensions!=target:
                # Reset demo icons across each configuration change, including alternate orientations.
                demo('exit')
                shell('wm','size',f'{target[0]}x{target[1]}')
                shell('wm','density',density)
                time.sleep(2)
                demo('enter')
                demo('clock',hhmm='0941')
                demo('notifications',visible='false')
                demo('battery',level=100,plugged='false')
                demo('network',wifi='show',level=4,mobile='hide',fully='true')
                dimensions=target
            shell('am','force-stop',package)
            shell('am','start','-W','-n',f'{package}/com.bernaferrari.calculator.StoreCaptureActivity',
                  '--es','scene',slide['scene'], '--es','theme',slide.get('theme',config['captureDefaults']['theme']),
                  '--es','palette',slide.get('palette',config['captureDefaults']['palette']),
                  '--es','style',slide.get('style',config['captureDefaults']['style']))
            time.sleep(2.5)
            display=re.search(r'Display (\d+) \(HWC display 0\)',shell('dumpsys','SurfaceFlinger','--display-id').decode()).group(1)
            pixels=run('exec-out','screencap','-p','-d',display)
            width=int.from_bytes(pixels[16:20],'big');height=int.from_bytes(pixels[20:24],'big')
            if (width,height)!=dimensions: raise RuntimeError(f'Wrong capture size: {group}: got {width}x{height}, expected {dimensions[0]}x{dimensions[1]}')
            file=HERE/args.locale/'raw'/group/f"{slide['id']}.png"
            shell('uiautomator','dump','--compressed','/sdcard/calcula-layout.xml')
            xml=shell('cat','/sdcard/calcula-layout.xml')
            nodes=nodes_from_xml(xml)
            if not any(node.get('pkg') == package for node in nodes) or any(
                node.get('pkg') == 'android' and ("isn't responding" in node.get('text', '') or 'keeps stopping' in node.get('text', ''))
                for node in nodes
            ):
                raise RuntimeError(f'Unexpected screen or system error in {group}/{slide["id"]}; capture discarded')
            file.parent.mkdir(parents=True,exist_ok=True);file.write_bytes(pixels)
            write_sidecar(file, xml, width, height)
            records.append({'file':str(file.relative_to(HERE)), 'sha256':hashlib.sha256(pixels).hexdigest(),
                            'scene':slide['scene'],'palette':slide.get('palette',config['captureDefaults']['palette']),
                            'width':width,'height':height,'densityDpi':round(profile['density']*160)})
            print(f'{group}/{slide["id"]}',flush=True)
finally:
    shell('am','force-stop',package)
    set_app_locale(run, package, None)
    demo('exit')
    shell('wm','size','reset')
    shell('wm','density','reset')
capture_count=len(records)
if args.id:
    print(f'Refreshed {capture_count} screen; the full capture manifest was not changed.')
    sys.exit(0)
previous_manifest=HERE/args.locale/'capture-manifest.json'
if args.group and previous_manifest.exists():
    prior=json.loads(previous_manifest.read_text())
    # A phone-only refresh retains the provenance of untouched tablet/desktop captures.
    records += [record for record in prior.get('captures',[]) if not record['file'].startswith(f'{args.locale}/raw/{args.group}/')]
manifest={'source':'adb exec-out screencap from the Android Resizable_Experimental emulator',
          'locale':args.locale,
          'commit':subprocess.check_output(['git','rev-parse','HEAD'],cwd=ROOT,text=True).strip(),
          'apkSha256':hashlib.sha256(apk.read_bytes()).hexdigest(),'serial':args.serial,
          'systemUi':'real status bar with 09:41 clock; full battery/Wi-Fi; gesture navigation pill',
          'captures':records}
(HERE/args.locale/'capture-manifest.json').write_text(json.dumps(manifest,indent=2)+'\n')
print(f'Refreshed {capture_count} real Android screens; manifest tracks {len(records)} captures.')
