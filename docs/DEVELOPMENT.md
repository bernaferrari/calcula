# Developing Calcula


The running app is Kotlin Multiplatform: `shared/` contains the domain logic and Compose UI,
`androidApp/` is the Android host, `iosApp/` is the Xcode host, and `webApp/` is the Wasm host.
The separate `web/` directory is a Next.js project, not the shared calculator runtime.

## Prerequisites

- Use the checked-in Gradle wrapper. Its daemon toolchain is JetBrains JDK 21; Android bytecode targets Java 17.
- Install the Android SDK matching `compileSdk` in `androidApp/build.gradle.kts` (currently API 37, minor API 2).
  Point `sdk.dir` in your untracked `local.properties` at that SDK, or configure `ANDROID_HOME`.
- iOS builds require macOS, Xcode, its command-line tools, and an installed iOS simulator runtime.
- Gradle provisions the Node runtime for the Wasm build. The first build needs network access for dependencies.

## Run

```bash
# Build Android; installDebug also installs onto a connected emulator/device.
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug

# Run the shared Compose calculator in a browser.
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

For iOS, open `iosApp/iosApp.xcodeproj`, select the `iosApp` scheme and a simulator, then Run.
Device installation additionally requires your own signing team in Xcode.

## Validate changes

```bash
# Common tests plus JVM-only differential checks against java.math.BigDecimal.
./gradlew :shared:testAndroidHostTest

# Verify shared code compiles for the other platforms.
./gradlew :shared:compileKotlinWasmJs :shared:compileKotlinIosSimulatorArm64
```

`shared/build/reports/tests/testAndroidHostTest/index.html` contains the test report.
The suite covers expression evaluation, history delete/undo, persisted preferences, calculator
state restoration, and decimal arithmetic. Decimal differential tests use a fixed random seed
so failures are reproducible. UI checks should include large fonts, reduced motion, both flick
directions, history undo after a new calculation, and graph pan/zoom/trace/export.

Graph UI code is grouped by responsibility: `GraphScreen` coordinates layout, `GraphControls`
edits functions and parameters, `GraphCanvas` handles plot gestures, `GraphDrawing` renders the
plot and shared thumbnails/export primitives, and `GraphKeypadResize` handles resizing.


Localized store artwork is maintained in [Screenshot Studio](../store-listing/README.md): shared layout in `store-listing/gallery.json`, words in `store-listing/<locale>/copy.json`, and native captures plus UI layout files in `store-listing/<locale>/raw/`. The editor compares languages and saves copy; strict export rejects English fallbacks and unresolved callouts. English remains the README image source.
