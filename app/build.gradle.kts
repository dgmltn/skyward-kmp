plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(providers.gradleProperty("skyward.jvmTarget").get().toInt())

    android {
        namespace = "app.skyward.app"
        compileSdk = providers.gradleProperty("skyward.compileSdk").get().toInt()
        minSdk = providers.gradleProperty("skyward.minSdk").get().toInt()
    }
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SkywardKit"
            isStatic = true
        }
    }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain)
            implementation(projects.sky)
            implementation(projects.data)
            implementation(projects.design)
            implementation(libs.compose.ui.tooling.preview)
            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            implementation(libs.kotlinx.datetime)
        }
        androidMain.dependencies { implementation(libs.datastore.preferences) }
    }
}

dependencies { "androidRuntimeClasspath"(libs.compose.ui.tooling) }
