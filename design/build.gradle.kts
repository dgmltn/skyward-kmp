plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(providers.gradleProperty("skyward.jvmTarget").get().toInt())

    android {
        namespace = "app.skyward.design"
        compileSdk = providers.gradleProperty("skyward.compileSdk").get().toInt()
        minSdk = providers.gradleProperty("skyward.minSdk").get().toInt()
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            api(libs.compose.runtime)
            api(libs.compose.foundation)
            api(libs.compose.ui)
            api(libs.compose.material3)
            implementation(libs.compose.ui.tooling.preview)
        }
    }
}

dependencies { "androidRuntimeClasspath"(libs.compose.ui.tooling) }
