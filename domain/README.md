# :domain

App-wide models and repository contracts for Skyward: locations, a day of sky spans and events,
and the sun and moon right now. Pure Kotlin.

Targets: jvm, Android, iosArm64, iosSimulatorArm64. Tests run with `./gradlew :domain:jvmTest`.

Depends on: Kastro, kotlinx-datetime and kotlinx-coroutines, all exposed as `api` so every consumer can use their types.
