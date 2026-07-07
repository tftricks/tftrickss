# TFTricks

TFTricks is an offline-first **Teamfight Tactics companion app** for Android with 100% original branding: pure black background, bright yellow (`#FFD400`) accent, dark-mode only.

The app is feature-complete for its first release: brand theme, navigation, all main screens — dashboard, filterable databases (comps, champions, items, augments), detail screens, traits with breakpoints, item combos, patch notes, global search — a DataStore-backed favorites system, a Team Builder with live trait calculation, the **in-game overlay** (floating button over TFT that expands into a searchable panel), ad placeholder slots ready for AdMob, and a real Settings screen.

## Tech stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **MVVM** with a hand-rolled dependency container (no DI framework)
- **Offline-first**: all data comes from JSON files in `assets/data/` parsed with **kotlinx.serialization** — no network calls anywhere
- **DataStore (Preferences)** for local persistence: favorites, saved teams, overlay customization
- **Compose Navigation** with a bottom navigation bar, a "More" hub, parameterized detail routes, and cross-fade transitions
- **AdMob** dependency wired behind a single config switch (placeholders shown until enabled)
- Min SDK **26**, target/compile SDK **35**, Gradle Kotlin DSL with a version catalog

## Project structure

```
app/src/main/
├── assets/data/            # The offline database (JSON) — see "How to add/edit data"
│   ├── team_comps.json     ├── champions.json      ├── traits.json
│   ├── items.json          ├── augments.json       └── patch_notes.json
└── java/com/tftricks/app/
    ├── TFTricksApplication.kt      # Creates the AppContainer, initializes ads
    ├── MainActivity.kt             # Splash + Compose entry point
    ├── di/AppContainer.kt          # Manual dependency wiring
    ├── ads/                        # AdsConfig (flags + ids) and AdsManager
    ├── data/                       # DATA LAYER
    │   ├── source/AssetJsonDataSource.kt   # Reads + parses asset JSON off the main thread
    │   ├── local/UserDataStore.kt          # Preferences DataStore instance
    │   └── repository/                     # Json* (assets) and DataStore* (local) repositories
    ├── domain/                     # DOMAIN LAYER: models + repository interfaces
    ├── overlay/                    # In-game overlay: service, lifecycle owner, Compose UI
    └── ui/                         # UI LAYER (MVVM)
        ├── theme/                  # Black/yellow brand: Color, Type, Shape, Theme
        ├── components/             # InfoCard, TierBadge, BoardGrid, BannerAdSlot, …
        ├── navigation/             # Destination enum, DetailRoutes, NavHost graph
        ├── AppViewModelProvider.kt # ViewModel factory wired to repositories
        └── screens/                # One package per screen (screen + ViewModel)
```

### Architecture flow

```
assets/data/*.json → AssetJsonDataSource → Json*Repository (in-memory cache) ┐
DataStore (favorites, saved teams, overlay prefs) ───────────────────────────┤
        → domain interfaces → ViewModel (StateFlow<UiState>) → Compose screen
```

---

## How to add / edit data

All game data lives in `app/src/main/assets/data/`. Edit the JSON, rebuild, done — there is no database or code generation step. Keep these rules in mind:

- **Names are the glue.** Comps, traits and items reference champions **by display name** (`"Jinx"`), while `bestComps` references comps **by id** (`"comp_gunner_fast8"`). Keep spelling identical everywhere.
- **Ids must be unique** within a file. Convention: `comp_…`, `champ_…`, `trait_…`, `item_…`, `aug_…`, `patch_…`.
- Unknown JSON keys are ignored (`ignoreUnknownKeys` is on), but a **wrong enum value** (tier, difficulty, category) will fail parsing and the affected screen shows an error state.

### team_comps.json — field reference

| Field | Type | Notes |
|---|---|---|
| `id` | string | unique, e.g. `comp_gunner_fast8` |
| `name` | string | shown everywhere |
| `tier` | `"S" \| "A" \| "B" \| "C"` | drives badge color and sorting |
| `difficulty` | `"Easy" \| "Medium" \| "Hard"` | exact spelling |
| `patchVersion` | string | e.g. `"26.2"` |
| `finalBoard` | BoardUnit[] | the level-8/9 board |
| `earlyGameBoard`, `midGameBoard` | BoardUnit[] | optional (default empty), keep 4–6 units |
| `carryChampions`, `tankChampions` | string[] | champion names |
| `bestItems`, `alternativeItems` | string[] | item names |
| `traitsActive` | `{trait, count}[]` | trait name + active unit count |
| `positioningNotes`, `levelingGuide`, `economyGuide`, `rollTiming`, `whenToPlay` | string | free text, keep 1–3 sentences |
| `strengths`, `weaknesses` | string[] | 2–3 bullets each |
| `tags` | string[] | free-form; known values: `reroll`, `fast8`, `vertical`, `flex`, `AD`, `AP`, `beginner`, `advanced` |

