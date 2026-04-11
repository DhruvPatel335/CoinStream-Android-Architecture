package com.cryptocurrency.tracker.core.util

import java.util.Locale

fun formatCurrency(value: Double): String {
    return String.format(Locale.US, "%,.2f", value)
}

fun formatLargeNumber(value: Double): String {
    return when {
        value >= 1_000_000_000_000 -> String.format(Locale.US, "%.2fT", value / 1_000_000_000_000)
        value >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", value / 1_000_000_000)
        value >= 1_000_000 -> String.format(Locale.US, "%.2fM", value / 1_000_000)
        else -> String.format(Locale.US, "%,.2f", value)
    }
}
