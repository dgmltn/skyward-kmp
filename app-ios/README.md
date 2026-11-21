# app-ios

The XcodeGen project for the iPhone app and its StandBy widget. `project.yml`, the Swift sources,
Info.plists and entitlements are committed; the generated `Skyward.xcodeproj` is not.

```sh
xcodegen
xcodebuild -scheme Skyward -destination 'generic/platform=iOS Simulator' build
```

| Target | Kind | Bundle ID | Kotlin framework |
|---|---|---|---|
| `Skyward` | iOS app | `com.dgmltn.skyward` | `SkywardKit` from `:app` |
| `SkywardWidget` | Widget extension, embedded in `Skyward` | `com.dgmltn.skyward.widget` | `SkywardWidgetKit` from `:widget` (static) |

Both targets share the App Group `group.com.dgmltn.skyward`, through which the app saves the last
location for the widget. Each target builds its framework in a pre-build script with
`./gradlew :<module>:embedAndSignAppleFrameworkForXcode`.

Deployment target: iOS 17.0. Requires [XcodeGen](https://github.com/yonaskolb/XcodeGen) and JDK 17.

## Signing

`project.yml` sets team `PAPMGT5WBR` with automatic signing (`CODE_SIGN_STYLE: Automatic`). To use
a different account, change `DEVELOPMENT_TEAM` and the `com.dgmltn.skyward` bundle ID and App
Group.

### First run on a device

The first build to a phone registers the App IDs (`com.dgmltn.skyward`, `com.dgmltn.skyward.widget`) and
the App Group `group.com.dgmltn.skyward` on the developer account through automatic signing. Pick your
device in Xcode and run the `Skyward` scheme; Xcode creates the provisioning profiles itself.
