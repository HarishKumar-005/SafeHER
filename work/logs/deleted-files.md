# Deleted/Modified Files — Women-Only Refactor

## Files Destructively Modified

### `app/src/main/java/com/phantomcrowd/data/UseCase.kt`
**Action:** Removed 5 enum entries and all associated subcategories
- **Removed enum entries:** `ACCESSIBILITY`, `LABOR_RIGHTS`, `FACILITIES`, `ENVIRONMENTAL`, `CIVIL_RESISTANCE`
- **Removed subcategory lists:** `accessibilityCategories`, `laborRightsCategories`, `facilitiesCategories`, `environmentalCategories`, `civilResistanceCategories`
- **Removed ImpactMessages branches** for all 5 non-women use cases
- **Added:** `DEVELOPER_SHOW_ALL_CATEGORIES = false` toggle for future restore
- **Kept:** `WOMENS_SAFETY` enum entry + 6 subcategories (Assault, Harassment, Stalking, Unsafe Area, No Emergency Help, Other)

### `app/src/main/java/com/phantomcrowd/ui/PostCreationARScreen.kt`
**Action:** Removed Step 1 (UseCase selection grid)
- Step 1 UseCase grid (`Step1UseCaseSelection`) is no longer invoked
- Flow renumbered: 5 steps → 3 steps + success
- Form defaults to `UseCase.WOMENS_SAFETY`

### `app/src/main/java/com/phantomcrowd/ui/NearbyIssuesScreen.kt`
**Action:** Removed category filter dropdown
- `FilterRow` function replaced with `SortFilterRow` (sort-only)
- `selectedUseCase` state variable removed
- `showUseCaseDropdown` state variable removed
- "All Categories ▼" chip completely removed
- **Added:** SafeHer AR header + Women's Safety badge at top

### `app/src/main/java/com/phantomcrowd/ui/theme/Color.kt`
**Action:** All color hex values updated to PRD tokens
- Primary: `#7C3AED` → `#C87FAE` (mauve pink)
- SOS/Error: `#DC2626` → `#E34F5A` (coral)
- Success: `#16A34A` → `#3FB28F` (teal)
- Background: `#FFF5F7` → `#FFF7FA` (soft panel)
- Text primary: `#1A0E2E` → `#0F1724`
- Divider: `#E8D5E8` → `#EAE6E9`
- Heatmap yellow: `#FBBF24` → `#F6C85F`

### `app/src/main/java/com/phantomcrowd/ui/theme/DesignSystem.kt`
**Action:** Typography replaced, button radius updated
- Added Poppins SemiBold FontFamily (`R.font.poppins_semi_bold`)
- Added Inter FontFamily (`R.font.inter_variable`)
- H1: FontFamily.Default → Poppins SemiBold 28sp
- H2: FontFamily.Default → Poppins SemiBold 22sp
- Body/Labels: FontFamily.Default → Inter Regular/SemiBold
- Button radius: 10dp → 14dp

### `app/src/main/java/com/phantomcrowd/ui/components/OnboardingOverlay.kt`
**Action:** Reduced from 5 pages to 3
- Removed: "100% Anonymous", "Emergency SOS", "Data & Privacy" pages
- Kept/Modified: Privacy (page 1), AR Tutorial (page 2), Quick Start (page 3)
- CTA changed: "Get Started" → "Start SafeHer"

## No Files Deleted
All modifications were in-place. The `Step1UseCaseSelection` composable function still exists in `PostCreationARScreen.kt` but is no longer invoked (dead code).
