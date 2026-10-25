# Calcula icon assets

The approved design is a white rounded C with two dots on a lavender tile and a purple background. `icon-concepts/calculator-icon-v3.png` is the image-generated reference. Production icons recreate it as flat vector geometry for clean edges at all sizes.

## Android

The manifest already references `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`. Both adaptive resources use:

- `app/src/main/res/drawable/ic_launcher_background.xml`: opaque purple background.
- `app/src/main/res/drawable/ic_launcher_foreground.xml`: lavender tile, white C and dots, transparent outside the tile.
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`: C and dots for Android themed icons.

All layers use a 108 × 108 dp viewport. The tile stays within the central safe region; the extra 18 dp per side accommodates launcher masking and motion. Legacy square/round PNGs use the normal 72 dp viewport, exported at mdpi through xxxhdpi. Themed icons omit the tile so the single-color silhouette remains recognizable.

`android/` contains reusable foreground/background SVGs and 432 × 432 PNG layers, a transparent monochrome PNG, and `launcher-preview.png` with circular, rounded, square, and themed examples. Android consumes the vector resources directly.

`logo.svg` and `logo.png` are full-bleed web/logo exports. `app/src/main/ic_launcher-web.png` is the opaque 512 × 512 store export. These retain the reference image’s generous spacing.

## Web favicons

`favicons/` includes SVG, a multi-resolution ICO (16/32/48), PNGs (16/32/48/192/512), a 180 px Apple touch icon, and a web manifest. There is no website in this checkout; copy this directory to your website and add these tags, adjusting the paths if needed:

```html
<link rel="icon" href="/favicons/favicon.ico" sizes="any">
<link rel="icon" href="/favicons/favicon.svg" type="image/svg+xml">
<link rel="apple-touch-icon" href="/favicons/apple-touch-icon.png">
<link rel="manifest" href="/favicons/site.webmanifest">
<meta name="theme-color" content="#4F40EE">
```

## Regenerate

With Python 3 and Pillow installed, run from the repository root:

```sh
python3 scripts/generate_brand_assets.py
```

The script shares geometry and colors across Android vectors, SVGs, and raster exports. It intentionally overwrites production icon assets. It does not overwrite the social card or the image-generated concepts.

## GitHub preview

The intended repository is [bernaferrari/calcula](https://github.com/bernaferrari/calcula). `social.png` is the image-generated 1280 × 640 share card using the approved purple C-and-dots identity. `social-source.png` preserves the original generation. The README displays the exported card. Upload the finished card under the repository’s **Settings → General → Social preview**; committing an image does not activate repository sharing previews.

References: [Android adaptive icon requirements](https://developer.android.com/develop/ui/compose/system/icon_design_adaptive) and [GitHub social preview settings](https://docs.github.com/en/repositories/managing-your-repositorys-settings-and-features/customizing-your-repository/customizing-your-repositorys-social-media-preview).
