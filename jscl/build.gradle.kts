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

    testImplementation(libs.junit)
    testImplementation(libs.opencsv)
}

kotlin {
    jvmToolchain(17)
}