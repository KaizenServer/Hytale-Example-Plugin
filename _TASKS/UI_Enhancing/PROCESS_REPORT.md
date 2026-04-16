# PROCESS_REPORT — UI Enhancing

## Task Overview

**Goal:** Improve the UI for class and talent selection.
**Folder:** `_TASKS/UI_Enhancing/`
**Status:** Awaiting user enhancement requirements.

## Scope

| Layer | Files |
|---|---|
| UI layouts | `src/main/resources/Common/UI/Custom/ClassSelectionPage.ui` |
| | `src/main/resources/Common/UI/Custom/TalentTreePage.ui` |
| | `src/main/resources/Common/UI/Custom/XpProgressHud.ui` |
| Java UI pages | `src/main/java/.../infrastructure/ui/ClassSelectionPage.java` |
| | `src/main/java/.../infrastructure/ui/TalentTreePage.java` |
| | `src/main/java/.../infrastructure/ui/HytaleUiPresenter.java` |
| | `src/main/java/.../infrastructure/ui/XpProgressHud.java` |

## Current UI State

### ClassSelectionPage
- 4-card horizontal layout (920×440)
- Each card: Title, Role, Description, Choose button
- Dark blue theme (`#0d0d1a` / `#1a1a2e`)
- Status line at bottom

### TalentTreePage
- 16-node tree across 5 tiers (728×488)
- Each node: Name, Rank (current/max), Level requirement, Button
- Left-click: unlock rank | Right-click: remove rank
- Header shows class name + available talent points

### XpProgressHud
- Persistent HUD showing `Lv. X` and `X / Y XP`
- Text-only (no interactive elements)

## Key Constraints (from CLAUDE_BIBLE)

- All UI element IDs must be globally unique — Hytale has no descendant selector support.
- Only `.Text` properties can be set at runtime via `cmd.set()`. Layout (Anchor, Background) is baked into the `.ui` file.
- `.ui` files live in `Common/UI/Custom/` and are referenced by filename only.

## Sessions

| Date | Summary |
|---|---|
| 2026-04-12 | Task kickoff. Tracking files created. Awaiting user requirements. |
