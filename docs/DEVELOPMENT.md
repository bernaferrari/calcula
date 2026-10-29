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

## Before you push

```bash
./gradlew :shared:testAndroidHostTest
./gradlew :shared:compileKotlinWasmJs :shared:compileKotlinIosSimulatorArm64
```

There is no CI, so these checks are local only. The tests cover evaluation, history delete and undo, preferences, state restore and decimal arithmetic; the differential tests use a fixed seed, so failures reproduce. The report is at `shared/build/reports/tests/testAndroidHostTest/index.html`.

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
- Release signing is not in Gradle. `upload-keystore.properties` is gitignored; never commit it or any keystore.
- The Gradle root project is still named `CalculatorPlusPlus`.
- Run capture and the `store-listing/verify-programmer-*.py` checks only on a disposable emulator. They change emulator settings, and leave the normal app's data alone.
- `app/`, `old-app/` and the `*reference-ui/` folders are untracked local leftovers, not part of the build.
