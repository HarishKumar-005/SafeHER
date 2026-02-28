# SafeHer AR — Project Structure

A mapping of the key source files, directories, and configuration nodes within the repository.

## Root Level
* `/app` - The primary Android application module
* `/build.gradle.kts` (or `.gradle`) - Project-level Gradle build script
* `/gradle.properties` - Global configurations
* `/work/` - Documentation and generated artifacts (Docs, Logs, Reports, Screenshots)

## App Module (`/app`)
* `build.gradle` - Application module dependencies (Compose, CameraX, Firebase, Location).
* `google-services.json` - Firebase integration config (must be injected by the developer).
* `src/main/AndroidManifest.xml` - Declares permissions (Camera, Fine Location, Internet).

## Source Code (`/app/src/main/java/com/phantomcrowd/`)
### UI Components (`/ui/` and `/ui/components/`)
* **Screens:**
  - `ARViewScreen.kt` - Main interactive augmented reality camera module.
  - `ARNavigationScreen.kt` - (Inferred) Turn-by-turn or directional overlay.
  - `SurfaceAnchorScreen.kt` - Places physical anchors (ARCore extension if needed).
  - `HomeScreen.kt` - (Inferred) Landing dashboard.
  - `PostCreationScreen.kt` - (Inferred) Step-by-step reporting UI.
  - `ImpactDashboardScreen.kt` - (Inferred) User progress and community stats.
* **Composables:**
  - `ARDetailSheet.kt` - Draggable bottom sheet for inspecting an AR incident.
  - `ARInlineLabel` (Inside `ARViewScreen.kt`) - The floating AR marker.
  - `MessageCard.kt`

### Data Layer (`/data/`)
* `AnchorData.kt` - Core model mapping Firebase documents (id, lat, lon, text, severity, upvotes).
* `SurfaceAnchor.kt` - Heavy 3D anchor representation.
* `RiskLevel.kt` - Enums for HIGH/MED/LOW.
* `RiskScoring.kt` - Algorithm computing risk decay over time based on votes.

### Utils / Helpers (`/utils/`)
* `BearingCalculator.kt` - Geographic math converting lat/lon to compass headings.
* `Logger.kt` - Standardized debug output formatting.

## Resources (`app/src/main/res/`)
* `/values/colors.xml` - Non-Compose color constants defining the SafeHer palette (`#FFDB2777` Pink).
* `/values/themes.xml` and `/values-night/themes.xml` - Base XML themes (forced light mode for unified UI).
* `/font/` - Custom `.ttf` fonts ensuring consistent typography (`poppins_semi_bold.ttf`, `inter_variable.ttf`).
* `/mipmap/` - App iconography vectors.
