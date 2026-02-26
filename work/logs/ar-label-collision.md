# AR Label Collision Algorithm

## Overview
2D screen-space collision avoidance for AR labels positioned on camera preview.

## Priority Sorting
Labels are sorted before placement:
1. **Risk level** (descending): HIGH=3 > MEDIUM=2 > LOW=1
2. **Distance** (ascending): closer labels take priority
3. **Confirmations** (descending): more-confirmed labels take priority

## Collision Detection
Each label occupies a rectangle in normalized screen space:
- **X position:** `angleDiff / 60f` (range -1 to +1, center = 0)
- **Y position:** `0.25 + (placedIndex * 0.14)` (top-down stacking)

Collision occurs when a new label's position satisfies BOTH:
- `|existing.x - new.x| < 0.25` (horizontal overlap threshold)
- `|existing.y - new.y| < 0.12` (vertical overlap threshold)

## Radial Push
When collision detected:
- Push new label DOWN by `0.12` (approximately 12° equivalent in screen space)
- Maximum 3 push attempts per label
- If still colliding after 3 attempts → label goes to cluster

## Cluster Handling
- Labels that can't be placed (position > 0.85 or > 5 already placed) are clustered
- When 2+ labels clustered → show `ClusterMarker` (circle badge with count)
- Tapping cluster opens highest-priority clustered anchor's detail sheet

## Edge Indicators
- Labels beyond ±60° FOV but within 200m get `EdgeIndicator`
- Shows directional arrow (◀ or ▶) + distance
- Color-coded by risk level

## Parameters
| Parameter | Value |
|---|---|
| FOV half-angle | 60° |
| Max visible labels | 5 |
| Max process labels | 8 |
| Horizontal overlap threshold | 0.25 (normalized) |
| Vertical overlap threshold | 0.12 (normalized) |
| Vertical push per collision | 0.12 |
| Max push attempts | 3 |
| Max Y before clustering | 0.85 |
| Edge indicator max distance | 200m |
