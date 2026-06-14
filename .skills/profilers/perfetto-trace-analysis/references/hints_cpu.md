The document `hints_cpu.md` provides a collection of expert tips and techniques for analyzing CPU performance using **Perfetto** traces. The strategies focus on identifying latency, scheduling contention, and frequency issues.

### Thread State & Latency Analysis
*   **Investigate Long Slices:** When a slice takes too long, examine its **thread states** (Running, Sleeping, Blocked on I/O). If a slice has high latency, recursively check its child slices to find the bottleneck.
*   **Wake-up Delays:** If a thread is woken up but delayed in running, check the **IRQ track** for that CPU to see if interrupts are interfering.
*   **Runnable vs. Running:** 
    *   A large **Runnable** time indicates **CPU contention** (the thread wants to run but the scheduler hasn't allocated time).
    *   If a slice's wall duration increases but the percentage of "Running" time stays the same, it suggests a **lower CPU frequency**.

### Scheduling & Concurrency
*   **Expanding Scope:** If you can't find the root cause within your app package, expand your view to all threads and processes. Look for other high-priority kernel threads or runnable threads on the same CPU.
*   **Scheduler Contention:** Quantify contention by measuring the duration of the "Runnable" state (scheduling latency). Use the `preceding_sched_slice_for_thread` function to find P95/P99 latency spikes.
*   **Idle CPUs:** If a task has high scheduling latency while other CPUs are idle (running the `swapper` or idle thread), there may be an issue with load balancing or thread migration.
*   **Core Placement:** Check the `cpu_id` for critical threads. Performance can often be improved by ensuring key threads are scheduled on "big" cores rather than "LITTLE" (slower) cores.

### Advanced Debugging with SQL & Ftrace
*   **Uninterruptible Sleep (State D):** For app startup issues, use SQL queries to aggregate reasons for uninterruptible sleep on the main thread. To find concurrency locks, join blocked state data with scheduling data to identify the "waker" thread.
*   **CPU Frequency Bugs:** Monitor the `cpu_frequency` counter. Missing or stuck frequencies often point to kernel-level governor bugs. You can query raw `ftrace` events for the governor thread (e.g., `su_gov`) for deeper analysis.
*   **Kernel vs. Userspace:** Compare time spent in userspace functions against kernel-level operations (identified by the `[k]` prefix).

### Pattern Recognition
*   **Catch-up Storms:** Look for long gaps in activity immediately followed by a high-density burst of slices.
*   **Real-time Priority:** Check if hardware-associated kernel threads are running with real-time priority; if they aren't, they are susceptible to preemption, causing latency.
