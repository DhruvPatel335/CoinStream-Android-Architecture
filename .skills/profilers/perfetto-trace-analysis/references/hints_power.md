The `hints_power.md` file provides several key strategies for analyzing power consumption and battery drain using Perfetto traces. Here is a summary of the techniques mentioned:

### General Power Analysis
*   **Overall Drain:** To investigate total battery usage, query the `power_rails` track. Calculate the energy consumed for each rail by summing `power_ma * duration`.
*   **Comparison:** Convert findings into a common energy unit, such as **milliwatt-hours (mWh)**, to compare the severity of different power issues accurately.

### Sleep and Suspend Issues
*   **Verify Sleep:** Check if a device is correctly entering sleep mode during screen-off periods by querying the `suspend_state` track. If the device is not in the "suspended" state during these times, there is a wakefulness problem.
*   **Identify Blockers:** If the device fails to suspend, query the `kernel_wakelock` track. Aggregate the total duration for each wake lock name to find the root cause.

### Specialized Investigations
*   **Bluetooth:** If top kernel wake locks are related to Bluetooth (e.g., names containing `bt_` or `bcm` like `bt_host_wake`), cross-reference their timing with events in the `bluetooth_scan_results` track.
*   **Modem/Network:**
    *   If the modem rail in `power_rails` shows high consumption, query the `network_packets` table and aggregate traffic volume by `uid` to identify the responsible app.
    *   For apps using a shared UID, use the `socket_tag` associated with network packets for more granular attribution.
