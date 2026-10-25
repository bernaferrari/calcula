# Calcula · Google Play listing kit

Real shared Compose UI captures, editable copy, and deterministic HTML/CSS artwork. Inspired by the sibling Wi-Fi Analyzer listing workflow. No AI-generated UI, design-service account, external fonts, or hand-edited screenshots are required.

## What to edit

| Change | Source |
| --- | --- |
| App title, short/full descriptions | `en-US/listing.json` |
| Screenshot order, headlines, captions, alt text | `design.json` → `slides` |
| Backgrounds, type colors, frames | `design.json` → `themes` |
| Default app palette, light/dark mode, color style | `design.json` → `captureDefaults` |
| Screen sizes and render density | `design.json` → `profiles` |
| Layout, type sizes, framing and crops | `artwork.html` |
| Example expressions, history, graphs and scores | `capture/Main.kt` |
| App icon | `../branding/calcula-icon.svg` |

The screenshots show illustrative sample calculations, not a person's history. The capture entry point is compiled only with `-PstoreCapture`; ordinary Android, iOS and web builds do not include it. It renders production screens and can export their canvas as a lossless PNG, avoiding browser full-page clipping of Compose lazy lists.

## First setup

From `store-listing/`, with Node.js and the project's usual JDK/Android build environment:

```sh
npm ci
npx playwright install chromium
```

## Change only copy, colors or framing

```sh
npm run render
npm run validate
```

This reuses the current raw screenshots. Editing the store descriptions also regenerates `title.txt`, `short-description.txt`, and `full-description.txt` for pasting into Play Console. To regenerate only those text files, run `npm run render -- --text-only`.

## Recapture after app changes

```sh
npm run capture:build
npm run capture
npm run validate
```

To change the actual app's theme as well as the surrounding artwork:

```sh
npm run capture -- --theme dark --app-theme dark --palette TEAL --style VIBRANT
```

Supported `--theme` names come from `design.json`. App palettes and styles use their enum names (`INDIGO`, `TEAL`, `PURPLE`; `TONAL_SPOT`, `NEUTRAL`, `VIBRANT`, `EXPRESSIVE`). Optional `--group phone`, `--group tablet7`, `--group tablet10`, or `--group desktop` limits regeneration. Full renders also produce the store icon and feature graphic. The renderer creates its own local HTTP server and fresh browser context, then closes both.

## Preview and adjust by hand

From the repository root:

```sh
python3 store-listing/serve.py
```

Open `http://127.0.0.1:8787/store-listing/` for the interactive device/theme preview, or `http://127.0.0.1:8787/store-listing/artwork.html?kind=phone&slide=0` for one full-size image. Other kinds are `tablet7`, `tablet10`, `desktop`, `feature`, and `icon`. Add `&theme=dark` or `&theme=paper` to preview another design theme. Slide indices follow that group's order in `design.json`.

The capture UI is at `/store-listing/capture-resources/?scene=calculator&density=2.5&theme=light&palette=INDIGO`. The **Export capture PNG** button exposes the canvas as an image for saving. Other scenes: `graph`, `converter`, `customize`, `programmer`, `statistics`, and `appearance`.

## Outputs and upload order

- `en-US/phone/`: seven 1080 × 1920 captioned images.
- `en-US/tablet7/`: four clean 1080 × 1920 app captures.
- `en-US/tablet10/` and `en-US/desktop/`: four clean 1920 × 1080 app captures each.
- `en-US/graphics/app-icon.png`: 512 × 512 RGBA PNG.
- `en-US/graphics/feature-graphic.png`: opaque 1024 × 500 PNG.
- `en-US/assets.json`: export dimensions, sizes, and provenance.
- `en-US/raw/`: original lossless app captures. Replace these to update artwork without recapturing.

Upload images in filename order and verify the thumbnails before saving. Play Console can reorder parallel uploads. Save the listing draft; submitting for review or publishing is a separate step. An optional YouTube video is omitted unless a real approved video exists. No XR assets are supplied for an app without an XR experience.

Large-screen images intentionally contain only app UI, as recommended by [Google Play's preview asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151). Phone artwork uses a fixed, consistent frame. The feature graphic uses the same brand and real app capture, without store badges or promotional claims.
