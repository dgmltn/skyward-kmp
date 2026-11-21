plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
}

kotlin {
    jvmToolchain(providers.gradleProperty("skyward.jvmTarget").get().toInt())

    jvm()
    android {
        namespace = "app.skyward.dial"
        compileSdk = providers.gradleProperty("skyward.compileSdk").get().toInt()
        minSdk = providers.gradleProperty("skyward.minSdk").get().toInt()
    }
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies { api(projects.domain) }
        commonTest.dependencies { implementation(kotlin("test")) }
        jvmTest.dependencies { implementation(projects.data) }
    }
}
