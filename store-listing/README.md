# Calcula · Screenshot artwork

The README and Play Store use the same image set: current production Android screens captured in the resizable emulator, with the real status bar and gesture navigation pill. Phone, 7-inch tablet, and 10-inch tablet sets each have five connected compositions: flick → graph → convert → personalize → more room. Desktop-sized images show the app UI without framing.

## Edit the artwork

The artwork runs on [Screenshot Studio](studio/), the shared template used by all of these apps (master copy in `~/Documents/screenshot-studio`; never edit `studio/` here, sync it from there). The studio provides the editor, card format, headline markup, frames, and exporter checks; Calcula provides:

| Change | Source |
| --- | --- |
| Order, backgrounds, and real capture sources | `gallery.json` (tablets inherit phone layout) |
| Headlines, supporting words, and device notes | `<locale>/copy.json` |
| Export canvases, capture sizes, and device frames | `profiles.json` |
| Composition, type, and framing | `theme.css` |
| Panorama wave, frame scaling, brand, notes, and the enlarged sin key | `theme.js` |
| App scene palettes and capture sizes for the emulator | `design.json` → `slides`, `captureDefaults`, `profiles` |
| Sample calculations and scene setup | `../androidApp/src/storeCapture/kotlin/com/bernaferrari/calculator/StoreCaptureActivity.kt` |
| Capture settings and device sizes | `capture-emulator.py` and `design.json` → `profiles` |
| Store descriptions | `en-US/listing.json` |

The isolated `storeCapture` build uses the package `com.bernaferrari.calcula.storecapture`. Its sample data and exported capture activity are excluded from normal debug and release builds. It does not touch the installed Calcula app’s data. Every scene renders the production screen; no UI or system bars are painted into the images.

The five cards share a heading line and type system, with a flat color per card and different screen arrangements. Only the first card carries the Calcula name. One sine wave runs through the whole carousel: the area under it is a shared cream ground, drawn in panorama coordinates (five canvases side by side) with each card showing its own window, so the curve meets itself exactly at every seam. Its center, amplitude, wavelength, and phase are set per device in `theme.js`. Devices take about two thirds of the card width and may run off the bottom edge. Product views come from the raw captures named in each card's `devices`. The enlarged sin key on the first card is vector type in the app’s font and the capture’s sampled key colors (`key` on the card), so it stays sharp at any size. Tablet variants inherit the approved copy, colors, and order, with their own real device views and compositions.

Screen frames follow the source image’s intrinsic aspect ratio. Phone captures use the Galaxy S25’s 19.5:9 ratio, tablets 16:10 (or 10:16 in portrait). Frames are solid, upright bezels; bezel width and corner radius scale with frame width, with smaller ratios for landscape tablets. The artwork page checks that every frame keeps its status bar and sides inside the canvas, that at least 70% of a frame running off the bottom stays visible, and that images retain the captured aspect ratio.

To change only the artwork, render again. Recapture when the app UI, sample data, or app palette changes. The seven raw phone scenes are retained even though the campaign has five exports. The 7-inch campaign uses a portrait canvas and 600 dp-wide tablet UI; the 10-inch campaign uses a landscape canvas and an expanded tablet UI. Alternate orientations are captured through `captureVariants` for each set’s final card and paired views.

## Recapture everything

Start a disposable **Resizable (Experimental)** Android emulator with gesture navigation. Build the current source, then pass its exact emulator serial:

```sh
./gradlew :androidApp:assembleStoreCapture
cd store-listing
python3 capture-emulator.py --serial emulator-5560
```

The script installs the fresh APK, switches between phone, tablet, and desktop display modes, and captures every scene in `design.json` with `adb exec-out screencap`. It selects the emulator’s primary display explicitly. Android demo mode fixes the real System UI clock at 9:41, hides notifications, and shows full connectivity and battery icons. It resets demo mode around each resize so Android does not duplicate connectivity icons. Capture provenance, source commit, APK hash, dimensions, and image hashes are written to `en-US/capture-manifest.json`.

Raw phone captures are 1080 × 2340 at 400 dpi, placed in 1080 × 1920 campaign canvases. The 7-inch portrait captures are 1200 × 1920 at 320 dpi, 10-inch landscape captures are 1920 × 1200 at 240 dpi, and desktop-sized captures are 1920 × 1080 at 200 dpi. `captureWidth` and `captureHeight` can differ from the export canvas dimensions. `--group phone`, `--group tablet7`, `--group tablet10`, or `--group desktop` limits a diagnostic run; a full run refreshes the complete manifest.

## Preview the sequence

Run `node studio/server.cjs 8790` from this directory and open `http://127.0.0.1:8790/store-listing/studio/`. The editor shows every device gallery live from the source files, joined edge to edge or spaced like Play, and can switch to the exported images. Refresh after editing.

## Render and validate

With Node.js installed, run from this directory:

```sh
npm ci
npx playwright install chromium
npm run render
npm run validate
```

`npm run render` writes the listing text files, runs the studio exporter, and records `en-US/assets.json`. It creates five phone, five 7-inch, and five 10-inch campaign images from the real emulator captures, four unframed desktop screenshots, the feature graphic, and the 32-bit app icon, using local app fonts. The exporter fails if a capture has the wrong ratio, a headline overflows, copy, the brand, or a note leaves the canvas, copy overlaps an app view, or any request fails; `theme.js` also fails if a frame leaves the sides or runs more than 30% off the bottom. `npm run validate` checks dimensions, file sizes, icon and feature-graphic transparency, and copy limits without rendering. `--group <kind>` renders one gallery.

