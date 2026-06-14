The provided document is a reference guide for the **PerfettoSQL standard library**, a repository of tables, views, functions, and macros designed to simplify querying traces by providing high-level abstractions over low-level trace data.

### Overview
The library allows developers to move from raw trace concepts (like slices and tracks) to domain-specific concepts (like app startups or binder transactions).

To use a module in your SQL query, use the `INCLUDE PERFETTO MODULE` statement:

```sql
-- Include a specific module
INCLUDE PERFETTO MODULE android.startup.startups;

-- Query a table defined in that module
SELECT * FROM android_startups;
```

---

### Key Packages and Modules

#### 1. Prelude (Automatically Included)
The `prelude` module contains universally useful tables and views.
*   **Core Tables/Views:**
    *   `track`: Fundamental "timeline" for events.
    *   `cpu`: Information about device CPUs (ucpu, cluster_id, capacity).
    *   `sched_slice` (alias `sched`): Kernel thread scheduling information (ftrace `sched_switch`).
    *   `thread_state`: Every thread's scheduling state (Running, Runnable, etc.).
    *   `slice` (alias `slices`): Userspace slices (atrace/track_event).
    *   `thread` & `process`: Metadata about threads and processes (TID, PID, names).
    *   `args`: Arbitrary key-value metadata for other tables.
*   **Functions:**
    *   `slice_is_ancestor(ancestor_id, descendant_id)`: Returns true if the first slice is an ancestor of the second.
    *   `trace_start()`, `trace_end()`, `trace_dur()`: Get trace timing information in nanoseconds.
*   **Macros:**
    *   `cast_int(value)`, `cast_double(value)`, `cast_string(value)`: Type casting utilities.

#### 2. V8 (v8.jit)
Used for analyzing V8 engine execution, common in Chrome or Node.js traces.
*   `v8_isolate`: Represents an isolated instance of the V8 engine.
*   `v8_js_script` & `v8_wasm_script`: Compiled scripts and their sources.
*   `v8_js_function`: Details on specific JavaScript functions (name, line, column).

#### 3. Wattson (wattson.system_state)
Used for energy estimation for the CPU subsystem.
*   `wattson_system_states`: Combines CPU frequency, idle states, and L3 cache hits/misses to provide data for power modeling.

#### 4. Stacks (stacks.cpu_profiling)
Used for analyzing sampled CPU profiles (e.g., from Linux perf or Chrome profiling).
*   `cpu_profiling_samples`: Individual timestamped callsite samples.
*   `cpu_profiling_summary_tree`: A summarized tree of callstacks with `self_count` and `cumulative_count`.

### Usage Note
Tables like `raw` and `ftrace_event` are included in the prelude but are intended for **debugging purposes only**. For production metrics or standard library development, you should rely on the strongly-typed tables like `sched_slice` or `slice`.
