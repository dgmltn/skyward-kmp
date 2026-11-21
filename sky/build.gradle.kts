plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

kotlin {
    jvmToolchain(providers.gradleProperty("skyward.jvmTarget").get().toInt())

    android {
        namespace = "app.skyward.sky"
        compileSdk = providers.gradleProperty("skyward.compileSdk").get().toInt()
        minSdk = providers.gradleProperty("skyward.minSdk").get().toInt()
        withHostTest {}
    }
    iosArm64()
    iosSimulatorArm64()

    compilerOptions { freeCompilerArgs.add("-Xexpect-actual-classes") }

    sourceSets {
        commonMain.dependencies {
            implementation(projects.domain)
            implementation(projects.dial)
            implementation(projects.design)
            implementation(libs.compose.ui.tooling.preview)
            implementation(libs.lifecycle.viewmodel)
            implementation(libs.lifecycle.runtime.compose)
            implementation(libs.kotlinx.collections.immutable)
            implementation(libs.kermit)
        }
        androidMain.dependencies { implementation(libs.androidx.activity.compose) }
        commonTest.dependencies { implementation(kotlin("test")) }
        getByName("androidHostTest").dependencies {
            implementation(kotlin("test"))
            implementation(libs.kotlinx.coroutines.test)
            implementation(projects.data)
        }
    }
}

dependencies { "androidRuntimeClasspath"(libs.compose.ui.tooling) }
