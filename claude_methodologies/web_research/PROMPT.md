# Methodology: Web Research

Use this when a task requires intensive information gathering from the internet.

---

## When to apply

- Research reports on a topic (technical, academic, contextual)
- Fact-checking or verifying claims
- Gathering sources for a document or section
- Any task where the primary work is synthesizing external information

---

## Steps

### Step 1 — Define the research scope
Before searching, write down:
- What is the core question?
- What sub-questions must be answered?
- What types of sources are most reliable for this topic? (academic papers, official docs, journalism, technical blogs)

### Step 2 — Generate query set
Write at least 8 distinct search queries covering:
- The main topic from different angles
- Known controversies or criticisms
- Technical vs. non-technical perspectives
- Recent developments (add year: current year and previous)

Do not rely on a single query. Each query should target a different facet.

### Step 3 — Execute searches in parallel
Run all queries. For each result:
- Identify the 2-3 most relevant links
- Use WebFetch to read the actual page — do not rely on search result snippets alone

### Step 4 — Synthesize
Group findings by sub-question. Flag:
- What is confirmed by multiple sources
- What is disputed or uncertain (say so explicitly)
- What could not be found

### Step 5 — Structure output
Default output structure:
1. [Topic area 1]
2. [Topic area 2]
3. ...
N-1. Limitations and open questions
N. Practical conclusions

Always cite sources with URLs for key claims.

---

## Rules

- Do not rely on training data alone — always search.
- Prefer academic/technical sources over marketing claims.
- If something is uncertain, say it explicitly — do not smooth it over.
- Minimum 8 queries per research task. More if the topic is broad.
- Fetch actual pages, not just search snippets, for key sources.

---

## Output location

The research report lives in the task's source folder (defined at task_kickoff).
If the report is long, save it as `research_report.md` in the source folder.
