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
}

dependencies {
    implementation(projects.app)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core)
}