## Check Programmer interactions

With the current capture APK installed in the disposable emulator:

```sh
python3 verify-programmer-scroll.py --serial emulator-5560
python3 verify-programmer-scroll.py --serial emulator-5560 --landscape
python3 verify-programmer-bits.py --serial emulator-5560
```

The scroll check creates eight calculations, reaches older history, reuses a result, and returns to typing in both orientations. The bits check covers the compact inline disclosure and its 48 dp target, the collapsed default, long-press collapse, saved visibility, long binary values, and signed/unsigned arithmetic at the top bit. The normal app’s data is untouched.

## Outputs

- `en-US/phone/`: five connected 1080 × 1920 campaign images.
- `en-US/tablet7/`: five 1200 × 1920 portrait campaign images.
- `en-US/tablet10/`: five 1920 × 1200 landscape campaign images.
- `en-US/desktop/`: four 1920 × 1080 app screenshots.
- `en-US/raw/`: 25 current lossless Android captures, including tablet orientation variants and real System UI.
- `en-US/graphics/`: the app icon and feature graphic.
- `en-US/assets.json`: export dimensions, sizes, and provenance.
- `en-US/contact-sheet-joined.jpg`: the five phone cards edge to edge.
- `en-US/tablet7/contact-sheet-joined.jpg` and `en-US/tablet10/contact-sheet-joined.jpg`: the tablet sequences edge to edge; upload the five files prefixed with the device name, not these overview images. The editor shows store-style gaps.

Nothing in this workflow uploads or publishes assets. `upload-status.json` records the earlier saved draft separately from the newly recaptured local files. The visual direction draws on [Material’s shape guidance](https://m3.material.io/styles/shape) and [Expressive design research](https://design.google/library/expressive-material-design-google-research?pubDate=20250521). See [Google Play’s preview asset guidance](https://support.google.com/googleplay/android-developer/answer/9866151?hl=en).

## Localized screenshots (Studio v4.3)

`gallery.json` is shared layout; `<locale>/copy.json` contains only words. Add or refresh a language with `node store-listing/studio/locales.cjs --init de-DE` from the repository root (existing words are preserved). `node store-listing/studio/locales.cjs` reports copy, capture and layout coverage. Other supported languages are scaffolded as drafts with empty fields.

Capture English first, then pass `--locale de-DE` to the capture script. It sets only the isolated capture app's language, verifies Android's language setting, writes images and `*.nodes.json` UI trees under `<locale>/raw/`, and resets the app language afterwards. Use only the dedicated `StudioCapture` emulator (`emulator-5566`); no physical phone or another session's emulator. Sample names and other fictional content remain English.

Open the editor with `node store-listing/studio/server.cjs 8790`. Use **Cards**, **Languages**, and **Copy**; Copy saves directly to the selected language's file (Cmd/Ctrl+Enter also saves). Pick callouts on English captures; matching localized layout nodes move them. Headline copy shrinks to fit down to 72%, with script-specific font fallbacks.

Render one completed language with `node store-listing/studio/export.cjs --locale de-DE --strict` (set `PLAYWRIGHT_MODULE` if needed). `--kind phone` limits the gallery. Missing words or images borrow English and are flagged. A callout that cannot follow its UI element is flagged too; `--strict` rejects those three conditions. Draft locales should fail strict checks until translated and captured. README images continue to use English assets. Nothing is uploaded or published by this workflow.

### Multiple projects

The server also opens a shared workspace at `http://127.0.0.1:8790/studio/`. Use **Add project** to connect another app or screenshot folder, or **Find projects** to discover listings in a parent folder. The sidebar switches between projects; each overview shows its languages, captures, layout maps and exports. Plain folders open as image libraries. Registrations are shared in `~/.screenshot-studio/projects.json`; removing a project leaves its files intact. Unsaved copy survives project switches and reloads in the same browser tab. Save before ending the browser session. Complete asset counts describe file presence; run strict export and review artwork before release.

Project and view navigation now updates in place, preserving the sidebar and recently loaded previews. Browser Back/Forward works between views, and pending saves stay scoped to the app where they started.

The editor includes searchable language coverage, copy-completion filters and expandable previews. Copy supports search, missing-field filtering, card navigation and field status; English reference editing uses one column. On narrow screens, Preview opens the saved artwork in a dialog. The gallery includes Previous/Next scroll controls.

Studio v4 uses React and official shadcn/ui components throughout, including the expandable Sidebar and mobile Sheet drawer. The sidebar footer offers Light, Dark and System appearance; Cmd/Ctrl+K searches projects. Interface source lives in the master repository’s `ui/src/`; run `npm ci` and `npm run build` there before syncing. This app receives the compiled local bundle and still starts with the same Node server command.

Screenshot images now open a shared shadcn gallery dialog with thumbnails, wrapping arrows, keyboard/swipe navigation, Fit and 1:1 zoom, panning and explicit original/download actions. Live cards preserve their language and device. Escape returns focus to the clicked image; browsing does not save or change copy.

Studio v4.3 refines the project overview, language screens and shared controls with a neutral shadcn theme, keyboard-accessible filter tabs, compact project cards and responsive headers.

The v4.3 project page separates Overview, Galleries and Languages, links to unfinished work, and keeps language counts and actions visible on phones. Narrow workbenches use a device-gallery select, and translation references stack above inputs when space is limited.
