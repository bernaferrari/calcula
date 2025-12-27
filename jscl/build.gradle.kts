plugins {
    `java-library`
    kotlin("jvm")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    api(libs.bignum)
    implementation(libs.kotlinx.atomicfu)

    testImplementation(libs.junit)
    testImplementation(libs.opencsv)
}

kotlin {
    jvmToolchain(17)
}
