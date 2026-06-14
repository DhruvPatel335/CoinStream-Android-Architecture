The provided document outlines several techniques for analyzing Inter-Process Communication (IPC) and Binder transactions using Perfetto. Here is a summary of the hints extracted from `hints_ipc.md`:

### Identifying Binder Performance Issues
*   **Binder Storms:** Look for multiple outbound binder transactions from the same process (typically `system_server`) carrying similar data to different destinations in a short time. This indicates a lack of multiplexing.
*   **Binder Spam:** Query the `binder_transaction` table and group by `tid`, `service_name`, and `method_name` to find high volumes of identical calls.
*   **UI Jank via Async Operations:** Look for a binder transaction that returns quickly to the controlling process but is followed by a long-running slice in the receiving process.
*   **Scheduling Bottlenecks:** A long-running slice on one thread (e.g., `system_server`) causally linked to a slice on another (e.g., `SystemUI`) indicates a scheduling dependency.

### Tracing and Correlation
*   **Cross-Process Tracing:** Use flow events to link slices across processes by correlating a slice's ID to `flow.source_slice_id` or `flow.dest_slice_id`.
*   **Identifying Bottlenecks:** When high binder concurrency is detected, group transactions by `server_upid` to identify the bottleneck server process.
*   **Latency Analysis:** Calculate time spent outside the server (overhead) by subtracting `server_dur` from the total `dur` in the `binder_transaction` table.

### Root Cause Analysis
*   **CPU Consumption:** Correlate a suspected thread's `tid` with the `cpu_slice` table to check for high CPU usage associated with binder spam.
*   **Identifying Code Paths:**
    *   Use the `utid` of a problematic thread to query `stack_profile_callsite` to find the code responsible for binder spam.
    *   To find callers of a specific function, filter `stack_profile_callsite` for relevant frames and trace upwards using `parent_id`.
