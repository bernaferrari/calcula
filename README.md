<p align="center">
  <img src="branding/calcula-icon.svg" width="88" alt="Calcula icon">
</p>

<h1 align="center">Calcula</h1>

<p align="center"><strong>Flick a key. Get a function.</strong></p>

<p align="center">A calculator for Android, iOS, and the web. Keep the keypad you know, with scientific functions a flick away, answers that update as you type, and a history you can pick up again.</p>

<p align="center">
  <img src="docs/demo.webp" width="270" alt="Flick the 7 up for sin, the 2 up for x², the 2 down for √">
  &nbsp;
  <img src="store-listing/en-US/phone/phone-01-every-key.jpg" width="270" alt="A little flick: the calculator with scientific functions named on its keys and an enlarged sin key">
</p>

## Everything under your thumb

<table>
  <tr>
    <td width="33%"><img src="store-listing/en-US/phone/phone-02-graph.jpg" alt="Watch it take shape: plotting functions and an equation with a parameter slider"></td>
    <td width="33%"><img src="store-listing/en-US/phone/phone-03-convert.jpg" alt="From °C to °F: the unit converter showing 22 Celsius as 71.6 Fahrenheit"></td>
    <td width="33%"><img src="store-listing/en-US/phone/phone-04-programmer.jpg" alt="Hex, sorted: Programmer mode showing C0 FFEE in hexadecimal and decimal"></td>
  </tr>
  <tr>
    <td align="center"><strong>Graph</strong><br>Plot functions and equations. Drag a parameter and watch it move.</td>
    <td align="center"><strong>Convert</strong><br>Temperature, mass, area, volume and more, as you type.</td>
    <td align="center"><strong>Programmer</strong><br>Hex, decimal, octal and binary, with bits and shifts.</td>
  </tr>
</table>

- **Functions on the keys.** Flick up for the function written above a key, down for its partner. Trigonometry, logs, roots, powers, π and more, with no second screen of buttons.
- **Exact answers.** Decimal arithmetic keeps `0.1 + 0.2` at `0.3`. Percentages work the way you expect: `200 + 10%` gives `220`. Simple fractions appear beside decimal results.
- **Room to correct yourself.** Move the cursor, select part of an expression, wrap it in a function, or undo. Past calculations can be reused, edited, and given a note.

## Make it yours

<table>
  <tr>
    <td width="50%"><img src="store-listing/en-US/phone/phone-06-customize.jpg" alt="Yours, down to the keys: custom key shortcuts above, appearance settings below"></td>
    <td width="50%"><img src="store-listing/en-US/phone/phone-05-statistics.jpg" alt="Numbers, summed up: mean, median, sum, count and spread for a list of values"></td>
  </tr>
</table>

Choose light or dark, your wallpaper's colors or a color family, and a style you can preview before applying. Keep the function labels, switch to small dots, or hide the hints, and drag the bar above the keys to resize them. When you want more, assign your own key shortcuts, save named memories, define functions such as `tip(x) = x × 0.18`, or switch to reverse Polish notation. Statistics summarizes a list of values in one glance.

## On a bigger screen

History gets its own pane on tablets and foldables, keeping previous calculations in view beside your current work.

<p align="center">
  <img src="store-listing/en-US/tablet10/tablet10-01-every-key.jpg" width="49%" alt="Calcula on a landscape tablet, history beside the calculator">
  <img src="store-listing/en-US/tablet10/tablet10-02-graph.jpg" width="49%" alt="Graphing on a landscape tablet">
</p>

## A few gestures

- **Flick up or down** on a key for its scientific functions. Hold it to reveal both.
- **Tap the answer preview** to accept it, just like pressing equals.
- **Double-tap the expression** to select, then apply a function or parentheses to that selection.
- **Flick backspace up or down** to undo or redo.

## Private by design

No ads, accounts, or tracking. Calculations run on your device. Read the [Privacy Policy](PRIVACY.md) for local storage, optional sharing, backups, and deletion controls.

Calcula grows out of [Calculator++](https://github.com/serso/android-calculatorpp), Sergey Solovyev's swipe-key calculator. That idea lives on in a new implementation built with Kotlin Multiplatform and Compose.

## Build and develop

The running app is Kotlin Multiplatform: `shared/` contains the domain logic and Compose UI,
`androidApp/` is the Android host, `iosApp/` is the Xcode host, and `webApp/` is the Wasm host.
The separate `web/` directory is a Next.js project, not the shared calculator runtime.

### Prerequisites

- Use the checked-in Gradle wrapper. Its daemon toolchain is JetBrains JDK 21; Android bytecode targets Java 17.
- Install the Android SDK matching `compileSdk` in `androidApp/build.gradle.kts` (currently API 37, minor API 2).
  Point `sdk.dir` in your untracked `local.properties` at that SDK, or configure `ANDROID_HOME`.
- iOS builds require macOS, Xcode, its command-line tools, and an installed iOS simulator runtime.
- Gradle provisions the Node runtime for the Wasm build. The first build needs network access for dependencies.

### Run

```bash
# Build Android; installDebug also installs onto a connected emulator/device.
./gradlew :androidApp:assembleDebug
./gradlew :androidApp:installDebug

# Run the shared Compose calculator in a browser.
./gradlew :webApp:wasmJsBrowserDevelopmentRun
```

For iOS, open `iosApp/iosApp.xcodeproj`, select the `iosApp` scheme and a simulator, then Run.
Device installation additionally requires your own signing team in Xcode.

### Validate changes

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


Localized store artwork is maintained in [Screenshot Studio](store-listing/README.md): shared layout in `store-listing/gallery.json`, words in `store-listing/<locale>/copy.json`, and native captures plus UI layout files in `store-listing/<locale>/raw/`. The editor compares languages and saves copy; strict export rejects English fallbacks and unresolved callouts. English remains the README image source.
