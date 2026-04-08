# Methodology: Chat Rescuing

**Purpose:** Close and document a Claude conversation session that ended prematurely due to an API error or connection issue. Ensures no work is lost and all insights are captured.

---

## Connected resources

- Session closure: `claude_methodologies/session_closure/PROMPT.md`
- Problem bible: `problem_solving/CLAUDE_BIBLE.md`
- Usage log: `claude_methodologies/chat_rescuing/USAGE_LOG.md`

---

## The Prompt

Use this prompt verbatim when you have a rescued chat file that needs to be analyzed and closed:

---

> It's time to rescue and document this session. Before doing anything, read the exported chat file to understand what was attempted.
>
> Then follow the **8-step closure process** in order. Do not skip any.
>
> **Step 1 — Self-report (based on chat content only, before looking at folders)**
> Based only on what the chat file says, list everything that was done: tasks attempted, files created or modified, commands run, problems encountered, decisions made. Be specific. Do not check folders yet.
>
> **Step 2 — Task genealogy**
> For each task identified in Step 1, classify it:
> - Is it a parent task (a standalone goal that exists independently)?
> - Or a sub-task (something done in service of a larger goal)?
> For each parent task, describe: how it was born in the chat, how it was steered or refined, and what its final state is (done / in progress / blocked).
> If a sub-task belongs to a parent task that spans multiple sessions, name that parent explicitly.
>
> **Step 3 — Folder inspection**
> Inspect the key parent folders where the work happened. List direct contents only — do not recurse into every subfolder. Report what you actually find.
>
> **Step 4 — Cross-check**
> Compare Step 1 and Step 3. Flag every discrepancy explicitly: "The chat says X but the folder shows Y." If nothing is missing, say so — but only after checking.
>
> **Step 5 — Methodology identification**
> List the methodologies applied during the session. Check `claude_methodologies/` for existing ones. If a new methodology emerged organically, name it and note it should be formalized.
>
> **Step 6 — Documentation**
> For each parent task, produce or update its documentation in its source folder. Use PROCESS_REPORT.md and STATUS_LOG.md as the base structure. Evolve the structure if needed. Each document must reference which methodology was used.
>
> **Step 7 — Problem log**
> If any mistakes, wrong assumptions, or misdiagnoses occurred during the session, add them to `problem_solving/CLAUDE_BIBLE.md` following the existing format. One entry per mistake. No generics.
>
> **Step 8 — Methodology usage logs**
> For each methodology used or created in this session, append one row to its `USAGE_LOG.md` in `claude_methodologies/<methodology_name>/`. If the methodology is new and has no folder yet, create it with a `PROMPT.md` and `USAGE_LOG.md`.

---

## Notes on application

- The 8-step process is the same whether rescuing a failed session or closing a normal one
- Step 1 and Step 2 must happen before Step 3 — never contaminate the self-report with folder inspection
- Step 2 distinguishes this from a simple audit: parent tasks are the unit of documentation
- If a parent task spans multiple sessions, the documentation accumulates — add to it, don't restart it
- Step 5 is where methodologies grow: they are identified after being used, not designed upfront
- Keep all documentation minimal — a document that is never read again is noise
- Chat rescuing is always triggered by external failure (API error, rate limit, connection drop), not by deliberate session end

---

## When to use this methodology

- A session ended unexpectedly (API error, rate limit, connection drop, etc.)
- The exported chat summary or context file exists in `problem_solving/chat_rescuing/`
- Work was in progress and you need to verify what was actually completed before continuing

## Difference from Session Closure

**Session Closure:** Closes a normal, intentional session end.
**Chat Rescuing:** Handles a session that ended due to external failure. Same 8 steps, but with emphasis on verifying work completion despite the interruption.
