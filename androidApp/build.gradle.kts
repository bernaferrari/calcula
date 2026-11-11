import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
}

val releaseSigningFile = providers.environmentVariable("CALCULA_SIGNING_PROPERTIES")
    .orNull?.let { rootProject.file(it) } ?: rootProject.file("upload-keystore.properties")
val releaseSigning = Properties().apply {
    if (releaseSigningFile.isFile) releaseSigningFile.inputStream().use(::load)
}
val releaseKeystore = releaseSigning.getProperty("storeFile")?.let { rootProject.file(it) }
if (releaseSigningFile.isFile) {
    check(releaseKeystore?.isFile == true &&
        listOf("keyAlias", "keyPassword", "storePassword").all {
            !releaseSigning.getProperty(it).isNullOrBlank()
        }) { "Release signing configuration is incomplete: ${releaseSigningFile.name}" }
}

android {
    // Calcula ships under its own id. The old Calculator++ listing (org.solovyev.android.calculator)
    // is a different app as far as Android and Play are concerned: no data or updates carry over.
    namespace = "com.bernaferrari.calculator"
    compileSdk {
        version = release(37) { minorApiLevel = 2 }
    }

    defaultConfig {
        applicationId = "com.bernaferrari.calcula"
        minSdk = 26
        targetSdk = 37
        versionCode = 6
        versionName = "1.0"
    }

    signingConfigs {
        if (releaseSigningFile.isFile) {
            create("release") {
                storeFile = releaseKeystore
                storePassword = releaseSigning.getProperty("storePassword")
                keyAlias = releaseSigning.getProperty("keyAlias")
                keyPassword = releaseSigning.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        create("storeCapture") {
            initWith(getByName("debug"))
            applicationIdSuffix = ".storecapture"
            matchingFallbacks += "debug"
        }
        release {
            signingConfig = signingConfigs.findByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures { compose = true }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    "storeCaptureImplementation"(libs.compose.foundation)
    "storeCaptureImplementation"(libs.compose.material3)
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
}
