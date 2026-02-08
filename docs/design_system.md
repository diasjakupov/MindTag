# MindTag Design System

Comprehensive design system extracted from 8 reference screens. All values are derived from the HTML/CSS source code and verified against the visual screenshots.

---

## 1. Color Palette

### 1.1 Core Theme Colors (Consistent Across All 8 Screens)

| Token                | Hex Value   | Usage                                      |
|----------------------|-------------|---------------------------------------------|
| `primary`            | `#135BEC`   | Primary action, active nav, links, accents  |
| `primary-dark`       | `#0F4BC4`   | Pressed/hover state variant of primary      |
| `background-dark`    | `#101622`   | Main app background (dark mode)             |
| `background-light`   | `#F6F6F8`   | Main app background (light mode)            |
| `surface-dark`       | `#1C2333`   | Cards, bottom sheets, elevated surfaces     |
| `surface-dark-alt`   | `#1E2736`   | Variant used in results screen              |
| `surface-dark-alt2`  | `#1F2937`   | Variant used in note details                |
| `surface-light`      | `#FFFFFF`   | Cards, elevated surfaces (light mode)       |
| `card-dark`          | `#192233`   | Card backgrounds (home, practice center)    |
| `card-light`         | `#FFFFFF`   | Card backgrounds (light mode)               |

### 1.2 Text Colors

| Token                | Value                | Usage                            |
|----------------------|----------------------|----------------------------------|
| `text-primary`       | `#FFFFFF`            | Headings, primary text (dark)    |
| `text-primary-light` | `#111418` / slate-900| Headings, primary text (light)   |
| `text-secondary`     | `#92A4C9`            | Muted text, descriptions, labels |
| `text-tertiary`      | slate-400 / gray-400 | Timestamps, hints, captions      |
| `text-slate-300`     | slate-300             | Body text in content areas       |
| `text-slate-500`     | slate-500             | Subtle metadata, descriptions    |

### 1.3 Semantic/Status Colors

| Token              | Hex Value   | Usage                                     |
|--------------------|-------------|-------------------------------------------|
| `success`          | green-500   | Correct answers, completed items          |
| `success-bg`       | green-500/10| Background tint for success indicators    |
| `error`            | red-500     | Wrong answers, error states               |
| `error-bg`         | red-500/10  | Background tint for errors                |
| `warning/streak`   | orange-500  | Fire streak icon, bolt icon               |
| `warning-bg`       | orange-900/30 (dark) / orange-100 (light) | Category icon bg |
| `info`             | blue-500    | Time stat icon                            |
| `info-bg`          | blue-500/10 | Background for info indicators            |
| `accent-purple`    | purple-500  | XP stat, AI-generated labels              |
| `accent-purple-bg` | purple-500/10 (dark) / purple-100 (light) | Purple category bg |
| `accent-teal`      | teal-600 (light) / teal-400 (dark)| Related note category |
| `progress-yellow`  | yellow-500  | Progress bar (45% review)                 |
| `progress-red`     | red-500     | Progress bar (20% review)                 |

### 1.4 Surface & Border Colors

| Token              | Value               | Usage                                 |
|--------------------|----------------------|---------------------------------------|
| `border-subtle`    | `white/5`            | Card borders in dark mode             |
| `border-medium`    | slate-800 / gray-800 | Prominent borders, nav border         |
| `border-light`     | slate-200 / gray-100 | Light mode borders                    |
| `divider`          | `white/10`           | Section dividers                      |
| `overlay-bg`       | `black/40` - `black/60` | Image overlays, backdrops        |
| `inactive-dot`     | `#324467`            | Onboarding step dots (inactive)       |

### 1.5 Gradients

| Name                    | Value                                             | Usage                    |
|-------------------------|----------------------------------------------------|--------------------------|
| `hero-gradient`         | `from-primary/80 to-blue-900/80`                  | "Up Next" banner overlay |
| `ai-insight-gradient`   | `from-primary to-purple-600`                      | AI Insight badge         |
| `footer-fade`           | `from-background-dark via-background-dark to-transparent` | Sticky footer fade |
| `card-content-gradient` | `from-rgba(16,22,34,0.95) 20% to rgba(16,22,34,0.6) 100%` | Graph preview card |
| `primary-shadow`        | `shadow-primary/25` or `shadow-primary/20`        | Primary button glow      |

