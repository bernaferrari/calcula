#!/usr/bin/env node
// Validate a locale and stage images for fastlane without touching Play Console.
import fs from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
import sharp from 'sharp';

const root = path.dirname(fileURLToPath(import.meta.url));
const args = process.argv.slice(2);
const locale = args[0];
const stage = args.includes('--stage');
if (!locale || args.includes('--help')) {
  console.log('Usage: npm run play:prepare -- <locale> [--stage]\nChecks translation coverage and source images; stages tablet artwork on 9:16 or 16:9 canvases. --stage writes a fastlane metadata directory only after every check passes.');
  process.exit(locale ? 0 : 1);
}
const read = async p => JSON.parse(await fs.readFile(p, 'utf8'));
const config = await read(path.join(root, 'studio.json'));
if (!config.locales.includes(locale)) throw new Error(`Unknown locale: ${locale}`);
const base = await read(path.join(root, config.defaultLocale, 'copy.json'));
const copy = await read(path.join(root, locale, 'copy.json'));
const errors = [];
let listing;
try {
  listing = await read(path.join(root, locale, 'listing.json'));
  for (const [key, max] of Object.entries({title:30, shortDescription:80, fullDescription:4000})) {
    if (!listing[key]?.trim() || [...listing[key]].length > max) errors.push(`Missing or invalid listing ${key} (maximum ${max} characters)`);
  }
} catch { errors.push('Missing or unreadable listing.json'); }
for (const [key, fields] of Object.entries(base)) for (const field of Object.keys(fields)) {
  if (!copy[key]?.[field]?.trim()) errors.push(`Missing translation: ${key}.${field}`);
}
const gallery = await read(path.join(root, 'gallery.json'));
const {cardsFor, devicesFor, outputFile, nodesFile} = await import('./studio/gallery.js');
const files = [];
const names = {phone:'phoneScreenshots', tablet7:'sevenInchScreenshots', tablet10:'tenInchScreenshots'};
for (const kind of Object.keys(names)) {
  for (const [i, card] of cardsFor(gallery, kind).entries()) {
    for (const device of devicesFor(card)) {
      for (const raw of [device.raw, nodesFile(device.raw)]) {
        try { await fs.access(path.join(root, locale, 'raw', raw)); }
        catch { errors.push(`Missing capture or layout: ${raw}`); }
      }
    }
    const name = outputFile(kind, card, i);
    const file = path.join(root, locale, name);
    try {
      const meta = await sharp(file).metadata();
      if (kind === 'phone' && meta.width * 16 !== meta.height * 9 && meta.width * 9 !== meta.height * 16) errors.push(`${name}: ${meta.width}×${meta.height} is not 9:16 or 16:9`);
      if (kind !== 'phone' && !(meta.width > 0 && meta.height > 0)) errors.push(`${name}: invalid dimensions`);
      const stat = await fs.stat(file);
      if (stat.size > 8_000_000) errors.push(`${name}: exceeds 8 MB`);
      files.push({file, kind, dir:names[kind], name:`${String(i+1).padStart(2,'0')}.jpg`});
    } catch { errors.push(`Missing or unreadable image: ${name}`); }
  }
}
for (const [source, target, width, height, limit] of [
  ['graphics/app-icon.png','icon.png',512,512,1_000_000],
  ['graphics/feature-graphic.png','featureGraphic.png',1024,500,15_000_000]
]) {
  const file = path.join(root, locale, source);
  try {
    const meta = await sharp(file).metadata();
    const stat = await fs.stat(file);
    if (meta.width !== width || meta.height !== height || stat.size > limit) errors.push(`${source}: invalid dimensions or file size`);
    files.push({file, kind:'graphic', dir:'', name:target});
  } catch { errors.push(`Missing or unreadable image: ${source}`); }
}
if (errors.length) {
  console.error(`Cannot stage ${locale}:\n${[...new Set(errors)].map(e => `  - ${e}`).join('\n')}`);
  process.exit(1);
}
console.log(`${locale}: translation, capture, and image size checks passed.`);
console.log('Review a fresh strict export before uploading; these checks alone do not verify visual quality or freshness.');
if (stage) {
  const out = path.join(root, 'play-staging', locale, 'images');
  await fs.rm(out, {recursive:true, force:true});
  for (const item of files) {
    const dir = path.join(out, item.dir);
    await fs.mkdir(dir, {recursive:true});
    const target = path.join(dir, item.name);
    if (item.kind === 'phone' || item.kind === 'graphic') await fs.copyFile(item.file, target);
    else {
      const size = item.kind === 'tablet7' ? {width:1080, height:1920} : {width:1920, height:1080};
      await sharp(item.file).resize({...size, fit:'contain', background:'#f8f4e9'}).jpeg({quality:96}).toFile(target);
    }
  }
  for (const [key, filename] of Object.entries({title:'title.txt', shortDescription:'short_description.txt', fullDescription:'full_description.txt'})) {
    await fs.writeFile(path.join(root, 'play-staging', locale, filename), listing[key] + '\n');
  }
  console.log(`Staged text and screenshots in ${path.relative(root, path.dirname(out))}. No Play Console changes were made.`);
}
