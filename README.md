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
| **Trace** | "Follow every peso." A gold peso coin rides a rail of six levels: Citizens, Barangay, City / Municipality, Province, National Agencies, Office of the President. Swipe the card, tap a level, or press *Next* and the coin follows. Each level shows how a sample ₱100 is allocated, what will be visible there, and the public sources it relies on. |
| **How it works** | Three promises (official records only, one trail across every level, a receipt for every figure) and the public sources the project plans to connect. |
| **Join in** | Open-source links, a way to suggest a data source, and where the project stands today. |

Style: flag-blue stage, cool paper, a gold coin as the one bright accent. Barlow Condensed for headlines, Inter for text. The app icon source lives in [`design/icon`](design/icon).

## Tech

[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) with Kotlin. One shared UI in `commonMain`, with thin platform entry points.

| Target | Entry point | Notes |
|---|---|---|
| Android | `composeApp/src/androidMain` | minSdk 24, edge-to-edge |
| iOS | `composeApp/src/iosMain` + `iosApp/` | Needs a Mac with Xcode |
| Web | `composeApp/src/wasmJsMain` | Kotlin/Wasm (Beta in Compose Multiplatform) |

```
composeApp/src/commonMain/kotlin/ph/eagleswatch/
├── App.kt              # layout breakpoint, current screen, shared level pager state
├── content/Content.kt  # tiers, principles, sample data (all copy lives here)
├── nav/                # top bar, bottom bar, destinations
├── screens/            # TraceScreen, HowItWorksScreen, JoinScreen
└── ui/                 # theme, text, buttons, focus ring, layout info
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
