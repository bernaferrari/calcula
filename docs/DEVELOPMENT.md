# Developing Calcula

A Kotlin Multiplatform app: one Compose UI and calculator engine in `shared/`, hosted by `androidApp/` (Android), `iosApp/` (Xcode) and `webApp/` (Wasm in the browser). `androidApp/` is the app on Google Play.

[README](../README.md) · [Privacy](../PRIVACY.md) · [Store listing](../store-listing/README.md)

## Quick start

```bash
git clone https://github.com/bernaferrari/calcula.git
cd calcula
./gradlew :androidApp:installDebug
```

## What you need

| Tool | Version | Notes |
| --- | --- | --- |
| JDK | 21 (JetBrains) | Gradle daemon toolchain in `gradle/gradle-daemon-jvm.properties`, downloaded automatically. Android code targets Java 17. |
| Gradle | 9.8.1 | Use `./gradlew`; the wrapper pins it. |
| Android SDK | API 37, minor 2 | `compileSdk` in `androidApp/build.gradle.kts`. Set `sdk.dir` in `local.properties` or `ANDROID_HOME`. |
| Xcode | current | iOS only, on macOS, with an iOS simulator runtime. |
| Node.js | any LTS | Only for `store-listing/` and `web/`. Gradle provisions its own Node for the Wasm build. |

## Run it

| Target | Command |
| --- | --- |
| Android | `./gradlew :androidApp:installDebug` |
| Web (Wasm) | `./gradlew :webApp:wasmJsBrowserDevelopmentRun` |
| iOS | Open `iosApp/iosApp.xcodeproj`, pick the `iosApp` scheme and a simulator, Run. A real device needs your own signing team. |

## Where things live

| Path | What's there |
| --- | --- |
| `shared/src/commonMain/` | Engine, state, persistence and every Compose screen |
| `shared/src/commonMain/.../ui/graph/` | Graph: `GraphScreen` (layout), `GraphControls` (functions, parameters), `GraphCanvas` (gestures), `GraphDrawing` (plot, thumbnails, export), `GraphKeypadResize` |
| `shared/src/commonTest/`, `androidHostTest/` | Shared tests; JVM-only BigDecimal differential tests |
| `androidApp/` | Android host, Glance widget, and the `storeCapture` build type |
| `iosApp/` | Xcode project and the iOS widget |
| `webApp/` | Wasm browser host |
| `web/` | Separate Next.js landing page (pnpm), not the calculator runtime |
| `store-listing/` | Play listing text, screenshots and icon sources |
| `fastlane/`, `scripts/setup-play-api.sh` | Play listing upload and API setup |

## How the engine computes

The engine is in `shared/src/commonMain/.../domain/math/`; `CalcEngine` is the entry point.

- **Two kinds of value.** A result is either exact or bounded. Exact means a reduced fraction, optionally times π, e or a square or cube root (`SymbolicNumber`). Bounded means a `Decimal` centre with an error radius that the true value is guaranteed to lie within (`DecimalError`). A function must return one or the other.
- **Precision retry.** The parser keeps the expression tree (`Expression`). Evaluation starts at 40 digits and re-runs the same tree at 80 and 160 when cancellation eats the digits. "More digits" re-runs it at up to 4,096.
- **Series.** `exp`, `ln`, the trig functions and their inverses are summed as series, mostly in fixed point on big integers, and each adds its truncation error to the radius (`DecimalLogarithm`, `DecimalTrig`, `DecimalConstants`). π, e, ln 2 and ln 10 are computed on demand past about 170 digits.
- **No `Double`.** Evaluation never goes through `Double`. Only graphing does (`Plot`, `KeyPoints`).
- **Big integers.** `BigInteger` is expect/actual: `java.math.BigInteger` on Android, ionspin bignum on iOS and web, so high precision is slower there.
- **Time limits.** Every evaluation carries a deadline and gives up with `TIME_LIMIT` instead of blocking. Loops over big numbers must call the checkpoint they are given.

`DecimalTranscendentalTest` guards the radius. It checks results against reference values and checks that the 40-digit and 160-digit intervals for seeded random expressions overlap. A result shown as exact is a promise, so keep those tests passing when you touch a series or an error bound.

## Before you push

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:compileKotlinWasmJs :shared:compileKotlinIosSimulatorArm64
```

There is no CI, so these checks are local only. The tests cover evaluation, history delete and undo, preferences, state restore, decimal arithmetic and the error bounds of the series; the differential tests use a fixed seed, so failures reproduce. The report is at `shared/build/reports/tests/testAndroidHostTest/index.html`.

Nothing checks the UI. When you change it, try large fonts, reduced motion, both flick directions, history undo after a new calculation, and graph pan, zoom, trace and export.

## Store screenshots and icon

Screenshot Studio lives in `store-listing/studio/`. It's synced from `~/Documents/screenshot-studio`, so never edit it here.

- The `storeCapture` build type (`./gradlew :androidApp:assembleStoreCapture`, package `com.bernaferrari.calcula.storecapture`) installs beside the real app. `StoreCaptureActivity` seeds fictional calculations and opens each scene by intent.
- `node store-listing/studio/recapture.cjs` builds, installs and captures phone, tablet7, tablet10 and desktop in en-US, de-DE, es-ES and pt-BR, then exports. It runs on the capture emulator `CAPTURE_SERIAL` (default `emulator-5578`), never a personal phone. Narrow it with a locale (`de-DE`), `--profile phone`, `--no-build` or `--no-capture`.
- Export only: `cd store-listing && node render.mjs --locale en-US`. It writes the listing text, runs the studio exporter and checks every asset. Run `npm ci` in `store-listing/` once first.
- Live editor: `node store-listing/studio/server.cjs`.
- Icon: layers in `store-listing/brand/*.svg`, targets in `brand/icon.json` (Android mipmaps, `brand/icon.svg`, `web/app/icon.svg`, the iOS `app-icon-1024.png` and the store icon). Regenerate all of them with `PLAYWRIGHT_MODULE=… node store-listing/studio/icon.cjs`.

## Good to know

- The application id is `com.bernaferrari.calcula`; the Kotlin namespace is still `com.bernaferrari.calculator`. The old Calculator++ listing is a different app, so nothing carries over from it.
- Release signing loads the gitignored `upload-keystore.properties`, or an external file selected with `CALCULA_SIGNING_PROPERTIES`. It contains `storeFile`, `storePassword`, `keyAlias`, and `keyPassword`. The local configuration reuses Change Detection's keystore and credentials; never commit the properties file or keystore. Build with `./gradlew :androidApp:bundleRelease`. Without a signing configuration, release artifacts are unsigned.
- The Gradle root project is still named `CalculatorPlusPlus`.
- Run capture and the `store-listing/verify-programmer-*.py` checks only on a disposable emulator. They change emulator settings, and leave the normal app's data alone.
- `app/`, `old-app/` and the `*reference-ui/` folders are untracked local leftovers, not part of the build.
