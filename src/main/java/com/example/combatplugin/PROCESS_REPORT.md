# PROCESS REPORT — Combat Plugin Refactor

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

## Dependencies
- Java 25, Gradle 9.2.1
- Hytale Server API (com.hypixel.hytale:Server)
- Documentation in DOCUMENTATION/ folder
- UI examples in DOCUMENTATION/Ejemplos/UI/

## Methodology Used
`claude_methodologies/task_kickoff/`
