#!/usr/bin/env python3
"""Exercise Programmer bit controls and large-number rendering on the capture APK."""
import argparse,json,os,shutil,subprocess,re,xml.etree.ElementTree as E
from pathlib import Path
parser=argparse.ArgumentParser()
parser.add_argument('--serial',required=True)
args=parser.parse_args()
if not args.serial.startswith('emulator-'): parser.error('Use a disposable emulator.')
ADB=shutil.which('adb') or str(Path(os.environ.get('ANDROID_HOME',Path.home()/'Library/Android/sdk'))/'platform-tools/adb')
SERIAL=args.serial;PKG='com.bernaferrari.calcula.storecapture'
def run(*args):return subprocess.check_output([ADB,'-s',SERIAL,*map(str,args)])
def shell(*args):return run('shell',*args)
def tree():
 shell('uiautomator','dump','/sdcard/bits-check.xml')
 return E.fromstring(shell('cat','/sdcard/bits-check.xml'))
def tap(text,long=False):
 r=tree();nodes=list(r.iter('node'))
 for e in sorted(nodes,key=lambda n:n.get('content-desc')!=text):
  if e.get('text')==text or e.get('content-desc')==text:
   a,b,c,d=map(int,re.findall(r'\d+',e.get('bounds')));x=(a+c)//2;y=(b+d)//2
   shell('input','swipe',x,y,x,y,800) if long else shell('input','tap',x,y)
   return
 raise AssertionError(text)
def texts():return [n.get('text') for n in tree().iter('node') if n.get('text')]
def control(label):return any(n.get('content-desc')==label or n.get('text')==label for n in tree().iter('node'))
def saved_settings():
 preferences=E.fromstring(shell('run-as',PKG,'cat','shared_prefs/calculator.xml'))
 return json.loads(next(n.text for n in preferences if n.get('name')=='preferences'))
shell('wm','size','1080x1920');shell('wm','density','400')
shell('am','force-stop',PKG)
shell('am','start','-W','-n',f'{PKG}/com.bernaferrari.calculator.StoreCaptureActivity','--es','scene','programmer','--es','theme','dark','--es','palette','TEAL')
assert control('Show bits'), 'Grid should start collapsed'
r=tree()
bits=next(n for n in r.iter('node') if n.get('content-desc')=='Show bits')
bounds=lambda n:list(map(int,re.findall(r'\d+',n.get('bounds'))))
a,b,c,d=bounds(bits)
alternate=min((n for n in r.iter('node') if n.get('text')=='DEC'),key=lambda n:abs(sum(bounds(n)[1::2])-(b+d)))
_,top,_,bottom=bounds(alternate)
assert min(d,bottom)>max(b,top), 'Bits and alternate base should share the same row'
assert d-b>=120, 'Bits must keep its 48 dp tap target'
print('Compact Bits disclosure shares the alternate-base row with a 48 dp target.')
tap('Show bits');assert control('Hide bits')
assert saved_settings().get('programmerShowBits')==True, 'Expanded choice must be persisted'
tap('Bit 63: 0',long=True)
assert control('Show bits'), 'Long press must collapse the grid'
assert 'C0 FFEE' in texts(), 'Long press should not flip a bit'
tap('Show bits');shell('input','keyevent','KEYCODE_ESCAPE');tap('BIN');tap('Bit 63: 0')
r=tree();binary=[n for n in r.iter('node') if len([c for c in n.get('text','') if c in '01'])==64 and set(n.get('text',''))<={'0','1',' '}]
assert binary, 'Full 64-bit number should be retained'
a,b,c,d=map(int,re.findall(r'\d+',binary[0].get('bounds')))
print('Binary value bounds:',(a,b,c,d),'height:',d-b)
assert d-b>=100,'Long binary values should wrap, not clip on one line'
shell('input','text','+1');shell('input','keyevent','KEYCODE_ENTER')
assert not any('Incomplete' in t or 'large' in t for t in texts())
values=[n.get('text','') for n in tree().iter('node')]
assert any(len(t.replace(' ',''))==64 and set(t)<={'0','1',' '} and t.count('1')==2 for t in values), 'Top-bit + 1 must retain both set bits'
tap('Hide bits')
assert control('Show bits'), 'The grid should collapse before checking saved preferences'
assert saved_settings().get('programmerShowBits',False)==False, 'Collapsed choice must be persisted'
tap('64-bit');tap('Unsigned')
assert 'Unsigned' in texts()
assert any('9 223 372 036 854 775 809'==t for t in texts()), 'Unsigned interpretation should keep the same bits and display a positive value'
print('PASS: collapsed default, long press, preference persistence, 64-bit wrapping, top-bit + 1, and unsigned interpretation.')
