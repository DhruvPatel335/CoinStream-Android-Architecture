Based on the provided content from `hints_memory.md`, here is a summary of the technical hints for analyzing memory issues using Perfetto and ftrace:

### Investigating Low Memory Kills (LMK)
*   **Identify LMKs:** Look for the `lmk_kill_occurred` ftrace event. A high frequency of these events indicates severe memory pressure.
*   **Correlation with CPU:** When LMKs occur, query the `sched_slice` table for processes with high `wall_duration`. Processes consuming excessive CPU can exacerbate memory pressure.
*   **Direct Triggers:** Look for `memory_pressure` trace events from **Pressure Stall Information (PSI)** to find what triggered the LMK daemon.

### Process and System-Wide Memory
*   **Process Impact:** Inspect `anon_rss` (Anonymous Resident Set Size) via `mem.info` counters to understand a specific process's memory footprint.
*   **System Thrashing:** 
    *   Monitor for high or rapidly increasing `swap_used` values.
    *   Check for high CPU usage by the `kswapd` kernel thread, which confirms memory thrashing.
*   **OOM Baselines:** When investigating Out-of-Memory (OOM) errors, establish a baseline by comparing current `mem.rss` values (median and 95th percentile) against historical data for that process.

### Hardware and DMA Memory
*   **Accurate Accounting:** For memory shared with hardware (like TPUs or GPUs), query `dma_heap_stat` and `dmabuf_total_size` (or `ion_total_size` on older devices) instead of RSS for a more accurate picture.
*   **Lost Memory Detection:** If `RssFile` drops significantly while hardware accelerators are active, check `kswapd0` scheduling slices. 
*   **Caveat:** Note that `RssFile` can over-report memory if the same physical page is mapped multiple times.

### Bitmap Analysis
*   **Outliers and Duplicates:** Query the `android_bitmaps` table for width and height properties. Look for duplicate `android.graphics.Bitmap` objects with identical properties.
*   **Retainer Paths:** To find what is holding a bitmap in memory, query `heap_graph_reference` to trace the path back to a GC root. Pay specific attention to custom application classes.
*   **Software Bitmaps:** Identify software bitmaps, as they consume memory on both the app heap and in graphics memory.

For more information on using these queries, you can explore the [Perfetto SQL documentation](https://perfetto.dev/docs/analysis/sql-tables).
