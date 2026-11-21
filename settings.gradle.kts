rootProject.name = "skyward"

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        google()
        mavenCentral()
    }
}

include(
    ":domain",
    ":data",
    ":dial",
    ":design",
    ":sky",
    ":app",
    ":widget",
    ":app-android",
)
