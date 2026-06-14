---
name: r8-analyzer
description: Analyzes Android build files and R8/Proguard keep rules to identify redundancies and overly broad rules that increase app size. Use when the user wants to reduce APK size, troubleshoot build issues related to shrinking, or clean up legacy Proguard rules.
license: Complete terms in LICENSE.txt
metadata:
  author: Google LLC
  last-updated: '2026-05-14'
  keywords:
  - R8
  - Proguard
  - app size
  - shrinking
  - build performance
  - AGP
---

## Overview

The R8 Analyzer identifies redundant or broad keep rules to help reduce binary size and improve build performance.

## Setup and Configuration Check

1. **AGP Version:** Check the Android Gradle Plugin version. If it's below 9.0, recommend migrating for build-time improvements.
2. **Full Mode:** Check `gradle.properties` for `android.enableR8.fullMode=false`. If present, recommend removing it to enable R8 "Full Mode".
3. **Version Detection:** Inspect `build.gradle.kts` and `libs.versions.toml` to determine the R8 version.

## Analysis Path Selection

### Path A: Quantitative (R8 >= 9.3.7-dev)
1. **Requirements:** Requires Python and the `protobuf` package.
2. **Method:** Use the [configuration analyzer](references/CONFIGURATION-ANALYZER.md) to generate a proto file and analyze `analysis.txt`.
3. **Metrics:** Extract scores and impact metrics for the final report.

### Path B: Heuristic (R8 < 9.3.7-dev)
1. **Method:** Manually inspect `proguard-rules.pro`.
2. **Redundant Rules:** Compare rules against [REDUNDANT-RULES.md](references/REDUNDANT-RULES.md).
3. **Impact Hierarchy:** Prioritize rules based on the [KEEP-RULES-IMPACT-HIERARCHY.md](references/KEEP-RULES-IMPACT-HIERARCHY.md).

## Report Generation

Generate a strictly formatted Markdown report based on [REPORT_FORMAT.md](references/REPORT_FORMAT.md). Include:
*   **Scores and Metrics:** Impact on binary size.
*   **Redundant Rules:** Specific rules to remove.
*   **Broad Rules:** Refinements for package-level `keep` rules.
*   **Validation:** Guidance on using Macrobenchmarks for verification.

*Source: https://github.com/android/skills/tree/main/performance/r8-analyzer*
