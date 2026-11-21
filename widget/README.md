# :widget

The Swift-facing API for the iPhone widget: given the saved location, returns dial models for a
timeline of moments. Builds the static iOS framework `SkywardWidgetKit`, linked only into the
widget extension, and exports `:dial` and `:domain` so Swift sees the dial model types. No
Compose, to stay inside the widget extension's memory limit.

Targets: jvm, iosArm64, iosSimulatorArm64. Tests run with `./gradlew :widget:jvmTest`.

Depends on: `:data`, `:dial`, `:domain`.
