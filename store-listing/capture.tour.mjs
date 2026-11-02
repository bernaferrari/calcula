// Calcula's store screens on the capture emulator: production screens with the real System UI, in an isolated
// package. Scenes come from design.json (slides and captureVariants per device group); StoreCaptureActivity opens
// each one from intent extras (scene, theme, palette, style), so there is no tapping. Writes
// <locale>/raw/<group>/<id>.png and <locale>/capture-manifest.json (where each capture came from).
// `npx screenshot-studio recapture` builds the storeCapture build, runs this per language, and exports.
// Alone, after ./gradlew :androidApp:assembleStoreCapture:
//   npx screenshot-studio tour [--profile phone] [--only 02-graph] [--locale de-DE]
import crypto from 'node:crypto';
import fs from 'node:fs';
import path from 'node:path';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';

const here = path.dirname(fileURLToPath(import.meta.url));
const apk = '../androidApp/build/outputs/apk/storeCapture/androidApp-storeCapture.apk';
const design = JSON.parse(fs.readFileSync(path.join(here, 'design.json'), 'utf8'));
/** The Resizable emulator's display mode per group: phone, tablet, desktop (windowed apps). */
const DISPLAY = {phone: 0, tablet7: 2, tablet10: 2, desktop: 3};
const sha256 = file => crypto.createHash('sha256').update(fs.readFileSync(file)).digest('hex');
let records = [];

/** @type {import('screenshot-studio').Tour} */
export default {
  app: 'com.bernaferrari.calcula.storecapture',
  activity: 'com.bernaferrari.calculator.StoreCaptureActivity',
  install: apk,
  offline: false,
  tapMap: false,
  setup() { records = []; },
  async run(d) {
    const profile = design.profiles[d.profile];
    if (!profile) return;
    d.adb('emu', 'resize-display', DISPLAY[d.profile]);
    const density = Math.round(profile.density * 160);
    const views = [...design.slides.filter(slide => slide.groups.includes(d.profile)), ...(design.captureVariants?.[d.profile] || [])];
    let size;
    for (const slide of views) {
      const width = slide.width ?? profile.captureWidth ?? profile.width, height = slide.height ?? profile.captureHeight ?? profile.height;
      // Every group starts on its display mode's size; alternate orientations get their own size and status bar.
      if (size !== `${width}x${height}`) { await d.resize(width, height, density); size = `${width}x${height}`; }
      const {theme, palette, style} = {...design.captureDefaults, ...slide};
      await d.launch({extras: {scene: slide.scene, theme, palette, style}});
      const file = await d.shot(slide.id, {settle: 0});
      if (file) records.push({file, sha256: sha256(path.join(here, file)), scene: slide.scene, palette, width, height, densityDpi: density});
    }
  },
  teardown(d) {
    d.adb('emu', 'resize-display', 0);
    if (!records.length) return;
    // Captures this run didn't retake (another group, a scene left out with --only) keep their earlier provenance.
    const file = path.join(here, d.locale, 'capture-manifest.json');
    const retaken = new Set(records.map(record => record.file));
    const earlier = fs.existsSync(file) ? JSON.parse(fs.readFileSync(file, 'utf8')).captures || [] : [];
    const captures = [...records, ...earlier.filter(record => !retaken.has(record.file))].sort((a, b) => a.file.localeCompare(b.file));
    fs.writeFileSync(file, JSON.stringify({
      source: 'adb exec-out screencap from the Android Resizable_Experimental emulator', locale: d.locale,
      commit: execFileSync('git', ['rev-parse', 'HEAD'], {cwd: here, encoding: 'utf8'}).trim(),
      apkSha256: sha256(path.join(here, apk)), serial: d.serial,
      systemUi: 'real status bar with 09:41 clock; full battery/Wi-Fi; gesture navigation pill', captures,
    }, null, 2) + '\n');
    console.log(`Refreshed ${records.length} real Android screens; the manifest tracks ${captures.length}.`);
  },
};
