# TFTricks

TFTricks is an offline-first **Teamfight Tactics companion app** for Android with 100% original branding: pure black background, bright yellow (`#FFD400`) accent, dark-mode only.

This repository currently contains **Phase 1 — the foundation**: full project setup, brand theme, navigation shell with placeholder screens, data models, bundled sample data, repositories with in-memory caching, and ViewModels proving the whole data pipeline end to end.

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** with a hand-rolled dependency container (no DI framework yet)
- **Offline-first**: all data comes from JSON files in `assets/data/` parsed with **kotlinx.serialization** — no network calls anywhere
- **Compose Navigation** with a bottom navigation bar + "More" hub
- Min SDK **26**, target/compile SDK **35**, Gradle Kotlin DSL with a version catalog

## Project structure

```
app/src/main/
├── assets/data/            # The offline database (JSON)
│   ├── team_comps.json     # 5 team comps with full play guides
│   ├── champions.json      # 15 champions
│   ├── traits.json         # 8 traits with breakpoints
│   ├── items.json          # 12 items
│   ├── augments.json       # 10 augments
│   └── patch_notes.json    # 2 patch summaries
└── java/com/tftricks/app/
    ├── TFTricksApplication.kt      # Creates the AppContainer
    ├── MainActivity.kt             # Splash + Compose entry point
    ├── di/
    │   └── AppContainer.kt         # Manual dependency wiring
    ├── data/                       # DATA LAYER
    │   ├── source/AssetJsonDataSource.kt   # Reads + parses asset JSON off the main thread
    │   └── repository/                     # Repository implementations
    │       ├── CachedAssetRepository.kt    # Load-once in-memory cache base class
    │       └── JsonRepositories.kt         # One implementation per data type
    ├── domain/                     # DOMAIN LAYER
    │   ├── model/                  # TeamComp, Champion, Item, Trait, Augment, PatchNote
    │   └── repository/             # Repository interfaces the UI depends on
    └── ui/                         # UI LAYER (MVVM)
        ├── theme/                  # Black/yellow brand: Color, Type, Shape, Theme
        ├── components/             # TFTricksLogo (placeholder), TierBadge, InfoCard, …
        ├── navigation/             # Destination enum + NavHost graph
        ├── AppViewModelProvider.kt # ViewModel factory wired to repositories
        └── screens/                # One package per screen (screen + ViewModel)
```

### Architecture flow

```
assets/data/*.json → AssetJsonDataSource → Json*Repository (in-memory cache)
        → domain interfaces → ViewModel (StateFlow<UiState>) → Compose screen
```

Each list screen collects a `StateFlow<UiState<List<T>>>` from its ViewModel and renders loading / error / success states. Screens for later phases (Team Builder, Saved Comps, Search, Settings, Overlay Settings) are placeholders behind real navigation routes.

### Navigation

Bottom navigation bar with five tabs — **Home, Comps, Champions, Builder, More** — chosen over a drawer so core lookups are one thumb-tap away mid-game. The **More** tab hubs the secondary screens: Traits, Items, Augments, Saved Comps, Patch Notes, Search, Settings, Overlay Settings.

## Building

Requirements: **Android Studio** (Ladybug or newer) with **JDK 17+**; the Android SDK for API 35 is fetched by Studio automatically.

```bash
# From Android Studio: File → Open… → select this folder, then Run ▶
# Or from the command line:
./gradlew assembleDebug        # build the debug APK
./gradlew installDebug         # install on a connected device/emulator
```

The app launches into the navigation shell. The Home screen shows live counts from every JSON file, and the Comps / Champions / Traits / Items / Augments / Patch Notes screens list the sample data — proof the offline data pipeline works.

## Branding notes

- The logo composable (`ui/components/TFTricksLogo.kt`) and the launcher/splash vectors are **placeholders** with a stable API — swap in the real logo assets later without touching call sites.
- All game data in `assets/data/` is hand-written sample data for development.

## Roadmap (later phases)

Detail screens → search → favorites/saved comps → team builder → overlay → polish. Not started yet by design.
