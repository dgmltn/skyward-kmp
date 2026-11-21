# :data

Implementations of the `:domain` contracts: the Kastro-backed sky repository (phase, light and
moon spans ported from kastro-demo), platform location sources, and the last-location store
(DataStore on Android, App Group `NSUserDefaults` on iOS so the widget can read it).

Targets: jvm, Android, iosArm64, iosSimulatorArm64. Tests run with `./gradlew :data:jvmTest`.

Depends on: `:domain`.

Kastro publishes no Android variant. Android compiles against its jvm variant, which Kotlin
resolves directly, so no intermediate source set is needed.
