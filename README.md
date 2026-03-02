# Calculator++ (Fork)

Modern fork of [serso/android-calculatorpp](https://github.com/serso/android-calculatorpp), rebuilt around Kotlin Multiplatform + Compose.

## What this fork focuses on

- Shared app logic/UI across Android and iOS (`shared` module).
- Modern calculator UX (tabs, RPN, paper tape, programmer tools).
- Compose Material 3 screens for history, graphing, converter, variables, and functions.
- Android widgets and platform integrations in `androidApp`.

## Modules

- `androidApp`: Android app and widgets.
- `shared`: KMP core logic + Compose UI.
- `iosApp`: iOS host app.
- `jscl`: math engine/parser.
- `dragbutton`: gesture/button interaction module.

## Quick start

Requirements:

- JDK 17+
- Android SDK (`compileSdk 36`, `minSdk 26`)
- Android Studio (latest recommended)
- Xcode 15+ (for iOS)

Build Android debug:

```bash
./gradlew :androidApp:assembleDebug
```

Install on emulator/device:

```bash
./gradlew :androidApp:installDebug
```

Compile check:

```bash
./gradlew :androidApp:compileDebugKotlin
```

iOS:

1. Open `iosApp/iosApp.xcodeproj`
2. Run the `iosApp` target in Xcode

## Git remotes

- `origin`: your fork (`bernaferrari/android-calculatorpp`)
- `upstream`: original repo (`serso/android-calculatorpp`)
