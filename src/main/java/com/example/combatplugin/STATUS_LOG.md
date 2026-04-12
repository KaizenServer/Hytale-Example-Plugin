# STATUS LOG — Combat Plugin Refactor

| Date | Item | Progress | Note |
|------|------|----------|------|
| 2026-04-08 | Task started | 0% | Read all 73 existing files, documentation, and UI examples |
| 2026-04-08 | Domain layer | 15% | Fixed TalentEffect.CombinedEffect record field names |
| 2026-04-08 | Application layer | 25% | Fixed SetLevelUseCase spent-points bug |
| 2026-04-08 | Event systems | 40% | Removed reflection-based UUID resolution in DamageModifier/DeathSynergy/SlotSwitch systems |
| 2026-04-08 | Commands | 55% | Refactored ClassCommandCollection and TalentsCommand to accept use cases via constructor |
| 2026-04-08 | UI | 65% | Fixed ClassSelectionPage inner class name clash, corrected .ui file paths |
| 2026-04-08 | CombatPlugin.java | 85% | Rewrote setup(): ECS registration, all use cases, systems, events, commands properly wired |
| 2026-04-08 | Build test | 95% | ./gradlew build — BUILD SUCCESSFUL, 0 errors |
| 2026-04-08 | README | 100% | Created COMBAT_PLUGIN_README.md with full technical documentation |
| 2026-04-08 | Refactor terminal | 100% | HytaleUiPresenter sin fallback, /class y /talents solo UI, BUILD SUCCESSFUL |
| 2026-04-08 | v0.7.0 — fix UI keys + NPE join | 100% | EventData keys mayúscula ("Action","ClassId","TalentId"), null holder en onJoin, BUILD SUCCESSFUL 0 warnings |
| 2026-04-08 | v0.8.0 — fix .ui path | 100% | Changed UI_KEY to "UI/Custom/ClassSelectionPage.ui" |
| 2026-04-08 | v0.9.0 — fix .ui path again | 100% | Moved files to Common/Pages/, UI_KEY = "Pages/ClassSelectionPage.ui" |
| 2026-04-09 | v0.10.0 — @ComponentName wrapper | FAILED | Added @ClassSelectionPage wrapper — asset received but document still not found |
| 2026-04-09 | v0.11.0 — remove @ComponentName wrapper | DONE | Removed @ClassSelectionPage/@TalentTreePage wrappers. Files now start with Group{} directly. BUILD SUCCESSFUL. |
| 2026-04-09 | v0.12.0 — correct UI asset path | DONE | Official docs: files in Common/UI/Custom/, cmd.append("ClassSelectionPage.ui"). Moved .ui files. BUILD SUCCESSFUL. Document now found (UI/Custom/ClassSelectionPage.ui). |
| 2026-04-09 | v0.13.0 — fix null element (no descendant selectors) | FAILED | Crash persisted. Two causes identified: (1) Pages/ copies still in JAR — Hytale resolves wrong file. (2) cmd.set(id, Message) wrong API. |
| 2026-04-09 | v0.14.0 — delete Pages/ duplicates + fix .Text API | DONE | Deleted Common/Pages/*.ui from source. JAR now has only 2 .ui files. Changed all cmd.set(id, Message.raw) to cmd.set(id+".Text", string) following reference pattern. BUILD SUCCESSFUL. |
| 2026-04-09 | Session closed | PENDING TEST | v0.14.0 JAR ready. Next step: test /class in game. If crash, share log for v0.15.0 cycle. |
| 2026-04-11 | v0.18.0 — leveling system fixes | DONE | BUILD SUCCESSFUL. Persistence fixed (ProfileInitSystem), stats applied on load, MAX_LEVEL=30, XP=20+2*level |
| 2026-04-11 | v0.19.0 — file persistence + stats on login | DONE | Bug1: EcsProfileRepository.save() now writes playerdata/<uuid>.properties on every change; loadFromFile() on join. Bug2: ProfileInitSystem now watches Player component additions (fires reliably) instead of PlayerProfileComponent (never fired). Stats applied via Store+Ref when Player entity is initialised. BUILD SUCCESSFUL. |
| 2026-04-11 | v0.19.0 — Bug 2 test result | FAILED | ProfileInitSystem[Player] never fired — chicken-and-egg: entity doesn't match Player query yet when Player is being added. Bug 1 confirmed fixed. |
| 2026-04-11 | v0.20.0 — ProfileInitSystem watches EntityStatMap | PENDING TEST | Changed watched component to EntityStatMap.getComponentType() + Query.and() (no filter). EntityStatMap is proven to be initialised before PlayerReadyEvent (7 EntityStatUpdate warnings at 18:50:18.7229 in v0.19.0 log). Also hooks onComponentSet in case map is refreshed not added. BUILD SUCCESSFUL. |
| 2026-04-12 | v0.21.0 test result | FAILED | ProfileInitSystem NEVER fired — no log lines from it despite 2 player joins. Root cause: RefChangeSystem doesn't fire for built-in Hytale components regardless of which component type is watched. |
| 2026-04-12 | v0.22.0 — apply stats directly in PlayerEventListener.onJoin() | DONE | Replaced ProfileInitSystem dependency. Ref<EntityStore> from PlayerEvent.getPlayerRef() carries its Store via Ref.getStore(). Pass store+ref directly to statApplicator.applyProfileModifiers() inside onJoin(). BUILD SUCCESSFUL. |
| 2026-04-12 | v0.22.0 test result | FAILED | Stats applied without error but character panel unchanged. Root cause: addStatValue/resetStatValue only modify current stat value (like healing), not the max stat shown in the character panel. |
| 2026-04-12 | v0.23.0 — switch to putModifier / StaticModifier API | DONE | Replaced addStatValue→putModifier(idx,"combat_plugin_level_bonus",new StaticModifier(ModifierTarget.MAX, CalculationType.ADDITIVE, amount)). Replaced resetStatValue→removeModifier(idx,key). MAX target = modifies the max stat shown in character panel. BUILD SUCCESSFUL. |
| 2026-04-12 | v0.23.0 — in-game test | **CONFIRMED** | User confirmed: stats assign correctly, talent system working. Task complete. Session closed. |
