# CombatPlugin — Technical README

## Status: First Iteration / Template

Build: `./gradlew build` compiles successfully.

---

## Architecture

Hexagonal architecture with 5 layers:

```
domain/          Pure Java — no Hytale imports. Models, enums, exceptions.
application/     Services (stateless logic), Use Cases (orchestration), Ports (interfaces).
infrastructure/  Hytale-coupled code: commands, UI pages, ECS persistence, event systems, stat applicator.
data/            Class and talent definitions (data-driven, easily editable).
config/          Plugin-wide configuration record.
```

### Key design decisions
- **Immutable PlayerProfile** — every mutation returns a new instance. Thread-safe by design.
- **Use cases as central orchestrators** — commands and UI pages delegate to use cases, never to services directly.
- **IStatApplicator / ISummonAdapter / IUiPresenter** — port interfaces decouple application logic from Hytale API specifics.
- **ECS persistence** — PlayerProfileComponent serializes via BuilderCodec. In-memory cache (ConcurrentHashMap) for fast access, synced to ECS on disconnect.
- **Dual UI** — HytaleUiPresenter (interactive pages) with automatic TextFallbackUiPresenter (chat) fallback.

---

## What's implemented

| System | Status | Notes |
|--------|--------|-------|
| Player profile (PlayerProfile, ProfileService) | **Complete** | Immutable model, in-memory cache, ECS persistence |
| Progression (XP, levels, talent points) | **Complete** | Configurable formula, level-up grants points |
| Class system (4 classes, ClassService) | **Complete** | SwordMaster, Elementalist, Technocrat, Necromancer |
| Talent system (16 talents/class, TalentService) | **Complete** | Prerequisites, level requirements, multi-rank support |
| Effect system (CombatModifier, TriggeredEffect) | **Complete** | FLAT_ADD, PERCENT_ADD, PERCENT_MULTIPLY + trigger dispatch |
| ModifierService (compute active modifiers) | **Complete** | Stacks per rank, separates passive/triggered |
| Commands (/class, /talents, /xp, /level) | **Complete** | AbstractPlayerCommand with ECS access |
| Text UI fallback | **Complete** | Chat-based class/talent menus |
| Interactive UI pages (ClassSelectionPage, TalentTreePage) | **Complete** | .ui layouts + BuilderCodec event handling |
| ECS component registration | **Complete** | Wired in CombatPlugin.setup() |
| ECS systems (DamageModifier, DeathSynergy, ProfileInit) | **Complete** | Registered in CombatPlugin.setup() |
| Global events (PlayerReady, PlayerDisconnect) | **Complete** | Profile load/save lifecycle |
| Stat applicator (HytaleStatApplicator) | **Complete** | Maps StatTarget to EntityStatMap API |
| Reset (class reset, talent reset) | **Complete** | Full refund, no cost in v1 |
| Config (CombatConfig) | **Complete** | JSON seed file, hardcoded defaults for now |

---

## What's stubbed / INTEGRATION POINTs

| Integration Point | File | What's needed |
|-------------------|------|---------------|
| Damage.Source → attacker Ref | DamageModifierSystem, DeathSynergySystem | Confirm `Damage.getSource().getRef()` or equivalent |
| SpellCastEvent | StubCombatEventListener | Event class not yet confirmed in Hytale API |
| ConsumableUsedEvent | StubCombatEventListener | Event class not yet confirmed |
| HealEvent | StubCombatEventListener | Event class not yet confirmed |
| SummonCreatedEvent / SummonDestroyedEvent | StubCombatEventListener | Event class not yet confirmed |
| ShieldBlockEvent | StubCombatEventListener | Event class not yet confirmed |
| SwitchActiveSlotEvent | SlotSwitchSystem | Event class not yet confirmed |
| NPCPlugin.spawnNPC() | HytaleSummonAdapter | Import path not yet confirmed |
| CombatConfig from JSON | CombatPlugin.setup() | Wire to Hytale config loader API |
| Permission checks | CombatPermissions | Wire to PermissionsModule |
| .ui file path resolution | ClassSelectionPage, TalentTreePage | Confirm `Custom/XXX.ui` path convention |

---

## Commands

| Command | Description | Type |
|---------|-------------|------|
| `/class choose` | Open class selection UI | Player |
| `/class info [name]` | Show class details | Player |
| `/class reset` | Reset class + refund all points | Player |
| `/talents` | Open talent tree UI | Player |
| `/talents --unlock <id>` | Unlock a talent directly | Player |
| `/talents --reset` | Reset all talents | Player |
| `/xp add <amount>` | Award XP (admin) | Admin |
| `/level set <value>` | Force-set level (admin) | Admin |
| `/level get` | Show current profile stats | Player |

---

## Classes (4)

| Class | Role | Focus |
|-------|------|-------|
| Sword Master | Tank / Bruiser | Melee damage, shields, blocking, health |
| Elementalist | Damage Dealer / Healer | Spells, mana, healing, elemental damage |
| Technocrat | Support / Damage Dealer | Gadgets, consumables, cooldown reduction |
| Necromancer | Summoner / Damage Dealer | Summons, on-kill effects, dark synergies |

Each class has **16 talents** across 5 tiers (T0-T4) with prerequisites and level requirements.

---

## How to extend

### Add a new class
1. Add enum value to `CombatClass.java`
2. Create `NewClassTalents.java` in `data/` following the SwordMasterTalents pattern
3. Register in `TalentEffects.registerAll()`
4. Add definition in `DefaultClasses.register()`

### Add a new talent
1. Add `TalentDefinition` + `TalentEffect` in the class's `*Talents.java`
2. If it has triggered effects, add a handler case in the relevant ECS system (DamageModifierSystem, DeathSynergySystem, etc.)

### Add a new stat target
1. Add value to `StatTarget` enum
2. Map it in `HytaleStatApplicator.resolveStatIndex()` (or handle at event-time if virtual)

### Add a new event hook
1. Create an ECS system extending `EntityEventSystem<EntityStore, YourEvent>`
2. Register in `CombatPlugin.setup()` via `getEntityStoreRegistry().registerSystem()`
3. Dispatch triggered effects by iterating `modifierService.computeActiveTriggeredEffects()`

### Switch to database persistence
1. Implement `IProfileRepository` with your DB client
2. Replace `EcsProfileRepository` in `CombatPlugin.setup()`

---

## Next steps (recommended)

1. **Test in-game** — Run `./gradlew runServer`, verify commands and profile persistence
2. **Confirm stubbed APIs** — After `./gradlew build` decompiles Hytale JARs, search for Damage.Source, SpellCastEvent, etc.
3. **Wire permissions** — Add `CombatPermissions` checks to admin commands
4. **Add cooldown system** — TimedBuffComponent for Perfect Block, Overclock, etc.
5. **Data-driven config** — Load CombatConfig from `combat_config.json` via Hytale config API
6. **Balance pass** — Adjust talent values, XP curve, points per level
7. **Quest/achievement hooks** — Extend ProgressionService to award XP from external sources