### 1.6 Graph Visualization Colors

| Element              | Value               | Usage                           |
|----------------------|----------------------|---------------------------------|
| `node-bg`            | slate-800            | Default graph node background   |
| `node-border`        | slate-700 / slate-600| Default node border             |
| `node-selected-border`| `#135BEC` (primary) | Selected/active node            |
| `node-selected-glow` | `primary/30` blur    | Glow effect on selected node    |
| `edge-default`       | `#334155` (slate-700)| Graph connection lines          |
| `edge-active`        | `#135BEC` 0.8 opacity| Active connection line          |
| `edge-weak`          | `#334155` dashed     | Weak/cross-subject connections  |
| `graph-bg`           | `#0F1115`            | Graph canvas background         |
| `graph-grid`         | `#334155` at 20% opacity | Dot grid pattern           |

---

## 2. Typography

### 2.1 Font Families

| Token     | Family                       | Usage                          |
|-----------|------------------------------|--------------------------------|
| `display` | **Lexend** (wght 100-900)    | All headings, UI labels, buttons |
| `body`    | **Lexend Deca** / **Noto Sans** | Body text in note content   |

**Compose Mapping**: Use `Lexend` as the single font family. `Lexend Deca` is a variant of Lexend with slightly different character widths -- can use regular Lexend for simplicity.

### 2.2 Type Scale

| Style Name           | Size (px -> sp) | Weight    | Line Height   | Tracking       | Usage                                   |
|----------------------|-----------------|-----------|---------------|----------------|-----------------------------------------|
| `displayLarge`       | 48px -> 48sp    | Bold (700)| tight         | tighter        | Score percentage (80%)                  |
| `headlineLarge`      | 30px -> 30sp    | Bold (700)| tight         | tight          | Greeting "Good morning, Alex"           |
| `headlineMedium`     | 26px -> 26sp    | Bold (700)| tight         | tight          | Quiz question text                      |
| `headlineSmall`      | 24px -> 24sp    | Bold (700)| tight         | tight          | Section titles ("Principles of Scarcity") |
| `titleLarge`         | 20px -> 20sp    | Bold (700)| tight         | normal         | Card titles ("Quick Quiz", section h2)  |
| `titleMedium`        | 18px -> 18sp    | Bold (700)| tight         | -0.015em       | Top bar title ("Practice Center")       |
| `titleSmall`         | 16px -> 16sp    | Semi-bold (600) | normal  | normal         | Card headings, week titles              |
| `bodyLarge`          | 16px -> 16sp    | Normal (400) | relaxed   | normal         | Note body text, descriptions            |
| `bodyMedium`         | 15px -> 15sp    | Medium (500) | normal    | normal         | Quiz option text                        |
| `bodySmall`          | 14px -> 14sp    | Medium (500) | normal    | normal         | Task names, subtitles                   |
| `labelLarge`         | 14px -> 14sp    | Bold (700)| normal        | normal         | Button text                             |
| `labelMedium`        | 12px -> 12sp    | Semi-bold (600) | normal  | wider          | Chip text, metadata                     |
| `labelSmall`         | 10px -> 10sp    | Bold (700)| normal        | wider/uppercase| Category labels ("ACTIVE RECALL")       |
| `caption`            | 10px -> 10sp    | Medium (500) | normal    | normal         | Bottom nav labels, figure captions      |

### 2.3 Special Text Styles

- **Uppercase tracking-wider**: Used for category labels (e.g., "ACTIVE RECALL", "SIMULATION", "WEEK 1", "YOUR ANSWER", "CORRECT ANSWER")
- **Tabular numbers**: Used for timer display (`tabular-nums` class)
- **Highlighted inline text**: `bg-primary/10 text-primary rounded px-1` for inline concept highlighting in notes

---

## 3. Spacing System

### 3.1 Base Scale (4dp grid)

All spacing values observed across the 8 screens follow a **4dp base grid**:

| Token  | Value | Usage Examples                                        |
|--------|-------|-------------------------------------------------------|
| `xxs`  | 2dp   | Tiny gaps (icon-text in very compact areas)           |
| `xs`   | 4dp   | Inner padding, tight gaps                             |
| `sm`   | 6dp   | Small gaps between related items                      |
| `md`   | 8dp   | Gap between chip items, small card padding            |
| `lg`   | 12dp  | Section padding, card content gap                     |
| `xl`   | 16dp  | Standard horizontal padding (px-4), card padding      |
| `2xl`  | 20dp  | Larger padding (px-5), quiz content padding           |
| `3xl`  | 24dp  | Section spacing, larger gaps                          |
| `4xl`  | 32dp  | Large section spacing, top padding                    |

