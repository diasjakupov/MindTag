# Knowledge Graph Visual Overhaul — Design

## Goal
Make the knowledge graph bigger, more readable, and interactive with zoom/pan.

## Node Sizing
- Primary nodes (first per subject): 44f radius
- Secondary nodes: 34f radius
- Labels: 18 chars, 14sp primary / 11sp secondary
- Subject color dot: 7f radius
- Subject color ring stroke (1.5f) around each node

## Layout: Radial Cluster
- Each subject cluster gets a 120-degree sector (360/N subjects)
- Cluster center at ~250px from canvas center
- Primary node at cluster center, secondary nodes orbit at ~80px spacing
- Virtual canvas ~800x800px with absolute pixel positions
- Deterministic layout, no physics

## Zoom & Pan
- `transformable` + `graphicsLayer` for scale/translation
- Scale: 0.5x to 2.5x, initial scale fits graph to screen
- Free drag panning
- Tap coordinates inverse-transformed before hit testing

## Visual Polish
- Edge stroke: (strength * 3.5f).coerceIn(1.5f, 4f)
- Active edge glow: double-draw (thick low-opacity + crisp on top)
- Render order: edges → non-selected nodes → selected node
- Selected glow radius: +20f
- Dot grid spacing: 40f
