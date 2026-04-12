# PROCESS REPORT — Combat Plugin Refactor (sesión 2: eliminar fallback terminal)

## Problem
The user built a combat/class plugin for Hytale with 73 Java files across a hexagonal architecture.
The plugin does not compile/work. Needs a full refactor to make it functional, clean, and extensible.

## Solution
Refactor all layers of the existing code:
- Fix compilation errors and wiring issues
- Align commands, UI, and persistence with confirmed Hytale API patterns (from DOCUMENTATION/)
- Uncomment and properly wire ECS component registration, systems, and events
- Add missing .ui resource files for class selection and talent tree
- Ensure all use cases are instantiated and wired in CombatPlugin.java
- Fix logic bugs (SetLevelUseCase spent points calculation)
- Add configuration files and technical README

## Current State (v0.23.0 — 2026-04-12) — TASK COMPLETE

- BUILD SUCCESSFUL, 0 errors, 0 warnings
- UI system: `/class` and `/talents` working in-game
- Level system: XP gain, level-up, MAX_LEVEL=30, XP=20+2*level — working
- File persistence: `playerdata/<uuid>.properties` — working since v0.19.0
- **Stat system: level-based bonuses now visible in character panel** — v0.23.0
  - Root cause of prior failure: `addStatValue`/`resetStatValue` modify current value only (like healing), not the max stat displayed in the panel
  - Fix: `EntityStatMap.putModifier(statIdx, key, new StaticModifier(ModifierTarget.MAX, CalculationType.ADDITIVE, value))` — named modifier targeting MAX stat
  - `removeModifier(statIdx, key)` to reset before re-applying
- ProfileInitSystem (RefChangeSystem) confirmed dead — never fires for built-in Hytale components; stats applied in `PlayerEventListener.onJoin()` instead
- Confirmed working by user in-game test of v0.23.0

## Dependencies
- Java 25, Gradle 9.2.1
- Hytale Server API (com.hypixel.hytale:Server)
- Documentation in DOCUMENTATION/ folder
- UI examples in DOCUMENTATION/Ejemplos/UI/

## Methodology Used
`claude_methodologies/task_kickoff/`
