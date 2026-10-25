![Calculator++ — A little more than a calculator.](assets/social.png)

# Calculator++

An Android calculator built with Kotlin, Jetpack Compose, and Material 3. Everyday arithmetic, scientific functions, calculation history, and unit conversions in one app.

- Scientific functions, including trigonometry and inverse functions
- Editable expressions and calculation history
- Unit conversions
- Light, dark, dynamic color, and high contrast themes

Requires Android 9 (API 28) or newer.

## Build

Open the project in Android Studio with the Android SDK required by `app/build.gradle.kts`. Gradle uses JDK 21 through its configured daemon toolchain.

```sh
./gradlew :app:assembleDebug
```

## Branding and GitHub sharing

The app includes adaptive launcher icons, a monochrome icon for Android themed icons, and raster exports for all standard launcher densities. Editable sources and regeneration instructions are in [assets/BRANDING.md](assets/BRANDING.md).

The [social preview image](assets/social.png) is ready to upload in the GitHub repository’s **Settings → General → Social preview → Edit → Upload an image**. GitHub uses that repository setting for shared repository links; committing the PNG or displaying it in this README does not configure the link preview.

## License

[Apache License 2.0](LICENSE).
