# Methodology: Session Closure

**Purpose:** Close any working session with a full audit, task genealogy, documentation, and methodology tracking.

---

## Connected resources

- Methodology index: `claude_methodologies/`
- Problem bible: `problem_solving/CLAUDE_BIBLE.md`
- Usage log: `claude_methodologies/session_closure/USAGE_LOG.md`

---

## The Prompt

Paste this verbatim at the end of any chat to trigger full session closure:

---

> It's time to close and document this session. Follow these steps in order. Do not skip any.
>
> **Step 1 — Self-report (before looking at any folders)**
> List everything you think you did in this chat: tasks attempted, files created or modified, commands run, problems encountered, decisions made. Be specific. Do not check folders yet.
>
> **Step 2 — Task genealogy**
> For each task identified in Step 1, classify it:
> - Is it a **parent task** (a standalone goal that exists independently)?
> - Or a **sub-task** (something done in service of a larger goal)?
> For each parent task, describe: how it was born in the chat (initial request), how it was steered or refined during the session, and what its final state is (done / in progress / blocked).
> If a sub-task belongs to a parent task that spans multiple chats or sessions, name that parent task explicitly even if it was not completed here.
>
> **Step 3 — Folder inspection**
> Inspect the key parent folders where the work happened. List direct contents only — do not recurse into every subfolder. Report what you actually find.
>
> **Step 4 — Cross-check**
> Compare Step 1 and Step 3. Flag every discrepancy explicitly: "I thought X but the folder shows Y." If nothing is missing, say so — but only after checking.
>
> **Step 5 — Methodology identification**
> List the methodologies applied during this session. Check `claude_methodologies/` for existing ones. If a new methodology emerged organically during the session (a repeatable way of doing something), name it and note that it should be formalized.
>
> **Step 6 — Documentation**
> For each parent task, produce or update its documentation in its source folder. Use PROCESS_REPORT.md and STATUS_LOG.md. Each document must reference which methodology was used.
>
> **Step 7 — Problem log**
> If any mistakes, wrong assumptions, or misdiagnoses happened during this session, add them to `problem_solving/CLAUDE_BIBLE.md` following the existing format. One entry per mistake. No generics.
>
> **Step 8 — Methodology usage logs**
> For each methodology used or created in this session, append one row to its `USAGE_LOG.md` in `claude_methodologies/<methodology_name>/`. If the methodology is new and has no folder yet, create it with a `PROMPT.md` and `USAGE_LOG.md`.

---

## Notes on application

- Step 1 and Step 2 must happen before Step 3. The self-report and genealogy must not be contaminated by folder inspection.
- Step 2 is what distinguishes this from a simple audit. Parent tasks are the unit of documentation. Sub-tasks are context, not the main entry.
- Step 5 is how methodologies grow: they are identified after being used, not designed upfront.
- If a parent task spans multiple sessions, the documentation for it accumulates across sessions — each session adds to it, it does not restart.
- Keep all output files minimal. A document that is never read again is noise.
