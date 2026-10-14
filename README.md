<p align="center">
  <img src="docs/icon.png" alt="The Eagle's Watch" width="128" height="128">
</p>

<h1 align="center">The Eagle's Watch</h1>

<p align="center"><i>Sundan ang bawat piso. Bantayan ang bawat buwis.</i></p>

This is for the best transparency of tax that is earned through the blood and sweat of each citizen of the Philippines. We will keep on guarding from LGUs up to the President using this project.

The goal is a bridge of transparency: a single place to follow where citizens' taxes go, from the barangay and local government units up through national agencies to the highest office. Every figure should link back to an official public record.

> **Status: design preview.** The landing page shows the product's structure and uses **sample, placeholder figures only**. No live government data is connected yet.

## Landing page

A single scroll that follows a peso upward, in a calm, warm style: paper background, flag-blue accents, soft white cards.

| Section | What it does |
|---|---|
| **Hero** | "Follow every peso." The app icon floats above a card that previews the six levels. |
| **The trail** | A line fills with flag blue as you scroll from Citizens → Barangay → City/Municipality → Province → National Agencies → Office of the President. Each level lists what will be visible and the public sources it relies on. |
| **The explorer** | Pick a level of government to see how a sample ₱100 is allocated. Clearly badged *Sample data*. |
| **How it works** | Official records only, one trail across every level, a receipt for every figure. |

Type: Fraunces for headlines, Inter for text. The app icon source lives in [`design/icon`](design/icon).

## Tech

[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) with Kotlin. One shared UI in `commonMain`, with thin platform entry points.

| Target | Entry point | Notes |
|---|---|---|
| Android | `composeApp/src/androidMain` | minSdk 24, edge-to-edge |
| iOS | `composeApp/src/iosMain` + `iosApp/` | Needs a Mac with Xcode |
| Web | `composeApp/src/wasmJsMain` | Kotlin/Wasm (Beta in Compose Multiplatform) |

```
composeApp/src/commonMain/kotlin/ph/eagleswatch/
├── App.kt              # window sizing, scroll state, section order
├── content/Content.kt  # tiers, principles, sample data (all copy lives here)
├── sections/           # NavBar, Hero, Trail, Explorer, Principles, Footer
└── ui/                 # theme, text, buttons, scroll-reveal helpers
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

Fonts (Fraunces, Inter) are licensed under the SIL Open Font License 1.1. See [`licenses/`](licenses).
