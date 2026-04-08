# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

---

## STEP 0 — Read these files before starting any work

Read in this order. Do not skip any.

1. `problem_solving/CLAUDE_BIBLE.md` - rules derived from real mistakes. Apply them actively.
2. `problem_solving/SESSION_PRIMER.md` - how we handle problems and verification.
3. List the contents of `claude_methodologies/` - know what methodologies exist.
4. Check `Ejemplos/` - to know if they exist other real examples to understand how the sistem works, also check if there is an API.md.

**Do not respond to the user's request until you have read all three.**

---

## Project Overview

This is a **Hytale server plugin template** built with Java and Gradle. Plugins extend the Hytale game server with custom commands, items, blocks, and other content.

## Build Commands

```bash
./gradlew build          # Compile and package the plugin JAR
./gradlew runServer      # Run the development server (also syncs assets on stop)
./gradlew syncAssets     # Manually sync modified assets from build output back to src
```

Java 25 is required. The Gradle wrapper is at version 9.2.1.

## Custom Game Directory (Optional)

If the Hytale game is not in the default install location, create `%USERPROFILE%/.gradle/gradle.properties` with:

```properties
hytale.install_dir=path/to/Hytale
hytale.decompile_partial=true   # optional: faster decompilation on slow machines
```

## Architecture

### Plugin Metadata

Plugin identity is declared in [gradle.properties](gradle.properties) (`plugin_group`, `plugin_main_entrypoint`, `plugin_description`, `plugin_author`) and injected at build time into [src/main/resources/manifest.json](src/main/resources/manifest.json) via Gradle property expansion. The `manifest.json` should never be hard-coded — always edit `gradle.properties` for metadata changes.

### Plugin Entry Point

[ExamplePlugin.java](src/main/java/com/example/exampleplugin/ExamplePlugin.java) extends `JavaPlugin` and is the lifecycle entry point. Commands and other registrations happen in the `setup()` method.

### Commands

Commands extend `CommandBase` and are registered in the plugin entry point. See [ExampleCommand.java](src/main/java/com/example/exampleplugin/ExampleCommand.java) for the pattern — override `execute()` and set the command name/permission level.

### Assets

Assets live under `src/main/resources/` split into two trees:
- `Common/` — client-side assets (textures, icons, UI)
- `Server/` — server-side data (item/block JSON definitions, language files)

The `syncAssets` Gradle task copies modified assets from the game's runtime build output back into `src/main/resources/`, making it easy to iterate on assets in-game. `manifest.json` is excluded from this sync.

### Localization

String keys are defined in [src/main/resources/Server/Languages/en-US/server.lang](src/main/resources/Server/Languages/en-US/server.lang).

## No Tests

There is no test infrastructure in this project.

### Custom Orders

1. There's a custom folder called "DOCUMENTATION" which contains all the documentation required to make a Hytale plugin.

2. Don't search for results on internet unless it's completely necessary.

---

## WHAT COUNTS AS A TASK

Every one of these is a task and requires the full task_kickoff protocol:

- Research or investigation (even if it looks like a question)
- Generating a prompt, template, or document
- Analysing a file or document
- Writing or modifying code
- Any request that produces an output or modifies a file
- Any question that requires synthesis, comparison, or multi-step reasoning

**If you are unsure, treat it as a task.**

A task is NOT: a clarification question, a yes/no factual lookup, a direct file read with no synthesis.

---

## PROTOCOL FOR EVERY TASK

Follow `claude_methodologies/task_kickoff/PROMPT.md`. In brief:

1. **Understand** - confirm what the goal is, where the work happens, what the output is
2. **Check** - does a `PROCESS_REPORT.md` / `STATUS_LOG.md` already exist in the source folder?
   - Yes → continuation: read both, report last known state, resume
   - No → new task: proceed to step 3
3. **Track** - create `PROCESS_REPORT.md` and `STATUS_LOG.md` in the source folder before executing anything
4. **Plan** - present the approach to the user. Do not execute until they approve.
5. **Execute** - update `STATUS_LOG.md` at each milestone
6. **Log mistakes immediately** - wrong assumption or misdiagnosis → add to `CLAUDE_BIBLE.md` now, not at end of session
7. **Close** - apply `claude_methodologies/session_closure/PROMPT.md` at session end
8. **Log usage** - append one row to the methodology's `USAGE_LOG.md`

**Steps 1-3 happen before any code or commands are run.**
**Step 4 requires user approval before step 5 begins.**

---

## SAFETY RULES (from CLAUDE_BIBLE — critical ones inline)

- **Check before declaring.** Never say a file is missing, broken, or incomplete without verifying it first. `ls` it.
- **Diagnose before fixing.** Read the full error. Trace it to the exact line. A fix on the wrong line is worse than no fix.
- **Never use agents to read local files on Windows.** Agents run bash internally and trigger Microsoft Store prompts for Unix commands. Use the Read tool or PowerShell directly.
- **Verify agents independently.** After an agent completes a batch task, check the output yourself - do not trust the agent's self-reported status.

Full rules with context: `problem_solving/CLAUDE_BIBLE.md`

---

## SESSION CLOSURE

When the session is ending or the task is complete:

Apply `claude_methodologies/session_closure/PROMPT.md` - all 8 steps, in order. Do not skip any.

The 8 steps are:
1. Self-report (before looking at folders)
2. Task genealogy (parent vs sub-task)
3. Folder inspection
4. Cross-check self-report vs folders
5. Methodology identification
6. Documentation (PROCESS_REPORT + STATUS_LOG)
7. Problem log (CLAUDE_BIBLE updates)
8. Methodology usage logs

---

## AVAILABLE METHODOLOGIES

| Methodology | When to use |
|---|---|
| `task_kickoff/` | Start of every task |
| `session_closure/` | End of every session |
| `web_research/` | Intensive web research with multi-query synthesis |
| `chat_rescuing/` | Recovering from a crashed or interrupted session |