### 3.2 Common Layout Spacing

| Pattern                 | Value     | Usage                                 |
|-------------------------|-----------|---------------------------------------|
| Screen horizontal padding| 16dp (px-4) | Standard content inset             |
| Quiz horizontal padding | 20dp (px-5) | Quiz/exam content inset            |
| Card internal padding   | 12-20dp   | Card content area                     |
| Bottom nav height       | 64dp (h-16) | Navigation bar height              |
| Bottom nav padding      | 80dp total | With safe area padding              |
| Top app bar height      | 64dp (h-16) | Standard toolbar height            |
| Bottom content padding  | 96dp (pb-24) | Space above bottom nav            |
| Section gap             | 16-24dp   | Between major content sections        |
| Card gap                | 12-16dp   | Between cards in a list               |
| FAB size                | 56dp (size-14) | Floating action button diameter  |
| Avatar size             | 40dp (size-10)| User avatar                        |
| Icon button size        | 40dp (size-10)| Top bar icon buttons               |

---

## 4. Border Radius

| Token     | Value  | dp Equivalent | Usage                                      |
|-----------|--------|---------------|---------------------------------------------|
| `none`    | 0      | 0dp           | N/A                                         |
| `default` | 4dp    | 0.25rem       | Small inner elements, tags                  |
| `sm`      | 6dp    | 0.375rem      | Default radius (home dashboard config)      |
| `md`      | 8dp    | 0.5rem        | Search bar, segmented controls, buttons     |
| `lg`      | 12dp   | 0.75rem       | Cards, quiz options, input fields           |
| `xl`      | 16dp   | 1rem          | Large cards, bottom sheets, week containers |
| `2xl`     | 16dp   | 1rem          | Action cards, stat cards                    |
| `full`    | 9999px | circular      | Avatars, pills, FABs, radio buttons, chips  |

**Primary card radius**: `rounded-xl` (12dp) is the dominant card shape.
**Primary button radius**: `rounded-xl` (12dp) for large buttons, `rounded-lg` (8dp) for medium buttons, `rounded-full` for pill-shaped buttons.

---

## 5. Elevation / Shadows

| Level   | CSS Value                                       | Material 3 Mapping | Usage                          |
|---------|--------------------------------------------------|--------------------|--------------------------------|
| Level 0 | none                                            | Elevation 0        | Flat content                   |
| Level 1 | `shadow-sm` + `ring-1 ring-white/10`           | Elevation 1        | Cards, list items              |
| Level 2 | `shadow-lg`                                     | Elevation 2        | Review cards, FAB              |
| Level 3 | `shadow-xl shadow-primary/20`                   | Elevation 3        | Primary buttons                |
| Level 4 | `shadow-2xl`                                    | Elevation 4        | Bottom sheets, overlays        |
| Special | `shadow-[0_0_15px_rgba(19,91,236,0.5)]`        | Custom glow        | Selected graph node            |
| Special | `shadow-[0_-4px_20px_-4px_rgba(0,0,0,0.1)]`   | Custom             | Bottom sheet upward shadow     |
| Special | `shadow-[0_4px_20px_-12px_rgba(0,0,0,0.5)]`   | Custom             | Search area downward shadow    |

**Dark mode note**: Cards primarily use `ring-1 ring-white/5` or `border border-white/5` for subtle elevation indication rather than drop shadows, which is more visible in dark themes.

---

## 6. Component Catalog

### 6.1 Bottom Navigation Bar

**Screens**: All 8 (varies slightly in tab count)

| Property          | Value                                    |
|-------------------|-------------------------------------------|
| Height            | 64dp (h-16) + safe area padding           |
| Background        | `#111722/95` with `backdrop-blur-md`      |
| Border top        | `border-white/5`                          |
| Item width        | 56dp (w-14) per tab                       |
| Icon size         | 26px Material Symbols Outlined            |
| Label size        | 10sp, font-medium                         |
| Active color      | `#135BEC` (primary)                       |
| Inactive color    | `#92A4C9` (text-secondary)                |
| Active indicator  | None (color only) or pill bg on Practice  |

