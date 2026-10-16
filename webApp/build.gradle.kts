@file:OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose)
    alias(libs.plugins.jetbrains.compose)
}

kotlin {
    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "calculator.js"
                sourceMaps = false
            }
        }
        binaries.executable()
    }

    sourceSets {
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.browser)
        }
    }
}

dependencies {
    // Material 3 alpha resolves a newer Compose UI; its bundled Skiko runtime must match.
    val skikoRuntime = variantOf(libs.skiko.wasm.js) { classifier("skiko-runtime") }
    configurations.matching { it.name == "COMPOSE_SKIKO_JS_WASM_RUNTIME" }.configureEach {
        dependencies.addLater(skikoRuntime)
    }
}
