# :dial

Turns a day of sky spans and events into a renderer-agnostic `DialModel` (arc angles, radii,
colors with luminance, markers, hour labels, hand and moon phase) and hit-tests taps on it. No
Compose, so the iPhone widget can use it.

Targets: jvm, Android, iosArm64, iosSimulatorArm64. Tests run with `./gradlew :dial:jvmTest`.

Depends on: `:domain` (exposed as `api`).
