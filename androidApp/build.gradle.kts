plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose)
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
        versionCode = 200
        versionName = "3.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
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
    implementation(project(":shared"))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
}
