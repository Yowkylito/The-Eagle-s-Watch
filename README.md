<p align="center">
  <img src="docs/icon.png" alt="The Eagle's Watch" width="128" height="128">
</p>

<h1 align="center">The Eagle's Watch</h1>

<p align="center"><i>Sundan ang bawat piso. Bantayan ang bawat buwis.</i></p>

This is for the best transparency of tax that is earned through the blood and sweat of each citizen of the Philippines. We will keep on guarding from LGUs up to the President using this project.

The goal is a bridge of transparency: a single place to follow where citizens' taxes go, from the barangay and local government units up through national agencies to the highest office. Every figure should link back to an official public record.

> **Status: design preview.** The landing page shows the product's structure and uses **sample, placeholder figures only**. No live government data is connected yet.

## The app

Three screens, one tap apart. No long scroll: phones get a bottom navigation bar, wide windows get tabs in the top bar.

| Screen | What it does |
|---|---|
| **Trace** | "Follow every peso." The citizen sets roughly what they paid in taxes this year (a coin slider on a log scale, with presets), then a gold peso coin carries that amount up a rail of six levels: Citizens, Barangay, City / Municipality, Province, National Agencies, Office of the President. At each level the citizen calls where most of the money goes, then 100 coins flip over to show the split in their own pesos. Visited levels turn gold on the rail, the top of the trail keeps score, and a daily "Alam mo ba?" card shows a real rule of public finance with the law behind it. Progress and the amount are kept on the device, so a return visit picks up where it stopped. |
| **How it works** | Three promises (official records only, one trail across every level, a receipt for every figure) and the public sources the project plans to connect. |
| **Join in** | Open-source links, a way to suggest a data source, and where the project stands today. |

Style: flag-blue stage with the flag's sun behind the pitch (it turns as the coin climbs), cool paper, gold coins as the bright accent. Barlow Condensed for headlines, Inter for text. The app icon source lives in [`design/icon`](design/icon).

## Tech

[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) with Kotlin. One shared UI in `commonMain`, with thin platform entry points.

| Target | Entry point | Notes |
|---|---|---|
| Android | `composeApp/src/androidMain` | minSdk 24, edge-to-edge |
| iOS | `composeApp/src/iosMain` + `iosApp/` | Needs a Mac with Xcode |
| Web | `composeApp/src/wasmJsMain` | Kotlin/Wasm (Beta in Compose Multiplatform) |

```
composeApp/src/commonMain/kotlin/ph/eagleswatch/
├── App.kt              # layout breakpoint, current screen, shared pager and progress state
├── content/Content.kt  # tiers, sample splits, tax kinds, daily facts, principles
├── nav/                # top bar, bottom bar, destinations
├── screens/            # TraceScreen + TracePages, HowItWorksScreen, JoinScreen
├── state/              # TraceProgress, saved on the device (platform storage in each target)
└── ui/                 # theme, text, buttons, coin grid, coin slider, rolling amount, sun
```

## Run

Requires JDK 17+.

```bash
# Web (Kotlin/Wasm): opens a dev server
./gradlew :composeApp:wasmJsBrowserDevelopmentRun

# Android: needs the Android SDK (ANDROID_HOME)
./gradlew :composeApp:installDebug

# iOS: on a Mac
brew install xcodegen
cd iosApp && xcodegen generate && open iosApp.xcodeproj
```

Production web bundle: `./gradlew :composeApp:wasmJsBrowserDistribution`.

## Licenses

Fonts (Barlow Condensed, Inter) are licensed under the SIL Open Font License 1.1. See [`licenses/`](licenses).
