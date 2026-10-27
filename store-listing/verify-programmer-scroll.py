#!/usr/bin/env python3
"""Regression check for Programmer's working-area scroll, on the isolated capture APK."""
import argparse
import os
from pathlib import Path
import re
import shutil
import subprocess
import xml.etree.ElementTree as ET

parser = argparse.ArgumentParser()
parser.add_argument('--serial', required=True)
parser.add_argument('--landscape', action='store_true')
args = parser.parse_args()
if not args.serial.startswith('emulator-'):
    parser.error('Use a disposable emulator.')
adb = shutil.which('adb') or str(Path(os.environ.get('ANDROID_HOME', Path.home() / 'Library/Android/sdk')) / 'platform-tools/adb')
package = 'com.bernaferrari.calcula.storecapture'
def shell(*words):
    return subprocess.check_output([adb, '-s', args.serial, 'shell', *map(str, words)])
def hierarchy():
    shell('uiautomator', 'dump', '/sdcard/calcula-programmer-check.xml')
    return ET.fromstring(shell('cat', '/sdcard/calcula-programmer-check.xml'))
def bounds(node):
    return list(map(int, re.findall(r'\d+', node.get('bounds'))))
def texts(root):
    return [node.get('text') for node in root.iter('node') if node.get('text')]

shell('wm', 'size', '1920x1080' if args.landscape else '1080x1920')
shell('wm', 'density', '400')
shell('settings', 'put', 'system', 'font_scale', '1.0')
shell('am', 'force-stop', package)
shell('am', 'start', '-W', '-n', f'{package}/com.bernaferrari.calculator.StoreCaptureActivity',
      '--es', 'scene', 'programmer', '--es', 'palette', 'TEAL', '--es', 'theme', 'dark')
shell('input', 'keyevent', 'KEYCODE_ESCAPE')
for n in range(1, 9):
    shell('input', 'text', f'{n}+1')
    shell('input', 'keyevent', 'KEYCODE_ENTER')
root = hierarchy()
scroll = next(node for node in root.iter('node') if node.get('scrollable') == 'true')
x1, y1, x2, y2 = bounds(scroll)
height = (y2-y1)/2.5
assert height >= 160, f'Working area is only {height:.1f} dp tall; history is trapped in a strip.'
print(f'Working area: {height:.1f} dp (old layout: 45.6 dp).')
for _ in range(2):
    shell('input', 'swipe', (x1+x2)//2, y1+40, (x1+x2)//2, y2-40, 400)
root = hierarchy()
assert '1 + 1 = 2' in texts(root), 'Earlier history cannot be reached.'
entry = next(node for node in root.iter('node') if node.get('text') == '3 + 1 = 4')
a, b, c, d = bounds(entry)
shell('input', 'tap', (a+c)//2, (b+d)//2)
root = hierarchy()
assert '1 + 1 = 2' not in texts(root), 'Reusing a result did not return to the current value.'
shell('input', 'text', 'A')
root = hierarchy()
assert '10' in texts(root), 'Typing after history reuse did not update the current readout.'
print('PASS: full working-area scroll, history reuse, and return to typing.')
