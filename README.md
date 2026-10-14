<p align="center">
  <img src="docs/icon.png" alt="The Eagle's Watch" width="128" height="128">
</p>

<h1 align="center">The Eagle's Watch</h1>

<p align="center"><i>Sundan ang bawat piso. Bantayan ang bawat buwis.</i></p>

This is for the best transparency of tax that is earned through the blood and sweat of each citizen of the Philippines. We will keep on guarding from LGUs up to the President using this project.

The goal is a bridge of transparency: government agencies post their projects in one public place, and citizens follow them. For every project: where the money comes from and how much, when it starts and ends, its purpose, who proposed it and who is responsible, dated updates with pictures, and a full write-up. Citizens like or dislike a project and leave feedback. Every figure should link back to an official public record.

> **Status: design preview.** The app shows the product's structure with **10 sample projects**. Projects, people and figures are placeholders, not real government data, and are labelled as such. The daily "Alam mo ba?" facts are real rules of Philippine public finance, each with the law it comes from.

## The app

The interface takes its patterns from Airbnb's iOS app: a search-first explore feed of picture cards, a listing-style project page, a watchlist and an inbox of updates. Phones get a bottom navigation bar; wide windows get tabs in the top bar, a grid of cards and a side card on project pages.

| Screen | What it does |
|---|---|
| **Explore** | Search by project, place or agency. Tabs for Roads, Bridges, Schools, Health, Water, Flood control, Farms and Buildings, plus filters for level of government and status. Each card has swipeable pictures, a status badge, a watch (eye) button, citizen approval, place, proposing agency, dates and budget. |
| **Project page** | Picture gallery with captions; budget, funding source, dates, days left or overdue; proposing and implementing agencies, contractor and the officials responsible; work done against time used and funds released; the purpose and a full article; a timeline of updates with pictures; likes, dislikes and citizen feedback; and the records each figure will link to. A bottom bar keeps Like, Dislike and Feedback in reach. |
| **Watchlist** | Projects the citizen follows. |
| **Updates** | Every agency post, newest first, for all projects or only watched ones. |
| **About** | What the project promises, what agencies will be able to do, the public records it plans to connect, and how to contribute. |

Watched projects, reactions and feedback are saved on the device until accounts and a server exist.

Project pictures are drawn in code, not photographs: each kind of project is illustrated at the stage its works had reached, from a cleared site to a finished structure. When agencies post real photos, these give way to them.

Style: white pages, flag blue for actions, gold for approval, red only for the "Sample data" notice. Inter for text, Barlow Condensed for the wordmark and big numbers. The app icon source lives in [`design/icon`](design/icon).

## Tech

[Compose Multiplatform](https://www.jetbrains.com/compose-multiplatform/) with Kotlin. One shared UI in `commonMain`, with thin platform entry points.

| Target | Entry point | Notes |
|---|---|---|
| Android | `composeApp/src/androidMain` | minSdk 24, edge-to-edge |
| iOS | `composeApp/src/iosMain` + `iosApp/` | Needs a Mac with Xcode |
| Web | `composeApp/src/wasmJsMain` | Kotlin/Wasm (Beta in Compose Multiplatform) |

```
composeApp/src/commonMain/kotlin/ph/eagleswatch/
├── App.kt              # layout breakpoint, tabs, and the stack of project and article pages
├── content/            # Projects.kt (model and sample projects), Day.kt (dates), Content.kt (facts, principles)
├── nav/                # top bar, bottom bar, destinations
├── screens/            # Explore, Project, Article, Watchlist, Updates, About
├── state/              # CitizenState (watchlist, reactions, feedback), platform storage, share and back
└── ui/                 # theme, icons, project illustrations, buttons, badges
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