**Tab variations observed**:
- Home dashboard: Home, Graph, Notes, Plan, Profile (5 tabs)
- Library/Graph: Home, Library, Plan, Profile (4 tabs)
- Practice Center: Home, Graph, Practice, Profile (4 tabs)
- Planner: Home, Planner, Graph, Profile (4 tabs)

**Recommended unified tabs**: Home, Library/Notes, Practice, Plan, Profile (5 tabs)

### 6.2 Top App Bar

| Property          | Value                                    |
|-------------------|-------------------------------------------|
| Height            | 64dp                                     |
| Background        | `background-dark/95` with `backdrop-blur-sm` |
| Title style       | 18sp, Bold, centered                     |
| Nav icon          | 40dp touch target, `arrow_back` or `close`|
| Action icon       | 40dp touch target, contextual             |
| Sticky behavior   | `sticky top-0 z-50` with blur effect     |

**Variants**:
- **Home**: No top bar, inline greeting header
- **Detail screens**: Back arrow + centered title + action
- **Exam mode**: Close button + centered timer + spacer
- **Results**: Close button + centered title

### 6.3 Cards

#### Review Card (Home Dashboard)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Width         | 280dp, horizontal scroll                      |
| Background    | `card-dark` (#192233)                         |
| Border        | `border-white/5`                              |
| Radius        | 12dp (rounded-xl)                             |
| Padding       | 12dp                                          |
| Image         | aspect-video, rounded-lg, with overlay        |
| Subject tag   | `bg-black/60 backdrop-blur-sm` positioned top-right |
| Progress bar  | h-1.5, bg-white/10 track, colored fill        |
| Action button | h-10, bg-primary, rounded-lg, full width      |

#### Action Card (Practice Center)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `card-dark` (#192233)                         |
| Border        | `ring-1 ring-slate-800`                       |
| Radius        | 16dp (rounded-2xl)                            |
| Padding       | 20dp                                          |
| Layout        | Row: content + 96dp square thumbnail          |
| Thumbnail     | 96x96dp, rounded-xl                           |
| Category icon | 32dp circle, colored bg                       |
| Label         | 10sp uppercase bold tracking-wider            |
| Title         | 20sp bold                                     |
| Button        | h-9, rounded-lg, secondary or primary variant |

#### Week Card (Planner)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `surface-dark` (#1C2333)                      |
| Border        | `border-gray-800`                             |
| Radius        | 12dp (rounded-xl)                             |
| Week label    | 10sp bold primary, `bg-primary/10` pill       |
| Title         | 16sp semi-bold                                |
| Progress bar  | h-1, w-24 (96dp), colored fill                |
| Expand icon   | `expand_more`, rotates 180deg on open         |
| Item row      | 12dp padding, 40dp icon container, rounded-lg |

#### Related Note Card (Note Details)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Width         | 160dp (w-40)                                  |
| Height        | 128dp (h-32)                                  |
| Background    | `#151B26` (dark) / slate-50 (light)           |
| Border        | `border-slate-700`                            |
| Radius        | 12dp (rounded-xl)                             |
| Padding       | 12dp                                          |
| Category icon | colored bg, 20px icon, rounded-lg             |
| Category text | 12sp text-slate-500                           |
| Title         | 14sp semi-bold, line-clamp-2                  |

#### Stat Card (Quiz Results)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Layout        | 3-column grid                                 |
| Background    | `surface-dark`                                |
| Border        | `border-slate-800`                            |
| Radius        | 16dp (rounded-2xl)                            |
| Padding       | 12dp                                          |
| Icon          | 20px in colored circle bg                     |
| Label         | 12sp uppercase tracking-wider slate-400       |
| Value         | 14sp bold                                     |

### 6.4 Buttons

#### Primary Button (Large/Full Width)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 56dp (h-14)                                   |
| Background    | `#135BEC`                                     |
| Text          | 18sp bold white                               |
| Radius        | 12dp (rounded-xl)                             |
| Shadow        | `shadow-lg shadow-primary/25`                 |
| Active        | `scale-[0.98]` transform                      |
| Hover         | `bg-primary/90`                               |

#### Primary Button (Medium)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 40dp (h-10)                                   |
| Background    | `#135BEC`                                     |
| Text          | 14sp bold white                               |
| Radius        | 8dp (rounded-lg)                              |
| Shadow        | `shadow-lg shadow-blue-900/20`                |

#### Secondary Button (Outlined/Surface)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 36dp (h-9)                                    |
| Background    | `slate-100 dark:slate-800`                    |
| Text          | 14sp semi-bold                                |
| Radius        | 8dp (rounded-lg)                              |

#### Pill Button (Quiz Me)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 40dp (h-10)                                   |
| Background    | `#135BEC`                                     |
| Text          | 14sp bold white                               |
| Radius        | full (pill shape)                             |
| Max width     | 200dp                                        |
| Shadow        | `shadow-lg shadow-primary/20`                 |

#### FAB (Floating Action Button)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Size          | 56dp (size-14) circle                         |
| Background    | `#135BEC`                                     |
| Icon          | 30px (text-3xl) `add`                         |
| Shadow        | `shadow-lg`                                   |
| Hover         | `bg-blue-600`                                 |

#### Extended FAB (Add Syllabus)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 56dp (h-14)                                   |
| Background    | `#135BEC`                                     |
| Radius        | full (pill)                                   |
| Icon          | 24px `add`                                    |
| Text          | 14sp bold                                     |
| Shadow        | `shadow-xl shadow-blue-500/20`                |

### 6.5 Input Fields

#### Search Bar
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 48dp (h-12)                                   |
| Background    | `#232F48` (dark)                              |
| Border        | none                                          |
| Radius        | 8dp (rounded-lg)                              |
| Icon          | 24px `search`, slate-400 color                |
| Placeholder   | 16sp normal, `#92A4C9`                        |
| Focus         | No ring, no outline                           |

### 6.6 Segmented Control (Toggle)

| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 40dp (h-10)                                   |
| Background    | `#232F48` (dark) / slate-200 (light)          |
| Padding       | 4dp (p-1) internal                            |
| Radius        | 8dp (rounded-lg) outer, 6dp (rounded-md) inner|
| Active bg     | `#111722` (dark) / white (light) + shadow-sm  |
| Active text   | white (dark) / primary (light), 14sp medium   |
| Inactive text | `#92A4C9`, 14sp medium                        |

### 6.7 Progress Indicators

#### Linear Progress Bar (Thin)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 6dp (h-1.5) or 4dp (h-1)                     |
| Track         | `bg-white/10` or slate-700                    |
| Fill          | primary, yellow-500, or red-500               |
| Radius        | full (rounded-full)                           |

#### Quiz Progress Bar
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Height        | 8dp (h-2)                                     |
| Track         | `#324467`                                     |
| Fill          | primary with transition animation             |
| Radius        | full (rounded-full)                           |

#### Score Ring (Results)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Size          | 160dp (w-40 h-40)                             |
| Border width  | 6dp                                           |
| Track         | `primary/20`                                  |
| Fill          | primary (partial arc using clip-path)         |
| Center text   | 48sp bold                                     |

#### Bar Chart (Weekly Performance)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Bar width     | flex-1 (equal distribution)                   |
| Bar bg        | `slate-700/30`                                |
| Bar fill      | primary at varying opacity (30%-100%)         |
| Label         | 10sp uppercase medium                         |
| Active day    | full primary, bold label in primary color     |
| Height        | 128dp (h-32) container                        |

### 6.8 Chips / Tags

#### Subject Tag
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `bg-black/60 backdrop-blur-sm` (on image)     |
| Text          | 12sp medium white                             |
| Padding       | 8dp h, 2dp v (px-2 py-0.5)                   |
| Radius        | 4dp (rounded)                                 |

#### Metadata Chip
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `#232F48` (dark) / slate-200 (light)          |
| Text          | 12sp semi-bold uppercase                      |
| Padding       | 12dp h, 4dp v (px-3 py-1)                    |
| Radius        | 8dp (rounded-lg)                              |

#### Status Chip (Banner)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `white/20 backdrop-blur-md`                   |
| Text          | 12sp medium white                             |
| Padding       | 8dp h, 4dp v                                  |
| Radius        | 4dp (rounded)                                 |

#### Week Label Pill
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `primary/10` (active) / `white/5` (inactive)  |
| Text          | 10sp bold uppercase tracking-wider            |
| Color         | primary (active) / gray-500 (inactive)        |
| Padding       | 8dp h, 2dp v                                  |
| Radius        | full                                          |

### 6.9 Quiz Answer Options

| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | transparent (dark) / white (light)            |
| Border        | 2dp `#324467` (dark) / slate-200 (light)      |
| Radius        | 12dp (rounded-xl)                             |
| Padding       | 18dp all sides                                |
| Selected ring | 2dp primary                                   |
| Selected bg   | `primary/10` (dark) / `primary/5` (light)     |
| Radio circle  | 20dp, 2dp border, slate-500                   |
| Radio filled  | primary bg, 8dp white dot inside              |
| Text          | 15sp medium                                   |
| Active scale  | `scale-[0.99]`                                |

### 6.10 Question Review Cards (Results)

#### Correct (Collapsed)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `surface-dark`                                |
| Border        | `border-slate-800/50`                         |
| Radius        | 12dp (rounded-xl)                             |
| Padding       | 16dp                                          |
| Status icon   | 24dp circle, green-500/10 bg, green check     |
| Expand icon   | `expand_more` in slate-400                    |

#### Incorrect (Expanded)
| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Border        | `ring-1 ring-red-500/20`                      |
| Answer bg     | `red-50 dark:red-900/10` with red border      |
| Correct bg    | `green-50 dark:green-900/10` with green border|
| AI Insight    | gradient badge, `primary/5 to purple-500/5` bg|

### 6.11 Bottom Sheet

| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `surface-dark`                                |
| Top border    | `border-slate-800`                            |
| Radius        | 16dp (rounded-t-2xl) top corners only         |
| Drag handle   | 48dp wide, 4dp tall, slate-600, rounded-full  |
| Shadow        | `shadow-[0_-4px_20px_-4px_rgba(0,0,0,0.1)]`  |
| Padding       | 20dp horizontal, 8dp top (above handle)       |

### 6.12 Onboarding Page Indicators

| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Inactive dot  | 6dp circle, `#324467`                         |
| Active dot    | 6dp h, 32dp w (pill), primary color           |
| Gap           | 8dp between dots                              |

### 6.13 Timer Badge (Exam Mode)

| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `#1C2533` (dark) / white (light)              |
| Border        | `border-slate-800`                            |
| Radius        | full (pill)                                   |
| Padding       | 20dp h, 8dp v                                 |
| Icon          | 20px `timer` filled, primary                  |
| Text          | 20sp bold, tabular-nums                       |
| Shadow        | shadow-sm                                     |

### 6.14 Task List Item

| Property      | Value                                        |
|---------------|-----------------------------------------------|
| Background    | `card-dark` (#192233)                         |
| Border        | `border-white/5`                              |
| Radius        | 8dp (rounded-lg)                              |
| Padding       | 12dp                                          |
| Checkbox      | 24dp, 2dp border, `text-secondary/50`         |
| Title         | 14sp medium white                             |
| Subtitle      | 12sp text-secondary                           |
| Duration badge| `bg-background-dark` px-2 py-1, 12sp          |
| Locked state  | 60% opacity, lock icon in checkbox            |

### 6.15 Graph Nodes

#### Center/Primary Node
| Size  | Border      | Text           | Background |
|-------|-------------|----------------|------------|
| 80dp  | 4dp slate-700 | 12sp bold white | slate-800  |

#### Standard Node
| Size  | Border      | Text              | Background |
|-------|-------------|-------------------|------------|
| 56dp  | 2dp slate-600 | 10sp slate-300  | slate-800  |

#### Distant/Faded Node
| Size  | Border       | Text              | Background |
|-------|--------------|-------------------|------------|
| 48dp  | 1dp slate-700| 10sp slate-500    | `#1E293B`  |

#### Selected Node
| Size  | Border       | Text            | Background | Special           |
|-------|--------------|-----------------|------------|-------------------|
| 64dp  | 2dp primary  | 12sp bold white | slate-900  | Glow: primary/30 blur |

---

## 7. Icon System

**Icon Library**: Material Symbols Outlined (Google Fonts)
**Icon Weight**: Variable 100-700, FILL 0-1
**Default Size**: 24px
**Small Size**: 18-20px
**Large Size**: 26-28px

### Icons Used Across Screens

| Icon Name              | Usage                         |
|------------------------|-------------------------------|
| `home`                 | Home nav tab                  |
| `hub`                  | Graph nav tab                 |
| `description`          | Notes nav tab                 |
| `calendar_month`       | Plan nav tab                  |
| `person`               | Profile nav tab               |
| `local_library`        | Library nav tab               |
| `edit_note`            | Practice nav tab              |
| `settings`             | Settings action               |
| `arrow_back`           | Back navigation               |
| `arrow_back_ios_new`   | Back navigation (iOS style)   |
| `close`                | Dismiss/exit                  |
| `filter_list`          | Filter action                 |
| `search`               | Search                        |
| `add`                  | Create new / FAB              |
| `more_horiz`           | More options                  |
| `more_vert`            | More options vertical         |
| `share`                | Share action                  |
| `auto_awesome`         | AI indicator                  |
| `history_edu`          | Review action                 |
| `lightbulb`            | Quiz me action                |
| `headphones`           | Audio/listen                  |
| `cloud_upload`         | Upload action                 |
| `upload_file`          | Upload indicator              |
| `lock`                 | Locked content                |
| `lock_clock`           | Timed lock                    |
| `timer`                | Exam timer                    |
| `electric_bolt`        | Quick action / energy         |
| `bolt`                 | XP indicator                  |
| `school`               | Education/exam                |
| `local_fire_department`| Streak indicator              |
| `schedule`             | Time stat                     |
| `analytics`            | Analysis section              |
| `check`                | Correct answer                |
| `check_circle`         | Completed task                |
| `expand_more`          | Expandable section            |
| `chevron_right`        | Navigate forward              |
| `arrow_forward`        | Next action                   |
| `play_arrow`           | Start quiz                    |
| `article`              | Reading material              |
| `psychology`           | AI quiz                       |
| `show_chart`           | Economics category            |
| `balance`              | Decision making category      |
| `public`               | Global category               |
| `priority_high`        | Important/warning             |

---

## 8. Screen Inventory & User Flows

### 8.1 Home Dashboard
- **Purpose**: Primary landing screen showing personalized study overview
- **Key sections**: Greeting + AI status, Due for Review carousel, Up Next (current syllabus focus + task list)
- **Interactions**: Horizontal scroll on review cards, tap to review, tap task items, bottom nav
- **Navigation**: Entry point, leads to all other screens via bottom nav or card actions

### 8.2 Onboarding: Syllabus Upload
- **Purpose**: Guide new users to upload their syllabus for AI processing
- **Key sections**: Step indicators, hero illustration, headline, supported formats, upload CTA
- **Interactions**: Upload button, skip option, step-by-step progression
- **Navigation**: Part of onboarding flow (step 2 of 4), leads to next onboarding step

### 8.3 Library: Knowledge Graph View
- **Purpose**: Visual exploration of concept connections via interactive graph
- **Key sections**: Search bar, List/Graph toggle, interactive node graph, selected node preview card
- **Interactions**: Pan/zoom graph, tap nodes, search by concept, toggle view mode, FAB to add
- **Navigation**: Accessible from bottom nav "Library/Graph" tab

### 8.4 Note Details & Connections
- **Purpose**: Read full note content with semantic connections
- **Key sections**: Toolbar (listen, quiz me), metadata chips, note body (rich text + images), related notes bottom sheet
- **Interactions**: Scroll content, tap Quiz Me, expand bottom sheet, tap related note cards, tap View Graph
- **Navigation**: Reached from graph node preview, search results, or task items

### 8.5 Study Hub: Practice Center
- **Purpose**: Choose study mode and view performance
- **Key sections**: Challenge selection cards, recent performance stats with bar chart, weakest topic suggestion
- **Interactions**: Start Quick Quiz, Begin Exam, view performance details, act on suggestions
- **Navigation**: Accessible from bottom nav "Practice" tab

### 8.6 Active Exam Mode Quiz
- **Purpose**: Distraction-free timed quiz experience
- **Key sections**: Timer, progress indicator, question text, multiple-choice answers
- **Interactions**: Select answer, next question, exit exam
- **Navigation**: Entered from Practice Center, exits to Results or back to Practice

### 8.7 Quiz Performance Results
- **Purpose**: Show quiz score with detailed question-by-question analysis
- **Key sections**: Score ring, stats row (time/streak/XP), detailed analysis (expandable correct/incorrect)
- **Interactions**: Expand/collapse questions, review AI insights, review related notes
- **Navigation**: Shown after completing quiz, CTA leads to related notes in graph

### 8.8 Planner: Weekly Curriculum
- **Purpose**: View and manage syllabus-based weekly study plan
- **Key sections**: Calendar/List toggle, week cards with progress, expandable task lists
- **Interactions**: Toggle view, expand/collapse weeks, tap tasks, add syllabus FAB
- **Navigation**: Accessible from bottom nav "Plan/Planner" tab

### 8.2 Navigation Architecture

```
Bottom Navigation (persistent)
|-- Home (Dashboard)
|-- Library / Graph
|   |-- List View
|   |-- Graph View
|       |-- Node Detail -> Note Details
|-- Practice Center
|   |-- Quick Quiz -> Active Quiz -> Results
|   |-- Exam Mode -> Active Exam -> Results
|-- Planner
|   |-- Calendar View
|   |-- List View
|-- Profile

Onboarding Flow (linear, shown once):
  Step 1 -> Step 2 (Syllabus Upload) -> Step 3 -> Step 4 -> Home
```

**Navigation pattern**: Tab-based primary navigation + stack-based detail navigation.

---

## 9. Material 3 Theme Mapping (Compose)

```
// Suggested MaterialTheme color scheme (dark)
darkColorScheme(
    primary = Color(0xFF135BEC),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF324467),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF92A4C9),
    onSecondary = Color(0xFF101622),
    background = Color(0xFF101622),
    onBackground = Color.White,
    surface = Color(0xFF1C2333),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF192233),
    onSurfaceVariant = Color(0xFF92A4C9),
    outline = Color(0xFF324467),
    outlineVariant = Color(0x0DFFFFFF), // white/5
    error = Color(0xFFEF4444), // red-500
    onError = Color.White,
    tertiary = Color(0xFFF97316), // orange-500 for streaks
    onTertiary = Color.White,
)

// Typography
Typography(
    displayLarge  = TextStyle(fontFamily = Lexend, fontWeight = Bold, fontSize = 48.sp),
    headlineLarge = TextStyle(fontFamily = Lexend, fontWeight = Bold, fontSize = 30.sp),
    headlineMedium= TextStyle(fontFamily = Lexend, fontWeight = Bold, fontSize = 26.sp),
    headlineSmall = TextStyle(fontFamily = Lexend, fontWeight = Bold, fontSize = 24.sp),
    titleLarge    = TextStyle(fontFamily = Lexend, fontWeight = Bold, fontSize = 20.sp),
    titleMedium   = TextStyle(fontFamily = Lexend, fontWeight = Bold, fontSize = 18.sp),
    titleSmall    = TextStyle(fontFamily = Lexend, fontWeight = SemiBold, fontSize = 16.sp),
    bodyLarge     = TextStyle(fontFamily = Lexend, fontWeight = Normal, fontSize = 16.sp),
    bodyMedium    = TextStyle(fontFamily = Lexend, fontWeight = Medium, fontSize = 15.sp),
    bodySmall     = TextStyle(fontFamily = Lexend, fontWeight = Medium, fontSize = 14.sp),
    labelLarge    = TextStyle(fontFamily = Lexend, fontWeight = Bold, fontSize = 14.sp),
    labelMedium   = TextStyle(fontFamily = Lexend, fontWeight = SemiBold, fontSize = 12.sp),
    labelSmall    = TextStyle(fontFamily = Lexend, fontWeight = Bold, fontSize = 10.sp),
)
```

---

## 10. Design Inconsistencies & Notes

1. **Bottom nav tab count varies**: Home has 5 tabs, other screens have 4. Need to standardize.
2. **Surface color variants**: `surface-dark` ranges from `#1C2333` to `#1F2937` to `#1E2736` across screens. Should consolidate to one value.
3. **card-dark vs surface-dark**: Two similar but distinct tokens (`#192233` vs `#1C2333`). Consider merging or clarifying hierarchy.
4. **Font body fallback**: Some screens use `Noto Sans` as body, others use `Lexend Deca`, others just `Lexend`. Recommend standardizing to `Lexend` only.
5. **Border radius inconsistency**: Home dashboard uses `0.375rem` as DEFAULT, others use `0.25rem`. Minor but should pick one.
6. **Segmented control active state**: In Planner the active segment uses `bg-primary` (dark), while in Library it uses `bg-[#111722]`. Different visual treatments for same component.
7. **Icon style**: Most use outlined, but a few references (`fill-1`, `filled`) use filled variants. Establish when to use filled vs outlined.
