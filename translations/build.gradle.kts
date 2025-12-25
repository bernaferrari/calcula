plugins {
    application
}

application {
    mainClass.set("org.solovyev.android.translations.Android")
}

dependencies {
    implementation(libs.commons.cli)
    implementation(libs.httpclient)
    implementation(libs.json)
    implementation(libs.simple.xml)
}
