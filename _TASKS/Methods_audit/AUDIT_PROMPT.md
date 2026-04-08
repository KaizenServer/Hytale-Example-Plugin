# AUDIT PROMPT — Task Tracking & Chat Closer Validation

> **Paste this entire prompt into your Claude Code instance.** It is a **diagnostic-only** task. Do not let Claude modify, create, or delete anything in your project.

---

## CONTEXT

You are auditing this project to determine whether it has:

1. A **task tracking system** that automatically generates documents (e.g. `PROCESS_REPORT.md`, `STATUS_LOG.md`, `USAGE_LOG.md`) for every new task, with bidirectional traceability between a task and its outputs.
2. A **Chat Closer system** — a methodology for closing a working session that produces a self-report, task genealogy, folder cross-check, methodology identification, documentation updates, problem log entries, and usage log entries.

This is **NOT** a creation task. It is a **DIAGNOSTIC + EXPLANATION** task. The result must be **comparable** with a parallel audit done on a different project.

---

## HARD RULES (read first, do not violate)

- **READ-ONLY MODE.** Do NOT create, modify, rename, move, or delete any file or folder in this project.
- **No "fixes".** If you find a gap, report it. Do not patch it.
- **No new methodologies.** Do not propose adding new files or folders to `claude_methodologies/` or anywhere else.
- **No assumptions.** If a file exists, open it and verify. Do not infer behavior from filenames alone.
- **Do not invent.** If something does not exist, say "not found" — never describe how it "should" exist as if it does.
- **Verify independently.** If a methodology claims tracking files are created, sample at least one task folder and confirm the tracking files actually exist there.

---

## STEP 1 — LOCATE THE METHODOLOGY FOLDER

Find the project's methodology folder. Likely names: `claude_methodologies/`, `methodologies/`, `.claude/methodologies/`, `prompts/`, or referenced from a `CLAUDE.md` at the project root.

Report:
- Exact path of the methodology folder.
- Direct contents (one level only). For each entry: name, type (folder/file), and a one-line guess at purpose.
- Whether a `CLAUDE.md` exists at the project root and whether it references the methodology folder.

If no methodology folder exists at all → state this clearly and stop. The audit cannot continue.

---

## STEP 2 — TASK TRACKING AUDIT

For **each** methodology folder discovered in Step 1, check:

1. **Does it have a `PROMPT.md`?** (the methodology's instructions)
2. **Does it have a `USAGE_LOG.md`?** (the registry of when this methodology was applied)
3. **Does the `PROMPT.md` describe creating tracking documents** (e.g. `PROCESS_REPORT.md`, `STATUS_LOG.md`) inside the source folder of each task?

Then sample real task folders:

4. **Find at least 2 actual task folders** referenced from `USAGE_LOG.md` entries (or any methodology that should generate tracking).
5. For each sampled folder, report whether `PROCESS_REPORT.md` and `STATUS_LOG.md` actually exist there. Open them. Confirm they have real content, not stubs.
6. **Bidirectional traceability check:**
   - Task → output: does the tracking document name the produced files?
   - Output → task: do the produced files reference back to the task or methodology?
7. **Index/registry check:** is there a global index of all tasks somewhere? Or does each task live in isolation? Report what you find.

---

## STEP 3 — CHAT CLOSER AUDIT

Look for a methodology that closes a working session. Likely names: `session_closure/`, `chat_closer/`, `chat_closure/`, `session_end/`, or anything similar.

If found, open its `PROMPT.md` and check whether it covers these phases (any equivalent naming counts):

1. **Self-report** — Claude lists what it thinks it did, **before** looking at folders.
2. **Task genealogy** — classifying parent vs sub-tasks; tracing how each parent was born and refined.
3. **Folder inspection** — checking the actual state, ideally reading any checkpoint blocks first.
4. **Cross-check** — comparing the self-report against folder reality and flagging discrepancies.
5. **Methodology identification** — naming which methodologies were used (or emerged) this session.
6. **Documentation update** — writing/updating `PROCESS_REPORT.md` + `STATUS_LOG.md` for each parent task.
7. **Problem log update** — appending new mistakes to a "bible" / problem log.
8. **Usage log update** — appending one row per methodology used in this session.

For each phase: present / partial / missing. Quote the relevant lines from `PROMPT.md` as evidence.

If **no** Chat Closer methodology exists → state clearly: **"No Chat Closer methodology found in this project."** Do not invent one.

Also check: is there any **automation** (e.g. a `PreCompact` hook, a slash command, a keyword) that triggers checkpoint or closure behavior automatically? Look in `.claude/settings.json`, `.claude/settings.local.json`, or similar.

---

## STEP 4 — GAP REPORT

Produce a numbered list of **explicit gaps** between what this project has and what a fully-tracked methodology system would have. Use this format:

```
GAP N: <one-line description>
  Evidence: <where you looked, what you found / didn't find>
  Severity: critical / important / minor
```

Focus on functional gaps. Do not list cosmetic issues. Do not propose fixes.

---

## STEP 5 — IN-CHAT EXPLANATION (CRITICAL — output to chat, NOT to a file)

Write a structured explanation in the chat (not to any file) describing **how this project actually works** for tracking and closure. Use exactly this template so the explanation is comparable with a parallel audit:

### A. Task tracking — how it works here

- Where is the methodology folder?
- What does a new task trigger? (which files get created, where, when, by whom)
- How are milestones tracked during execution?
- Is there a per-methodology usage log? Where?
- Is there any automation (hooks, slash commands, keywords)?

### B. Chat Closer — how it works here

- Does a Chat Closer methodology exist? (yes/no)
- If yes: list its steps in order, in one line each.
- What outputs does it produce? Which files get updated?
- Is it triggered manually (paste prompt) or automatically (hook)?

### C. Known gaps in THIS project

- List the gaps from Step 4 in 2-5 lines.

### D. Verdict

One paragraph: is this project **ready to scale task tracking**? Is it **internally consistent**? Is it **comparable** with a sibling project that uses the same conventions?

---

## OUTPUT FORMAT

- Structured headers per step.
- Direct, no fluff.
- Quote evidence (file paths, snippets) wherever you make a claim.
- If something is missing, say "not found" — never paraphrase as if it existed.
- Final Step 5 must be in the **chat**, not in any file.

## ABSOLUTE PROHIBITIONS

- Do NOT run `Write`, `Edit`, `Bash` mutations, `mkdir`, `rm`, `mv`, or any tool that modifies the filesystem.
- Do NOT commit, push, or open PRs.
- Do NOT create new methodology folders or files.
- Do NOT "fix" anything you find broken.
- Do NOT skip the verification of sampled task folders — that is the whole point of the audit.
