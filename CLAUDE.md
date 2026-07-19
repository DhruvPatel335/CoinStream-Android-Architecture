# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

> See [AGENTS.md](AGENTS.md) for a deeper dive on architectural decisions, line-level pointers, and common task recipes. This file summarizes commands and the big-picture structure.

## Commands

```bash
./gradlew build                 # full build (compile + lint + test)
./gradlew assembleDebug          # build debug APK
./gradlew test                   # run all JVM unit tests (app/src/test)
./gradlew test --tests "com.cryptocurrency.tracker.data.repository.CoinRepositoryImplTest"  # single test class
./gradlew test --tests "*.CoinRepositoryImplTest.someTestMethod"                            # single test method
./gradlew connectedAndroidTest    # instrumentation tests (app/src/androidTest), needs device/emulator
./gradlew lint                   # Android lint
./gradlew clean                  # clean build outputs
```

No standalone linter/formatter config beyond Android Lint; rely on `./gradlew lint`.

## Architecture

Clean Architecture, three layers under `app/src/main/java/com/cryptocurrency/tracker/`:

- **presentation/** — Jetpack Compose UI + ViewModels (`StateFlow`). Dashboard screen, components, and per-feature viewmodels live in `presentation/dashboard/`.
- **domain/** — Pure Kotlin: `model/`, `repository/` (interfaces), `use_case/`. No Android dependencies.
- **data/** — `local/` (Room: entities, DAO, migrations), `remote/dto/` (network DTOs), `remote/websocket/` (BinanceWebSocketClient), `remote/paging/` (Jetpack Paging 3), `repository/` (CoinRepositoryImpl).
- **core/di/** — Dagger-Hilt modules (`AppModule.kt` provides OkHttpClient, Json, Retrofit ApiService, Room DB/migrations, CoinRepository).
- **core/util/**, **core/theme/** — shared utilities and Compose theme.

**Data flow**: UI → ViewModel → UseCase → Repository (hybrid WebSocket + REST) → Room DB (Single Source of Truth) → Flow back to UI. The UI never talks to the network directly; it only observes `CoinDao.observeAllCoins()`.

### Key design points (see AGENTS.md for full detail)

- **Hybrid Monitor** (`CoinRepositoryImpl.startHybridMonitor`): WebSocket is primary; REST polling kicks in after a connection-drop threshold and backs off once WS reconnects (exponential backoff, max 30s).
- **Sequence consistency**: `CoinDao.updateCoinPrice()` only applies an update if the incoming `lastUpdate` is newer than what's stored, preventing out-of-order WS packets from overwriting fresher data.
- **In-memory live price cache**: `CoinViewModel` keeps a `Map<String, Pair<Double, Double>>` of live prices separate from DB-persisted state to avoid DB churn from sub-second ticker updates; writes to DB are debounced (~2s) via `_dbUpdateQueue` (capacity 100, drops oldest on overflow).
- **Naming convention**: `Entity` suffix = Room models, `Dto` suffix = network models, domain models are undecorated.
- **Resource<T>** sealed class wraps Success/Error/Loading for REST flows only; the DB-backed Flow emits a plain `List<Coin>` directly.

## Testing

- Unit tests: `app/src/test/`, using MockK + kotlinx-coroutines-test + Turbine (Flow assertions).
- Instrumentation tests: `app/src/androidTest/` (boilerplate only currently).

## Performance Skills

Profiling/optimization playbooks live under `.skills/`:
- `.skills/profilers/perfetto-trace-analysis.md` — Perfetto trace triage methodology.
- `.skills/performance/r8-analyzer.md` — R8/Proguard rule pruning for APK size and build time.
- `.skills/performance-profiler.md` — Compose-specific high-frequency UI optimization (stability, `derivedStateOf`, `key` usage).

Consult these before making changes aimed at startup time, recomposition cost, or APK size — the repo has already been through multiple optimization passes (see recent commit history) and these docs capture the methodology used.
