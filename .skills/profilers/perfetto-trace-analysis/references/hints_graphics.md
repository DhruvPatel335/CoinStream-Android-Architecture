Based on the provided content from `hints_graphics.md`, here is a summary of the tips and techniques for analyzing graphics performance and memory usage using Perfetto:

### UI Jank Investigation
*   **Identify Missed Frames:** Compare the `actual_frame_timeline` against the `expected_frame_timeline` on the main process. Significant deviations indicate missed frames.
*   **Main Thread Analysis:** Look for long-running slices on the main thread. For example, if `ConstraintLayout.onMeasure` takes longer than 8ms, it is a likely cause of jank.
*   **Texture Uploads:** Check for frequent or long-running `texture_upload` slices on the main thread, as these can stall the UI.
*   **Frame Duration Correlation:** If `actual_frame_timeline_frame` durations exceed the vsync interval (e.g., 16.6ms) and correlate with `gpu_mem_total` spikes, the jank may be caused by memory pressure.

### Graphics Memory Analysis
*   **High-Level Overview:** Track the `gpu_mem_total` counter for a specific process (`upid`) to monitor overall graphics memory usage.
*   **Identify Large Allocations:** Query the `android_graphics_allocs` table and sort by `size_bytes`. Compare the width and height of these allocations against the device's display resolution to find oversized buffers.
*   **Double Memory Cost:** To detect images existing on both the CPU and GPU, look for a large buffer in `android_graphics_allocs` that matches a simultaneous CPU memory allocation (check `rss_anon_bytes` or `heap_graph`).
*   **Intermediate Render Targets:** Look for large buffers in `android_graphics_allocs` where the `usage_bits` lack the `COMPOSER_OVERLAY` flag; these are likely costly intermediate targets.
*   **Screen Presentation:** To verify if an allocation is actually shown on screen, correlate its `buffer_id` with SurfaceFlinger events.

### Rendering and Kernel Operations
*   **Bitmap Transfers:** If a `bitmap_write_to_parcel` slice takes milliseconds rather than microseconds, inspect its children. Long durations often indicate suboptimal kernel operations like `mmap` or unnecessary data zeroing.
*   **Buffer Swapping:** Analyze the duration of `eglSwapBuffersWithDamageKHR` in the graphics rendering thread. Consistently long durations suggest a large "damage area" is being redrawn every frame, which may be inefficient.
