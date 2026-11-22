plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

kotlin { jvmToolchain(providers.gradleProperty("skyward.jvmTarget").get().toInt()) }

android {
    namespace = "app.skyward.android"
    compileSdk = providers.gradleProperty("skyward.compileSdk").get().toInt()

    defaultConfig {
        applicationId = providers.gradleProperty("skyward.applicationId").get()
        minSdk = providers.gradleProperty("skyward.minSdk").get().toInt()
        targetSdk = providers.gradleProperty("skyward.targetSdk").get().toInt()
        versionCode = providers.gradleProperty("skyward.versionCode").get().toInt()
        versionName = providers.gradleProperty("skyward.versionName").get()
    }

    // Release signing comes from the environment, which only the release workflow sets. Without
    // it, release builds come out unsigned.
    val keystore = providers.environmentVariable("ANDROID_KEYSTORE_FILE").orNull
    if (keystore != null) {
        signingConfigs {
            create("release") {
                storeFile = file(keystore)
                storePassword = providers.environmentVariable("ANDROID_KEYSTORE_PASSWORD").get()
                keyAlias = providers.environmentVariable("ANDROID_KEY_ALIAS").get()
                keyPassword = providers.environmentVariable("ANDROID_KEY_PASSWORD").get()
            }
        }
        buildTypes { getByName("release") { signingConfig = signingConfigs.getByName("release") } }
    }
}

dependencies {
    implementation(projects.app)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
}
