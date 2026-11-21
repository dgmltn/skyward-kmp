# Skyward — design

Date: 2026-11-21
Status: draft, awaiting approval (builder check done)

## Intent

Skyward is a native Android + iPhone version of the narrow/portrait layout of James Yox's
[Kastro demo](https://yoxjames.github.io/kastro-demo/): a 24-hour sun-and-moon dial with a clock
hand, plus tabs listing today's phases and events. Doug wants it on his own nightstand — mainly
as an iPhone **StandBy widget** showing just the dial — and may publish it on GitHub as open
source. It's also a chance to exercise KMP with an iOS widget.

Success: the StandBy widget sits on Doug's nightstand showing an accurate dial for where he is,
readable in StandBy's red night mode; the app matches the demo's dial and tabs closely enough to
feel like the same thing, natively.

## Goals

- Android and iPhone app, one screen:
  - top: square (1:1) tappable dial for **today** at the device's location
  - below: a detail card for whatever was tapped (dismissable), then four tabs —
    **States, Events, Current, About**
- iPhone `systemSmall` widget for StandBy (also usable on Home Screen and, via iPhone widgets on
  Mac, the Mac desktop) showing only the dial.
- Approximate device location.
- Open-source on GitHub under GPL-3.0.

## Non-goals

- Date picker / other days (today only).
- Manual location entry or a map (the demo has a Leaflet map; we don't).
- Android widget, Android "StandBy"-like mode, watch, native macOS app.
- Interactive widget (tapping the widget just opens the app).
- Elevation/height input (use 0 m).

## Anti-goals

- A widget that blanks out or becomes an unreadable red blob in StandBy night mode.
- A widget that shows nothing useful when no location has ever been saved (it must say
  "Open Skyward to set location").
- Divergence between the app dial and the widget dial (different arcs/times for the same moment).

## Constraints

- **Distribution:** Doug has a paid Apple Developer account and installs on his own iPhone from
  Xcode or TestFlight. The App Store is out of scope for v1 (GPL-3.0 conflicts with App Store
  terms). Android: build/sideload; F-Droid or Play publishing isn't in v1 but shouldn't be blocked.
  The paid team makes the App Group (shared app/widget storage) available.

- **License:** the demo (`github.com/yoxjames/kastro-demo`) is GPL-3.0. We port its dial and
  state/phase derivation logic, so Skyward is GPL-3.0, with attribution to James Yox in the
  README, the About tab, and file headers of ported files. Kastro itself is Apache-2.0.
- **Kastro** (`dev.jamesyox:kastro`, 0.6.0 at time of writing — pin latest stable at setup)
  publishes `jvm`, iOS, and macOS targets but **no Android target**. Android consumes the `jvm`
  variant (Kotlin's `androidJvm` → `jvm` compatibility). Verify this resolves in the first build
  step; if it doesn't, fall back to depending on Kastro only from `jvmMain`/`iosMain` via a
  shared `jvmAndAndroid` intermediate source set.
- **WidgetKit:** widgets are SwiftUI only — no Compose, no `UIViewRepresentable`. Widget
  extensions have a ~30 MB memory limit and a limited refresh budget. StandBy shows
  `systemSmall` widgets, removes the container background, and in night mode renders in the
  `vibrant` rendering mode (luminance mapped to red). The widget's content must work by
  brightness/contrast, not hue.
- **Doug's KMP conventions** (kmp-preferences skill) apply: module naming, Koin, DataStore,
  Kermit, kotlinx-datetime + `kotlin.time.Instant`, kotlinx-collections-immutable, AndroidX
  ViewModel with a single UiState StateFlow, version catalog for versions only, project config in
  `gradle.properties`, latest stable versions pinned from live registry metadata, XcodeGen for
  iOS, non-UI modules get a `jvm()` target and test in `jvmTest`, module READMEs + root
  `ARCHITECTURE.md`, design docs under `/resources`.

## Approaches considered

**A. Shared Kotlin dial model, two thin renderers — chosen.** Kotlin turns Kastro results into a
renderer-agnostic description of the dial (arcs with angles/radii/colors, markers, hand angle,
moon phase, labels) and owns hit-testing. Compose Multiplatform draws it in the app on both
platforms (dial, tabs, detail card written once). A small SwiftUI `Canvas` draws the same model
in the widget. Trade-off: two drawing implementations to keep visually aligned, but both are
dumb loops over the same model, so drift is limited to styling.

**B. Kotlin renders a bitmap for the widget (Skia).** One renderer, but Skia in a widget
extension risks the memory limit, and `vibrant` mode tints a raster as a single silhouette,
destroying the rings in night mode. Rejected.

**C. Native SwiftUI UI on iOS, Compose on Android, KMP for logic only.** iOS app and widget
share one SwiftUI dial, but tabs/lists/detail get written twice — more duplicated code than A's
second dial renderer, and less KMP. Rejected.

## Design (decided)

### Screen

```
┌─────────────────────────┐
│        [ dial ]         │  1:1, full width (centered, capped on wide screens)
├─────────────────────────┤
│ ▸ Golden hour  6:41–7:12│  detail card for the selection, ✕ to dismiss
├─────────────────────────┤
│ States│Events│Current│About
│  …list for the tab…     │
└─────────────────────────┘
```

The column below the dial scrolls; the dial stays pinned at the top.

### Dial (ported from the demo's `clock/` package)

Geometry, in the demo's 200×200 coordinate space centered on the origin (scale to the canvas):

- Time range: local midnight today → local midnight tomorrow (device time zone), computed as
  `date.atStartOfDayIn(tz)..(date + 1 day).atStartOfDayIn(tz)` so DST days are 23 or 25 hours
  (the demo uses `start + 1.days`, which is wrong on DST days — builder's call, fixed here). Midnight sits
  at the bottom with a small wedge gap; noon at the top. Angle formula from the demo's
  `ClosedRange<Instant>.computeAngle` (`Utils.kt`), including `wedgeSize`.
- **Clock overlay** at r ≈ 88: hour ticks and hour labels (12am, 1am … 12am).
- **Solar phase ring** at r = 75: night / astronomical / nautical / civil twilight / day spans
  (demo `TwilightSpanner`, from `solarPhaseStates`).
- **Light ring** at r = 62: golden hour / blue hour spans (demo `LightPhaseSpanner`, from
  `solarLightState`).
- **Moon ring** at r = 35: moon up vs. down spans (demo `LunarHorizonSpanner`, from
  `lunarHorizonState`).
- **Event markers** at r ≈ 85–86.5: sunrise/sunset/noon/nadir/golden-hour/moonrise/moonset glyphs
  (demo `EventSpanner`, `events/` glyphs).
- **Now:** red center dot, red hand from center to r = 91, and a moon-phase icon at r = 51 on the
  hand's angle (demo `Moon.kt` phase drawing), shown only when now is within the range (always,
  for today).
- **Selection:** highlighted with the demo's opacity rule (non-selected items dimmed; see the
  demo's `Utils.kt` and `svgk/Selected.kt`).
- Colors: port the demo's palette (`KastroDemoStylesheet.kt` and spanner color constants). Each
  palette entry also carries a luminance level so the widget's vibrant variant stays legible.

Data derivation (`solarPhaseStates`, `solarLightState`, `lunarHorizonState`, solar/lunar event
lists) is ported from the demo's `CelestialParameters.kt`, `sol/`, `luna/`, and `clock/`
derivations into common Kotlin.

### Interaction

- Event markers are tappable — an addition over the demo, where only spans are (builder's call).
- The detail card sits above the tabs rather than replacing them as the demo does (agreed in
  conversation).
- Tapping the dial hit-tests in Kotlin (polar coordinates → ring + time → span, or nearest event
  marker within a tolerance) and selects that item; the detail card shows it.
- Tapping a row in States or Events selects the same item and highlights it on the dial.
- Tapping the selected item again, or ✕ on the card, clears the selection.
- Detail card content follows the demo's `detail/` views: name, start/end or time, duration,
  and the phase's description text (ported from the demo's description tables).

### Tabs (following the demo's `view/`)

- **States** — today's spans grouped as solar phases, light phases, moon horizon, with
  start–end times.
- **Events** — today's solar and lunar events in time order, with times.
- **Current** — sun and moon right now: current phase, sun altitude/azimuth, moon
  altitude/azimuth, illumination and phase name, next sunrise/sunset and moonrise/moonset.
- **About** — what the app is, credits to Kastro and the Kastro demo (James Yox), GPL-3.0
  notice, source link.

Times use the device's locale 12/24-hour setting.

### Location

- Approximate only. Android: `ACCESS_COARSE_LOCATION` via the platform `LocationManager` (no
  Google Play services, so the open-source build stays F-Droid-friendly). iOS:
  `CLLocationManager` with when-in-use authorization and `NSLocationDefaultAccuracyReduced = YES`.
- The app asks on first launch, requests a fresh fix each time it comes to the foreground, and
  saves the last fix with its timestamp.
- Before any fix exists: the dial area shows a "Location needed" state with a button to grant
  permission (or open Settings if denied).
- The widget reads the saved location only (it does not request location itself). With no
  saved location it shows "Open Skyward to set location".
- **Staleness is accepted (Doug's call):** if the phone moves and the app isn't opened, the
  widget keeps showing the last saved location, with no age indicator. The phone rarely leaves
  the nightstand. The day still runs midnight to midnight in the device's current time zone.

### Updates

- App: the hand updates once a minute while visible. At local midnight, or when the app returns
  to the foreground on a new day, it recalculates for the new date. A new location fix
  recalculates too.
- Widget: the timeline provider emits one entry every 5 minutes for the next 6 hours, crossing
  midnight correctly (entries after midnight use the next day's data), with reload policy
  `.atEnd`. The app calls `WidgetCenter.shared.reloadAllTimelines()` after saving a new location.

### Widget rendering

- `systemSmall` only. Dial only; hour labels may be dropped at this size if unreadable (builder's
  call after seeing it on a device), but rings, markers, hand, and moon stay.
- `containerBackground(for: .widget)` with the dark dial background so StandBy can remove it.
- Reads `widgetRenderingMode`: `fullColor` uses the palette; `vibrant` and `accented` draw each
  element at its palette luminance so rings remain distinct in red night mode.
- Not interactive; tap opens the app (default widget URL).

## Architecture

### Modules (tiny-app exception: one UI feature module)

This is a one-screen app, so the screen's UI lives in a single feature module `:sky` — the
tiny-app exception to one-module-per-feature.

| Module | Kind | Purpose |
|---|---|---|
| `:domain` | KMP, pure Kotlin (jvm, android, ios) | Models (`Location`, `SkyDay` with spans/events, `SkyNow`) and contracts (`LocationRepository`, `SkyRepository`). |
| `:data` | KMP (jvm, android, ios) | Kastro-backed `SkyRepository` (ported derivations), platform location actuals, last-location store. |
| `:dial` | KMP, pure Kotlin, no Compose (jvm, android, ios) | `DialModel` builder (angles, radii, ARGB colors + luminance, markers, labels, hand, moon phase), angle math, hit-testing. Depends on `:domain`. |
| `:design` | KMP Compose | Theme, colors, typography. |
| `:sky` | KMP Compose | Screen: ViewModel + UiState, Compose `DialCanvas` renderer for `DialModel`, detail card, four tabs. |
| `:app` | KMP Compose | `SkywardRoot`, theme application, Koin module assembly. No Navigation 3 — single screen. |
| `:widget` | KMP, iOS + jvm, no Compose | Swift-facing API for the widget: given a saved location and a list of instants, returns `DialModel`s. Builds the static framework `SkywardWidgetKit`, linked only into the widget extension, so Compose stays out of the widget's memory. |
| `app-android` | Android app | Thin entry. |
| `app-ios` | XcodeGen project | iOS app target (hosts `:app` framework) + `SkywardWidget` extension target (links `SkywardWidgetKit`, SwiftUI `Canvas` renderer for `DialModel`). Shared App Group. |

The app and widget are separate processes with separate Kotlin frameworks, so there's no
framework duplication within a single binary.

### Last-location store (deviation from the DataStore default)

The iOS app writes and the widget extension reads the saved location across processes. DataStore
is single-process, so on iOS the store is `NSUserDefaults(suiteName: <app group>)`, accessed
from Kotlin in `:data`'s `iosMain`. Android uses DataStore Preferences. This is a deliberate,
flagged deviation from the "DataStore for key-value" preference, made for cross-process safety.

### Libraries

Per kmp-preferences: Kastro, Koin, Kermit, kotlinx-datetime, kotlinx-coroutines,
kotlinx-collections-immutable, AndroidX lifecycle ViewModel (KMP), DataStore Preferences
(Android), Compose Multiplatform. No HTTP, database, or navigation library is needed.

### Testing

- `:data` (`jvmTest`): derivation tests against known dates/locations — compare spans and event
  times to the Kastro demo's output for the same inputs (e.g., a few fixed dates in Colorado,
  London, Tromsø in polar day/night, and the equator).
- `:dial` (`jvmTest`): angle math (midnight/noon/wedge), arc generation for edge cases (span
  crossing range boundaries, no moonrise today, polar day with no sunset), hit-testing.
- `:sky`: ViewModel tests in the Android host-test source set.
- Widget and Compose renderers: compile-verified and checked by eye on device/simulator,
  including StandBy night mode.

### Platform minimums

iOS 17 (StandBy, `containerBackground`, `widgetRenderingMode`). Android minSdk per the template's
current default, recorded in `gradle.properties` (builder's call).

## Left to the builder

- Exact visual tuning of the small-widget dial (label legibility, stroke widths at small size).
- How `vibrant` luminance levels are chosen per palette entry, as long as adjacent ring segments
  stay distinguishable.
- Hit-test tolerances.
- App icon and launch screen (simple placeholder is fine for v1).
- Package/bundle IDs, App Group name.
