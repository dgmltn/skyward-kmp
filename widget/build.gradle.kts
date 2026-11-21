plugins { alias(libs.plugins.kotlin.multiplatform) }

kotlin {
    jvmToolchain(providers.gradleProperty("skyward.jvmTarget").get().toInt())

    jvm()
    listOf(iosArm64(), iosSimulatorArm64()).forEach { target ->
        target.binaries.framework {
            baseName = "SkywardWidgetKit"
            isStatic = true
            export(projects.dial)
            export(projects.domain)
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(projects.dial)
            api(projects.domain)
            implementation(projects.data)
            implementation(libs.kermit)
        }
        commonTest.dependencies { implementation(kotlin("test")) }
    }
}
