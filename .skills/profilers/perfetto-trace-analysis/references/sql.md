This document provides a set of best practices, guidelines, and execution protocols for writing and running SQL queries within the Perfetto trace analysis framework.

### Key SQL Guidelines

When writing SQL for Perfetto, follow these principles to ensure queries are performant, accurate, and robust:

#### 1. Idempotency and Object Creation
To prevent "already exists" errors during repeated executions, always use idempotent statements:
*   **Perfetto Objects:** Use `CREATE OR REPLACE PERFETTO TABLE`, `VIEW`, `FUNCTION`, or `MACRO`.
*   **Virtual Tables (e.g., `SPAN_JOIN`):** These do not support `OR REPLACE`. You must explicitly drop them first:
    ```sql
    DROP TABLE IF EXISTS my_table;
    CREATE VIRTUAL TABLE my_table USING SPAN_JOIN(...);
    ```
*   **Indexes:** Use `DROP INDEX IF EXISTS index_name;` before creation.

#### 2. Handling Incomplete Durations
Slices that haven't finished when the trace ends are recorded with `dur = -1`. Always account for this when calculating total time or bounding boxes:
*   **Logic:** `IIF(dur = -1, trace_end() - ts, dur)`

**Example: Calculating total duration for a slice pattern**
```sql
SELECT 
  count(*) as total_count, 
  sum(IIF(slice.dur = -1, trace_end() - slice.ts, slice.dur)) / 1000000.0 as total_dur_ms
FROM slice
WHERE slice.name GLOB '*RenderThread*';
```

#### 3. String Matching
**Never use `LIKE`**, as it is computationally expensive and treats underscores as wildcards. Use `GLOB` instead:
*   **Exact match:** `name = 'my_event'`
*   **Substring match:** `name GLOB '*my_event*'`
*   **Case-insensitive:** `LOWER(name) GLOB '*renderthread*'`

#### 4. Unique Identifiers
Always join tables using **`utid`** (unique thread ID) or **`upid`** (unique process ID). Avoid using standard `tid` or `pid` because the operating system recycles these IDs, whereas Perfetto ensures `utid` and `upid` remain unique throughout the trace duration.

#### 5. Safe Argument Extraction
Do not use string parsing to extract values from slices. Use the built-in function:
`EXTRACT_ARG(arg_set_id, 'key_name')`

---

### Execution Protocol

The document defines a strict three-step process for executing queries via the `trace_processor` tool:

*   **Step 0: Tool Setup:** Use the official Python wrapper script (`trace_processor`). Download it from `https://get.perfetto.dev/trace_processor`.
*   **Step 1: Schema Research:** Before drafting, you must locate the schema for any table or view in the `perfetto-stdlib.md` documentation. Verify if a Standard Library module already provides a pre-computed view for your needs.
*   **Step 2: Validation Loop:** 
    *   Ensure all columns are prefixed with table aliases (e.g., `s.ts`).
    *   Include necessary modules using `INCLUDE PERFETTO MODULE {name}`.
    *   Verify `SPAN_JOIN` inputs are materialized as `PERFETTO TABLE`.
*   **Step 3: Final Output:** Execute the query using the `--query-string` flag and explain the results.

### Summary of Performance Hints
*   **Manual Arithmetic:** Avoid manual timestamp math to join events; rely on stdlib modules like `sched.runnable` or `intervals.overlap`.
*   **Span Join:** Always use the `PARTITIONED` clause to prevent crashes caused by overlapping intervals within the same input table.
*   **Time Overlaps:** To calculate overlap between `[start1, end1]` and `[start2, end2]`, use: `MIN(end1, end2) - MAX(start1, start2)`, ensuring the intervals actually overlap (`start1 < end2 AND start2 < end1`).
