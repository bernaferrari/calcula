/** Store listing for Calcula: copy files, artwork through Screenshot Studio, and an ordered upload manifest. */
import {readFile, writeFile, stat} from 'node:fs/promises';
import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {resolve, dirname} from 'node:path';
import {parseArgs} from 'node:util';
import {createRequire} from 'node:module';
import {cardsFor, devicesFor, outputFile, withCopy} from './studio/gallery.js';
const require = createRequire(import.meta.url), sharp = require('sharp');

const {values: args} = parseArgs({options: {validate: {type: 'boolean'}, group: {type: 'string'}, locale: {type: 'string'}, 'text-only': {type: 'boolean'}}});
const here = dirname(fileURLToPath(import.meta.url));
const read = async (file, fallback) => { try { return JSON.parse(await readFile(file, 'utf8')); } catch (error) { if (fallback !== undefined && error.code === 'ENOENT') return fallback; throw error; } };
const config = await read(resolve(here, 'studio.json'));
const defaultLocale = config.defaultLocale || 'en-US';
const locales = args.locale ? [args.locale] : config.locales || [defaultLocale];
// Store copy (title and descriptions) per locale; a locale without listing.json keeps the default listing in Play.
for (const code of locales) {
  const listing = await read(resolve(here, code, 'listing.json'), null);
  if (!listing) continue;
  for (const [key, file, max] of [['title', 'title.txt', 30], ['shortDescription', 'short-description.txt', 80], ['fullDescription', 'full-description.txt', 4000]]) {
    if (!listing[key] || listing[key].length > max) throw Error(`${code}: ${key} must contain 1–${max} characters`);
    await writeFile(resolve(here, code, file), listing[key] + '\n');
  }
}
if (args['text-only']) process.exit(0);

const galleries = await read(resolve(here, 'gallery.json'));
const profiles = await read(resolve(here, 'profiles.json'));
if (args.group && !galleries[args.group]) throw Error('Unknown export group');
if (cardsFor(galleries, 'phone').length > 8) throw Error('Google Play shows at most eight phone screenshots.');
if (!args.validate) execFileSync(process.execPath, [resolve(here, 'studio/export.cjs'), ...(args.group ? ['--kind', args.group] : []), ...(args.locale ? ['--locale', args.locale] : [])], {stdio: 'inherit'});

// Every asset is checked and listed in upload order, per locale, even after a partial render.
const defaultCopy = await read(resolve(here, defaultLocale, 'copy.json'), {});
let total = 0;
for (const code of locales) {
  const locale = resolve(here, code), copy = await read(resolve(locale, 'copy.json'), {});
  const assets = [];
  for (const kind of Object.keys(galleries)) {
    if (profiles[kind].defaultLocaleOnly && code !== defaultLocale) continue;  // README art is rendered once
    const {width, height} = profiles[kind];
    for (const [i, layout] of cardsFor(galleries, kind).entries()) {
      const {card} = withCopy(layout, kind, copy, defaultCopy);
      const file = outputFile(kind, card, i), full = resolve(locale, file), meta = await sharp(full).metadata(), info = await stat(full);
      if (meta.width !== width || meta.height !== height) throw Error(`Wrong dimensions: ${code}/${file}`);
      if (info.size > 8 * 1024 * 1024) throw Error(`Image exceeds 8 MB: ${code}/${file}`);
      if (kind === 'feature' && meta.hasAlpha) throw Error('Feature graphic must be opaque');
      if (kind === 'icon' && !meta.hasAlpha) throw Error('App icon must be a 32-bit PNG');
      assets.push({file, width, height, bytes: info.size, ...(card.alt ? {alt: card.alt} : {}), ...(devicesFor(card).length ? {sources: devicesFor(card).map(d => `raw/${d.raw}`)} : {})});
    }
  }
  total += assets.length;
  if (!args.validate) await writeFile(resolve(locale, 'assets.json'), JSON.stringify({generatedAt: new Date().toISOString(), campaign: 'A little flick goes a long way',
    source: 'Actual Android resizable-emulator screenshots with status and gesture-navigation bars; deterministic HTML/CSS framing by Screenshot Studio', assets}, null, 2) + '\n');
}
console.log(`${args.validate ? 'Validated' : 'Exported'} ${total} assets across ${locales.length} locale(s) and all copy limits.`);
