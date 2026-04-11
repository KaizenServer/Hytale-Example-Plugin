# CLAUDE BIBLE
Rules derived from real mistakes. No generics. Each rule has the context of what went wrong.

---

## 1. Never state a file is missing without checking it first

**What happened:**
A process error appeared in the logs. Concluded the output file write had failed and told the user the task needed a rerun. Never checked if the file existed. It did — the error was in a print statement, not the file write.

**Rule:**
Before declaring a file missing, broken, or incomplete — `ls` it. Always.

---

## 2. Never fix before diagnosing

**What happened:**
Assumed the error was in the file write line and changed the encoding parameter. The real error was in a print statement using a special character that crashed the Windows console. Applied a fix to the wrong line. Announced it confidently.

**Rule:**
Read the full error message and trace it to the exact line before touching code.
A fix applied to the wrong place is worse than no fix — it adds noise and false confidence.

---

## 3. A running process does not pick up file edits

**What happened:**
Fixed a script while the process was already running in the background. Did not explicitly state that the running process was unaffected.

**Rule:**
If a background process is running a script and the script is edited, state clearly:
the running process uses the version loaded at startup. The fix only applies to future runs.
Never imply a live process has been patched when it hasn't.

---

## 4. Agent token exhaustion leaves incomplete work silently

**What happened:**
An agent processing a batch of items ran out of tokens partway through. It created placeholder entries for the remaining items instead of fully processing them. The agent's completion report buried the issue in the summary text and claimed "complete."

The main conversation verified completion by reading the agent's self-reported status and did not catch the incomplete work until a separate verification step compared counts.

**Rule:**
After an agent completes a batch task, always verify the output independently — do not trust the agent's self-reported completion status. Perform a sample check: read one output file from the last assigned item to confirm it has real content, not a placeholder.

---

## 5. Agent may create files but skip tracking file updates

**What happened:**
An agent successfully created all content files in a batch but did not update the tracking/index files. The main conversation did not discover this until running verification commands.

The agent had separate instructions for "Step 1: Create files" and "Step 2: Update tracking files" but ran out of tokens or context before completing Step 2. Content existed on disk but was not registered in the inventory.

**Rule:**
After any agent that modifies both content files AND tracking/index files in a single run, perform an independent verification count before declaring success. Verify by comparing independent sources: file count on disk vs. entries in the tracking file. If they diverge, the agent did not complete both parts.

---

## 6. Diagnostic report stated items as unprocessed without checking the tracking file

**What happened:**
A status report listed a folder as "Unprocessed" because it was not listed in the current session's breakdown. A subsequent audit revealed the folder had been fully processed in a prior session and was correctly registered in the tracking file.

The report assumed "not processed in this session" meant "not processed at all."

**Rule:**
When producing a processing status report, always cross-reference the tracking file for every folder — including folders that pre-date the current session. A folder processed in a prior session is still processed. The source of truth for processing status is always the tracking file, not the session history.

---

## 7. Task kickoff methodology ignored when task felt like a "simple question"

**What happened:**
User asked for a research report on a topic. The request came as a natural conversation message, not a formal task. CLAUDE_BIBLE, SESSION_PRIMER, and claude_methodologies were not read. No PROCESS_REPORT or STATUS_LOG were created. The research was executed directly and delivered — only after being explicitly confronted did the tracking get created post-facto.

**Rule:**
The task_kickoff methodology applies to every task — including research tasks, prompt generation tasks, and anything that produces an output. "It felt like a question" is not an exception. If the user gives a goal and expects a deliverable, it is a task. Read the three mandatory files before starting, and create tracking files before executing.

---

## 9. Single-fix diagnosis when multiple root causes exist

**What happened:**
v0.13.0 fixed only one root cause (descendant selector IDs) but the crash persisted identically. The fix was applied confidently. The user had to report that the crash was unchanged and explicitly identify that there were likely multiple causes. A second diagnosis then found two additional root causes: (1) duplicate .ui files in JAR (both `Pages/` and `UI/Custom/` copies being packaged), and (2) wrong `cmd.set` API (passing `Message.raw()` instead of a plain String with `.Text` suffix).

**Rule:**
When a crash persists after a fix with the exact same error, do not assume the fix was correct and the crash is coincidental. Check: was the fix actually deployed? Are there other potential null sources in the same call stack? When fixing a null pointer in a UI build method, audit ALL `cmd.set()` and `evt.addEventBinding()` calls in the same method for additional null sources before declaring the fix complete.

---

## 8. Agents using bash internally triggered Microsoft Store on Windows

**What happened:**
Launched agents to read large local files. The agents internally ran bash commands (`cat`, `head`). On Windows, this triggered a system-level search for these commands, which opened the Microsoft Store and blocked execution.

**Rule:**
Never use agents to read local files on Windows. Agents use bash internally and will trigger system-level lookups for Unix commands that do not exist natively — causing Microsoft Store prompts or silent failures. Use the Read tool or PowerShell Bash commands directly. Reserve agents for tasks that genuinely require multi-step autonomous reasoning, not file I/O.