**BoardUnit**: `{ "champion": "Jinx", "position": 6, "items": ["Infinity Edge"], "starTarget": 2 }` — `position` is a hex index 0..27 (4 rows × 7 columns, row-major from the back row; row = index / 7). Omit `position` for flexible placement, `items`/`starTarget` are optional.

### champions.json

| Field | Type | Notes |
|---|---|---|
| `id`, `name` | string | |
| `cost` | int 1..5 | drives the grey→green→blue→purple→gold color |
| `traits` | string[] | must match trait names in traits.json |
| `ability` | object | `{name, description, manaStart, manaMax}` — use `manaMax: 0` for passive abilities |
| `recommendedItems` | string[] | item names, 2–3 entries |
| `role` | string | e.g. `"AD Carry"`, `"Tank"` |
| `positioningNotes` | string | one sentence |
| `bestComps` | string[] | **comp ids**, powers the tappable links on champion detail |

### traits.json

| Field | Type | Notes |
|---|---|---|
| `id`, `name`, `description` | string | |
| `breakpoints` | `{count, effect}[]` | ascending counts, ≥2 entries |
| `champions` | string[] | champion names carrying this trait |

### items.json

| Field | Type | Notes |
|---|---|---|
| `id`, `name`, `effect` | string | |
| `components` | string[2] | two base-component names; these also feed the **Combos** tab |
| `bestUsers` | string[] | champion names |
| `goodAlternatives` | string[] | item names |
| `category` | `"AD" \| "AP" \| "tank" \| "utility" \| "attackSpeed" \| "mana"` | exact spelling |

### augments.json

| Field | Type | Notes |
|---|---|---|
| `id`, `name`, `effect`, `notes` | string | |
| `tier` | `"Silver" \| "Gold" \| "Prismatic"` | drives name color |
| `bestComps` | string[] | comp ids → tappable chips |
| `priorityRating` | int 1..5 | rendered as ★★★★☆ |

### patch_notes.json

| Field | Type | Notes |
|---|---|---|
| `id`, `version`, `summary` | string | |
| `date` | string | ISO `YYYY-MM-DD` — the newest date becomes "current patch" on Home |
| `changes` | `{target, description}[]` | one entry per buff/nerf/system change |

**Checklist when adding a comp:** every champion/item/trait name it mentions exists in the other files; add the comp's id to the relevant champions' `bestComps` and augments' `bestComps` if it's a signature pairing.

---

## How to change the logo & colors

**Colors** — one file: `app/src/main/java/com/tftricks/app/ui/theme/Color.kt`. `BrandYellow` is the accent used everywhere; `PureBlack`/`Surface*` are the backgrounds; `TierS/A/B/C` and `Cost1..5` drive the badges. The theme mapping lives in `Theme.kt` (same folder), typography in `Type.kt`, corner radii in `Shape.kt`. Two XML colors mirror the brand for the splash/launcher: `app/src/main/res/values/colors.xml`.

**Logo** — the placeholder mark appears in four places, all designed to be swapped independently:

| Where | File | Replace with |
|---|---|---|
| In-app logo (Home) | `ui/components/TFTricksLogo.kt` | your logo as a drawable + `Image(painterResource(...))`, keep the composable signature |
| Splash screen | `res/drawable/ic_splash_logo.xml` | vector or PNG of the real mark (keep content in the center ~⅔) |
| Launcher icon | `res/drawable/ic_launcher_foreground.xml` (+ background color in `res/mipmap-anydpi-v26/ic_launcher.xml`) | adaptive-icon foreground; keep artwork inside the 66/108 safe zone |
| Overlay bubble | `overlay/ui/OverlayBubble.kt` | swap the Canvas spark for an `Image` |

**App name**: `res/values/strings.xml` → `app_name`.

---

## How to build the APK

Requirements: **Android Studio** (Ladybug or newer) with JDK 17+. First build downloads the Android SDK/AGP automatically.

