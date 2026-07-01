plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "id.andini.isyarat"
    compileSdk = 36

    defaultConfig {
        applicationId = "id.andini.isyarat"
        minSdk = 23
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}