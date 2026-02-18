# Graph Visual Overhaul Design

**Date:** 2026-02-18
**Status:** Approved
**Scope:** `LibraryScreen.kt` (GraphView, drawNodeLabel, drawDotGrid), `LibraryViewModel.kt` (buildGraphNodes)

## Problem

The knowledge graph visualization looks flat and lifeless:
- Dark slate nodes with barely visible subject color rings
- Tiny unreadable labels crammed inside small circles
- Straight-line edges with no visual flow
- Mechanical radial layout with perfect geometric orbits
- Useless 7px indicator dot adding visual noise

## Approach

Polish the existing Canvas-based rendering with vibrant subject-colored nodes, curved edges, and external labels. No architectural changes — same `GraphView` composable, same `GraphNode`/`GraphEdge` data model, same pan/zoom interaction.

## Changes

### 1. Node Rendering

| Property | Before | After |
|----------|--------|-------|
| Fill | `NodeBg` (#1E293B) flat | Subject color @ 15% opacity |
| Ring | Subject color, 1.5px stroke | Subject color, 2px stroke |
| Inner glow | None | Subject color @ 8%, radius + 6f |
| Indicator dot | 7px colored dot at top | Removed |
| Primary radius | 44f | 52f |
| Secondary radius | 34f | 40f |
| Selected fill | Same dark + glow | Subject color @ 25% + primary outer glow 3px |

Inside the node: single monogram (first 1-2 chars), 18sp bold white.

### 2. Labels

| Property | Before | After |
|----------|--------|-------|
| Position | Inside circle | Below node |
| Font size | 11sp / 14sp | 12sp / 13sp |
| Max width | `radius * 2 - 8` | `radius * 3` |
| Lines | 2 max inside | 2 max below |
| Content inside | Full truncated title | Monogram (1-2 chars) |

### 3. Edges

| Property | Before | After |
|----------|--------|-------|
| Shape | Straight lines | Quadratic bezier curves |
| Control point | N/A | 30-50px perpendicular offset from midpoint |
| Active glow | 2-pass straight | 2-pass curved |
| ANALOGY dash | Preserved | Preserved (on path) |

### 4. Layout

| Property | Before | After |
|----------|--------|-------|
| Cluster distance | 220f | 240f |
| Orbit base radius | 80f | 90f |
| Orbit expansion | index/5 * 40f | index/4 * 45f |
| Position jitter | None | +/- 8px seeded by noteId |

## Files Modified

- `LibraryScreen.kt` — GraphView canvas drawing, drawNodeLabel, drawDotGrid
- `LibraryViewModel.kt` — buildGraphNodes layout parameters
- `Color.kt` — No new colors needed (uses subject colors dynamically)