#!/usr/bin/env node
import {spawnSync} from 'node:child_process';
import {readFile} from 'node:fs/promises';
import path from 'node:path';
import {fileURLToPath} from 'node:url';
const root = path.dirname(fileURLToPath(import.meta.url));
const config = JSON.parse(await readFile(path.join(root, 'studio.json'), 'utf8'));
const results = config.locales.map(locale => spawnSync(process.execPath, [path.join(root, 'prepare-play.mjs'), locale], {encoding:'utf8'}));
for (let i=0; i<results.length; i++) process.stdout.write(`\n${config.locales[i]}\n${results[i].stdout}${results[i].stderr}`);
if (results.some(r => r.status !== 0)) {
  console.error('\nNo locales staged. Fix every reported issue, then rerun this command.');
  process.exit(1);
}
if (process.argv.includes('--stage')) {
  for (const locale of config.locales) {
    const r = spawnSync(process.execPath, [path.join(root, 'studio/export.cjs'), '--locale', locale, '--strict'], {stdio:'inherit'});
    if (r.status !== 0) { console.error(`Strict export failed for ${locale}; no upload was attempted.`); process.exit(r.status || 1); }
  }
  for (const locale of config.locales) {
    const r = spawnSync(process.execPath, [path.join(root, 'prepare-play.mjs'), locale, '--stage'], {stdio:'inherit'});
    if (r.status !== 0) process.exit(r.status || 1);
  }
}
