# Performance and Profiler Skill

This skill combines best practices for analyzing and optimizing Android application performance, leveraging tools like the Android Studio Profiler, Perfetto, and R8.

## 1. Perfetto Trace Analysis Protocol
Use this protocol when analyzing `.perfetto-trace` files to diagnose latency, memory leaks, or UI jank.

### Investigation Loop
1.  **Initialize Scratchpad:** Create `[trace_filename]_analysis.md` to log verified facts (timestamps, thread IDs, states).
2.  **Formulate Hypothesis:** Prioritize leads based on user input and Domain Hints (CPU, Graphics, I/O, IPC, Memory, Power).
3.  **Plan and Collect Data:** Use high-level metrics (e.g., `android_startup`) before diving into custom SQL.
4.  **Analyze and Drill Down:**
    *   **Wall Time vs. CPU Time:** Check if thread is `Running`, `Runnable` (waiting for CPU), or `Sleeping` (blocked).
    *   **Dependency Tracking:** Identify the blocker (Binder, Lock, I/O) across process boundaries.
5.  **Exhaustive Investigation:** Do not stop at the first issue; multiple bottlenecks often coexist.

### SQL Best Practices
*   **Joins:** Use `utid` (unique thread ID) or `upid` (unique process ID) instead of PIDs/TIDs.
*   **String Matching:** Use `GLOB` instead of `LIKE`.
*   **Incomplete Slices:** Handle `dur = -1` using `IIF(dur = -1, trace_end() - ts, dur)`.

## 2. R8 Performance & Size Optimization
Use this to reduce binary size and improve build performance.

### Configuration Checks
*   **Full Mode:** Ensure R8 "Full Mode" is enabled (remove `android.enableR8.fullMode=false` from `gradle.properties`).
*   **AGP Version:** Migrate to AGP 9.0+ for significant build-time improvements.

### Analysis
*   Identify redundant or overly broad Proguard/R8 rules.
*   Check for library-specific rules that are already handled by R8's internal defaults.

## 3. Jetpack Compose Performance (Project-Specific)
Optimizing high-frequency updates and large lists.

### Best Practices
*   **Stability:** Ensure models (e.g., `Coin`) are stable or immutable. Use `@Stable` or `@Immutable` if necessary.
*   **Recomposition:** Use `remember` with appropriate keys to prevent unnecessary computations.
*   **Lazy Lists:** Always use `key` in `LazyColumn`/`LazyRow` to prevent full list redraws.
*   **Derived State:** Use `derivedStateOf` when state is calculated from other states to avoid excessive recompositions.
*   **Debouncing:** Use `debounce` on high-frequency UI events (like scroll-based subscriptions) to reduce main thread load.

---
*Derived from: https://github.com/android/skills*
