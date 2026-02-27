plugins {
    alias(libs.plugins.android.application)

    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
}

android {
    namespace = "org.solovyev.android.calculator"
    compileSdk = 36

    defaultConfig {
        applicationId = "org.solovyev.android.calculator"
        minSdk = 26
        targetSdk = 36
        versionCode = 163
        versionName = "2.3.5"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard.cfg")
        }
        debug {
            isMinifyEnabled = false
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/LICENSE.txt"
            excludes += "META-INF/NOTICE.txt"
        }
    }

    sourceSets {
        getByName("main").assets.srcDir("build/generated/composeSharedAssets")
    }
}

val syncSharedComposeResources by tasks.registering(Sync::class) {
    val sharedPreparedResources = project(":shared")
        .layout
        .buildDirectory
        .dir("generated/compose/resourceGenerator/preparedResources/commonMain/composeResources")

    dependsOn(":shared:prepareComposeResourcesTaskForCommonMain")

    from(sharedPreparedResources)
    into(layout.buildDirectory.dir("generated/composeSharedAssets/composeResources/org.solovyev.android.calculator.ui"))
}

tasks.matching { it.name.startsWith("merge") && it.name.endsWith("Assets") }.configureEach {
    dependsOn(syncSharedComposeResources)
}

dependencies {
    // Shared module
    implementation(project(":shared"))

    // Compose
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.material3)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)

    // Android
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.core.ktx)

    // Android Services
    implementation(libs.billing.ktx)
    implementation(libs.play.services.ads)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.analytics)

    // Widgets
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Koin
    implementation(libs.koin.android)
    implementation(libs.koin.compose)
}
