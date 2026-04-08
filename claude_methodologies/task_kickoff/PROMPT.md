# Methodology: Task Kickoff

Start any new task with full tracking from minute zero.

---

## Before you start — read these files in order

1. `problem_solving/CLAUDE_BIBLE.md` — rules from past mistakes. Apply them actively.
2. `problem_solving/SESSION_PRIMER.md` — how we handle problems and verification.
3. List the contents of `claude_methodologies/` — know what methodologies exist and may be reusable.

Do not proceed until you have read all three.

---

## Steps — follow in order

### Step 1 — Understand the task

Read the user's message. If the task is clear, confirm your understanding back to them in 2-3 sentences. If unclear, ask before proceeding.

Identify:
- What is the goal?
- Where will the work happen? (source folder)
- What is the expected output?

If the task involves web research → apply `claude_methodologies/web_research/PROMPT.md`.

### Step 2 — New task or continuation?

Check the source folder for existing `PROCESS_REPORT.md` and `STATUS_LOG.md`.

- **If they exist:** this is a continuation. Read both files. Report the last known state to the user. Resume from where it was left off.
- **If they don't exist:** this is a new task. Proceed to Step 3.

### Step 3 — Set up tracking

Create the task's documentation in its source folder.

**PROCESS_REPORT.md** — initialize with:
- Problem: what and why
- Solution: the chosen approach and why
- Dependencies: tools, libraries, configs needed
- Methodology Used: reference to `claude_methodologies/task_kickoff/`

**STATUS_LOG.md** — first entry:
- Date, task name, `0%`, "Task started"

### Step 4 — Plan before executing

Present your approach to the user. Include:
- What you will do, in what order
- What files will be created or modified
- Any risks or decisions that need the user's input

**Do not start executing until the user approves the plan.**

### Step 5 — Execute

Work through the plan. As you reach milestones, update `STATUS_LOG.md` with a new row: date, item, progress, note.

When in doubt about a state (file exists? process running? output correct?): **check first, speak second.**

### Step 6 — Log mistakes immediately

If you make a wrong assumption, misdiagnose something, or state something that turns out to be false — add it to `problem_solving/CLAUDE_BIBLE.md` **immediately**. Do not defer to the end of the session. Follow the existing format: what happened, the rule derived, no generics.

### Step 7 — Session closure

When the task is done or the session is ending, read and apply `claude_methodologies/session_closure/PROMPT.md`. Follow all its steps.

### Step 8 — Log this methodology usage

Append one row to `claude_methodologies/task_kickoff/USAGE_LOG.md` with: date, task name, source folder, and one-line note.

---

## Rules

- Step 1-3 happen before any code or commands are run.
- Step 4 requires user approval before Step 5 begins.
- Step 6 is not optional and is not deferred. Mistakes get logged when they happen.
- If the task spans multiple sessions, each session resumes from the existing PROCESS_REPORT and STATUS_LOG (Step 2). Documentation accumulates, it does not restart.
- Keep all output files minimal. A document that is never read again is noise.
- Parent tasks are the unit of documentation. Sub-tasks live inside the parent task's STATUS_LOG, not as separate documents.
