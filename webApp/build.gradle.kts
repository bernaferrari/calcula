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
        // Store captures use the real shared UI with isolated sample data. Normal builds
        // keep their original entry point and never include the capture fixtures.
        if (providers.gradleProperty("storeCapture").isPresent) {
            wasmJsMain {
                kotlin.setSrcDirs(listOf("../store-listing/capture"))
            }
        }
        wasmJsMain.dependencies {
            implementation(project(":shared"))
            implementation(libs.compose.runtime)
            implementation(libs.compose.ui)
            implementation(libs.kotlinx.browser)
            if (providers.gradleProperty("storeCapture").isPresent) {
                implementation(libs.compose.foundation)
                implementation(libs.compose.material3)
                implementation(libs.lifecycle.viewmodel)
                implementation(libs.lifecycle.viewmodel.compose)
                implementation(libs.kotlinx.coroutines.core)
            }
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