### Debug build

- **Android Studio**: File → Open → this folder → Run ▶ (installs on the connected device/emulator).
- **Command line**:
  ```bash
  ./gradlew assembleDebug          # APK at app/build/outputs/apk/debug/app-debug.apk
  ./gradlew installDebug           # build + install on a connected device
  ```

### Release build (signed)

1. **Create a keystore** (once, keep it safe — losing it means you can never update the app on Play):
   ```bash
   keytool -genkeypair -v -keystore tftricks-release.keystore \
     -alias tftricks -keyalg RSA -keysize 2048 -validity 10000
   ```
2. **Configure signing** in `app/build.gradle.kts` (or Android Studio: Build → Generate Signed App Bundle/APK, which does this for you):
   ```kotlin
   android {
       signingConfigs {
           create("release") {
               storeFile = file("../tftricks-release.keystore")
               storePassword = System.getenv("TFTRICKS_STORE_PW")
               keyAlias = "tftricks"
               keyPassword = System.getenv("TFTRICKS_KEY_PW")
           }
       }
       buildTypes {
           release { signingConfig = signingConfigs.getByName("release") }
       }
   }
   ```
   Never commit the keystore or passwords; pass them via environment variables or `~/.gradle/gradle.properties`.
3. **Build**:
   ```bash
   ./gradlew assembleRelease        # APK  (app/build/outputs/apk/release/)
   ./gradlew bundleRelease          # AAB for Play Store (app/build/outputs/bundle/release/)
   ```
   Release builds are minified + resource-shrunk (R8) with kotlinx.serialization keep rules already in `app/proguard-rules.pro`.
4. Bump `versionCode`/`versionName` in `app/build.gradle.kts` for every release.

---

## Ads (AdMob)

Ads are fully wired but **disabled by default** — every slot renders a neutral "Ad placeholder" box.

**Paste your real ids in exactly two files:**

1. `app/src/main/java/com/tftricks/app/ads/AdsConfig.kt` — `ADMOB_APP_ID`, `BANNER_UNIT_ID`, `INTERSTITIAL_UNIT_ID`, then set `ADS_ENABLED = true` (and `INTERSTITIALS_ENABLED = true` if wanted).
2. `app/src/main/AndroidManifest.xml` — the `com.google.android.gms.ads.APPLICATION_ID` meta-data value (must match `ADMOB_APP_ID`).

The ids currently in place are Google's official **test ids** — safe for development, never earn revenue.

Placements: banner slots at the bottom of **Home, Team Comps, Champions, Items**; an interstitial hook fires after every `INTERSTITIAL_EVERY_N_COMP_DETAILS` (default 5) comp-detail opens, only when enabled. **The in-game overlay never shows ads** — keep it that way (see the note in `AdsConfig`).

---

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
- **After reboot**: overlays are never auto-restored by the OS — start it again from Home (quick toggle), Settings, or Overlay Settings.

---

## Future upgrade plan

Outline for the phases after this release, in rough order:

1. **Live data sync (CommunityDragon / Data Dragon)** — add a `data/remote` source that downloads champion/trait/item data for the current set, mapped into the existing domain models. The JSON assets become the *fallback/seed*; a `syncedAt` timestamp and a manual "Update data" action in Settings control freshness. The repository interfaces don't change, so no UI work is needed.
2. **Remote config** — a tiny hosted JSON (or Firebase Remote Config) with: minimum supported app version, current patch label, featured comp ids, kill-switch for ads. Fetched best-effort at launch, cached, never blocking.
3. **Comp/meta admin panel** — a small web tool (or even a GitHub repo with the same JSON schema + CI validation) where comps are edited and published as a versioned bundle the app downloads. The field reference above is the schema contract.
4. **Remove-ads IAP** — Google Play Billing with a single non-consumable product; on purchase set a `DataStore` flag that `AdsConfig`/`AdsManager` and `BannerAdSlot` check. The Settings screen already has the "Remove ads" placeholder row wired for this.
5. **Nice-to-haves** — overlay hotkey/quick-search, positioning editor in Team Builder (drag between hexes), per-set data packs, widget with the current top comps.

## Branding notes

- The logo composable (`ui/components/TFTricksLogo.kt`) and the launcher/splash vectors are **placeholders** with a stable API — swap in the real logo assets as described above.
- All game data in `assets/data/` is hand-written sample data for development; see "How to add/edit data" to grow it.
