# TFTricks

TFTricks is an offline-first **Teamfight Tactics companion app** for Android with 100% original branding: pure black background, bright yellow (`#FFD400`) accent, dark-mode only.

The app currently covers **Phases 1–3**: full project setup, brand theme, navigation, all main screens — dashboard, filterable databases (comps, champions, items, augments), detail screens, traits with breakpoints, item combos, patch notes, global search, a DataStore-backed favorites system, a Team Builder with live trait calculation — and the **in-game overlay**: a floating button over TFT that expands into a searchable panel with comps, items, traits, champions, augments and favorites.

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** with a hand-rolled dependency container (no DI framework yet)
- **Offline-first**: all data comes from JSON files in `assets/data/` parsed with **kotlinx.serialization** — no network calls anywhere
- **DataStore (Preferences)** for local persistence: favorites per category + teams saved from the Team Builder
- **Compose Navigation** with a bottom navigation bar, a "More" hub, and parameterized detail routes (comp / champion / item / patch)
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
    │   ├── local/UserDataStore.kt          # Preferences DataStore instance
    │   └── repository/                     # Repository implementations
    │       ├── CachedAssetRepository.kt    # Load-once in-memory cache base class
    │       ├── JsonRepositories.kt         # One implementation per data type
    │       ├── DataStoreFavoritesRepository.kt   # Favorite ids per category
    │       └── DataStoreSavedTeamsRepository.kt  # Teams built in the Team Builder
    ├── domain/                     # DOMAIN LAYER
    │   ├── model/                  # TeamComp, Champion, Item, Trait, Augment, PatchNote,
    │   │                           #   SavedTeam, FavoriteCategory
    │   └── repository/             # Repository interfaces the UI depends on
    └── ui/                         # UI LAYER (MVVM)
        ├── theme/                  # Black/yellow brand: Color, Type, Shape, Theme
        ├── components/             # TFTricksLogo, TierBadge, InfoCard, BoardGrid,
        │                           #   FilterChipRow, FavoriteButton, ExpandableSection, …
        ├── navigation/             # Destination enum, DetailRoutes, NavHost graph
        ├── AppViewModelProvider.kt # ViewModel factory wired to repositories
        └── screens/                # One package per screen (screen + ViewModel)
```

### Architecture flow

```
assets/data/*.json → AssetJsonDataSource → Json*Repository (in-memory cache) ┐
DataStore (favorites, saved teams) ──────────────────────────────────────────┤
        → domain interfaces → ViewModel (StateFlow<UiState>) → Compose screen
```

Each screen collects a `StateFlow<UiState<…>>` from its ViewModel and renders loading / error / success states. Filters and favorites are combined reactively, so hearts and filtered lists update instantly everywhere.

### Screens

- **Home** — quick-access tiles, current patch, featured S-tier comps, favorites section
- **Team Comps** — tier/tag filters, tier badges, favorites; **Comp detail** with boards on a hex grid, carries/tanks, items, traits, game plan, strengths/weaknesses in collapsible sections
- **Champions** — 2-column grid, cost color-coding, cost/trait filters; **detail** with ability, items, positioning, tappable best comps
- **Traits** — expandable breakpoints + tappable champion chips
- **Items** — category filter + **Combos** tab (component-pair explorer); **detail** with recipe, effect, best users, alternatives
- **Augments** — tier filter, priority stars, tappable best comps
- **Patch notes** — list + per-change detail
- **Search** — grouped results across all data types
- **Saved Comps** — favorited comps + teams saved from the builder
- **Team Builder** — tap-to-place board, live trait breakpoints, save teams locally
- **Overlay Settings** — permission onboarding, service start/stop, live customization (opacity, panel size, transparent background, compact mode)

Settings remains a placeholder for a later phase.

## In-game overlay

The overlay is a foreground service (`overlay/OverlayService`) that draws a small draggable yellow button on top of any app. Tapping it expands a compact panel with search + tabs (Comps, Items, Traits, Champs, Augments, Favorites); tapping a comp opens a mini guide (board grid, itemized units, positioning, roll timing) without leaving the game. The button snaps to screen edges and its position is remembered.

### Implementation choice: Compose in a WindowManager window

The overlay UI is **Jetpack Compose rendered through a `ComposeView` added directly to the `WindowManager`** (`TYPE_APPLICATION_OVERLAY`, available since API 26 — exactly our min SDK). A ComposeView outside an Activity needs view-tree owners, which `overlay/OverlayLifecycleOwner` provides (LifecycleOwner + ViewModelStoreOwner + SavedStateRegistryOwner). This is the widely used, stable pattern for Compose overlays and was chosen over classic Views so the overlay reuses the app's theme and components (`BoardGrid`, `TierBadge`) with zero duplication.

One window is reused for both states — its `LayoutParams` morph between:

- **Bubble**: wrap-content, `FLAG_NOT_FOCUSABLE` (game keeps keyboard/keys), draggable, edge-snapping, position persisted to DataStore.
- **Panel**: sized from the panel-size setting (42/58/74% of screen height), focusable so the in-panel search field can take keyboard input.

Performance: the panel reads from the same in-memory-cached repositories as the app (`OverlayPanelState`), loads each dataset once, and window-level opacity is applied via `LayoutParams.alpha` (no per-frame compositing work in Compose). Settings changes stream in live from DataStore.

On Android 14+ the service declares the required `specialUse` foreground-service type with an explanatory subtype property.

### Known Android overlay limitations & troubleshooting

- **Permission**: the overlay needs *Display over other apps* (`SYSTEM_ALERT_WINDOW`). Grant it from Overlay Settings → *Grant permission*. If it was denied, the same button reopens the system page — the rest of the app works fine without it.
- **Battery optimization / aggressive task killers** (Samsung, Xiaomi, Huawei, OnePlus…): the foreground service can still be killed in the background. Fix: exclude TFTricks from battery optimization ("Unrestricted" battery) and, on MIUI-like systems, allow "Display pop-up windows while running in the background".
- **Game/performance modes**: some devices' Game Mode or "Do Not Disturb while gaming" hides or blocks overlay windows. Disable that mode for TFT or whitelist TFTricks in the game-tools settings.
- **Notification on Android 13+**: the foreground-service notification is only visible if notifications are allowed; TFTricks asks once when you start the overlay. The overlay itself works either way.
- **Notch / cutouts / rotation**: the window is repositioned and re-clamped on configuration changes; the bubble is kept inside the visible screen area. On devices with big cutouts the snapped edge position may sit slightly below the cutout — drag it wherever you like, the position is saved.
- **Secure screens**: Android never draws overlays on top of secure/DRM surfaces or during some full-screen protected content; that's a platform restriction.
- **After reboot**: overlays are never auto-restored by the OS — start it again from Home (quick toggle) or Overlay Settings.

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
