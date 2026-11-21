# :app

The shared app module the platform entries host: the root composable that applies the theme, and
the DI assembly. Builds the static iOS framework `SkywardKit` that the iPhone app links.

Targets: Android, iosArm64, iosSimulatorArm64.

Depends on: `:domain`, `:sky`, `:data`, `:design`, and Koin for DI.
