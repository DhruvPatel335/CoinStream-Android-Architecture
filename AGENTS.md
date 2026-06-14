# CryptoTracker Agent Guide

A production-ready Android cryptocurrency dashboard showcasing SDE-2 level architecture with hybrid WebSocket + REST networking, offline-first persistence, and high-frequency UI optimization.

## Architecture Layers

**Clean Architecture** with strict separation:

- **Presentation Layer** (`presentation/`): Jetpack Compose UI, ViewModels using StateFlow, MutableStateFlow for in-memory live price cache
- **Domain Layer** (`domain/`): Pure Kotlin use cases & interfaces, no Android dependencies
- **Data Layer** (`data/`): Repository implementations, local/remote adapters, mappers (Entity ↔ DTO ↔ Domain Model)

**Data Flow**: UI → ViewModel → Use Cases → Repository (hybrid WS + REST) → Room DB (SSOT) → Back to UI via Flow observables.

## Critical Architectural Decisions

### 1. **Hybrid Monitor Pattern** (data/repository/CoinRepositoryImpl.kt)
- **Problem Solved**: High-frequency WebSocket can drop; REST polling ensures resilience
- **Implementation**: 
  - `observeCoins()` starts `startHybridMonitor()` that watches `webSocketClient.isConnected`
  - WebSocket prioritized; REST polling auto-activates on dropout (5s delay threshold)
  - Polling deactivates when WS reconnects (exponential backoff: max 30s)
- **When Modifying**: Ensure `isExplicitlyDisconnected` flag prevents reconnect loops on app close

### 2. **Sequence Consistency via eventTime** (CoinDao line 26)
- **Problem Solved**: Out-of-order WebSocket packets must not overwrite newer data
- **Implementation**: `updateCoinPrice()` uses `lastUpdate` timestamp in SQL WHERE clause
  - Only updates if incoming `lastUpdate > existing lastUpdate`
  - BinanceTickerDto.eventTime from WebSocket provides ordering guarantee
- **Pattern**: All temporal data compares via `lastUpdate` field, never overwrites if stale

### 3. **Room DB as Single Source of Truth (SSOT)**
- All UI reads from `dao.observeAllCoins()` Flow, guaranteeing consistency
- Initial REST fetch → clear DB → repopulate (atomic via `deleteCoins()` → `insertCoins()`)
- WebSocket updates immediately write to DB, UI reacts via database emissions
- Offline launch loads from DB instantly without network latency

### 4. **In-Memory Live Prices Cache** (CoinViewModel lines 34-35, 162-164)
- **Why**: Avoiding DB churn from 100+ sub-second ticker updates; separates UI state layers
- **Implementation**: `_livePrices: Map<String, Pair<Double, Double>>` holds (price, change%)
- **Flow**: WebSocket ticker → in-memory update → emit to UI → debounced DB write (2s, line 98)
- **Debouncing**: `_dbUpdateQueue` groups by symbol, debounces writes to reduce database contention

## Build System & Dependencies

**Gradle Version Catalog** (gradle/libs.versions.toml):
- Kotlin 2.1.0, AGP 8.13.2
- KSP (Kotlin Symbol Processing) for Hilt & Room annotation processing
- Retrofit 3.0 + OkHttp 5.0-alpha + kotlinx-serialization for network
- Room 2.6.1 with 3 active migrations (MIGRATION_1_2 → _3_4); schema evolves in AppModule
- Turbine for Flow testing, mockk for mocking

**Key Build Properties** (app/build.gradle.kts):
- Namespace: `com.cryptocurrency.tracker`
- Min SDK 24, Target/Compile SDK 36 (Android 15)
- Kotlin JVM target 11
- Compose enabled (`buildFeatures { compose = true }`)

**To Build**: `./gradlew build` or IDE sync; debug logging auto-enabled via `BuildConfig.DEBUG`

## Data Flow & Network Integration

### REST API Layer
- **ApiService.kt**: CoinGecko `/coins/markets` endpoint, fetches 100 coins with sparkline
- **Called in**: `CoinRepositoryImpl.getCoins()` for initial load and REST fallback polling
- **Error Handling**: HttpException vs IOException with user-friendly messages

### WebSocket Layer  
- **BinanceWebSocketClient**: Singleton scoped, injected into CoinRepositoryImpl
- **Streams**: One connection per symbol set (e.g., `bitcoinusdt@ticker`) using Binance multiplexing
- **Message Type**: BinanceTickerDto (symbol, price, priceChangePercent, eventTime)
- **Connection Lifecycle**: 
  - Opened in `observeCoins()` after REST initial fetch → ViewModel monitor active
  - Closed on ViewModel death via `onCleared()` → `disconnect()`
  - Auto-reconnect with exponential backoff (max 30s) on failure
  
### DAO Update Pattern
- `observeAllCoins()` returns reactive Flow<List<CoinEntity>> for UI subscription  
- `updateCoinPrice()` is lightweight: single-column UPDATE with WHERE clause on lastUpdate
- Insert strategy: `OnConflictStrategy.REPLACE` for full objects during REST refresh

## Testing Patterns

- **Unit Tests** (app/src/test/): `CoinRepositoryImplTest` using mockk + kotlinx-coroutines-test
- **Instrumentation**: Boilerplate in `androidTest/`
- **Flow Testing**: Turbine library for asserting Flow emissions

## UI Patterns & Optimizations

### Compose Recomposition Control
- **CoinListScreen.kt**: Uses `key = { it.id }` in LazyColumn to prevent full-list redraws
- **remember(coin, livePrice)**: Derive display coin only when coin or live price changes
- **staleness Tracking**: 15s threshold; 5s global check loop triggers visual indicators
- **Connection Badge**: Shows CONNECTED (green) / RECONNECTING (orange) / OFFLINE (red)

### State Management  
- **CoinListState**: Immutable UI state holding coins list, loading flag, error, selectedFilter
- **live Prices Map**: In-memory cache separate from persisted DB, allows fast UI updates
- **derivedStateOf (implicit)**: `combine()` operator merges coins from DB with filtered view

### Filtering & Sorting
- **CoinFilter enum**: ALL (no sort), TOP_GAINERS (changePercent desc), TOP_50 (marketCap desc), etc.
- **Applied in ViewModel**: `combine()` operator applies filter to DB coins before emitting to UI

## Dependency Injection (Dagger-Hilt)

**AppModule.kt** provides singletons:
- `OkHttpClient` with debug logging interceptor
- `Json` (kotlinx-serialization config ignores unknown keys)
- `ApiService` Retrofit instance (CoinGecko base URL)
- Database migrations defined here; Hilt auto-injects CoinDao
- `CoinRepository` instance assembled with all dependencies

**ViewModel Injection**: `@HiltViewModel` + `@Inject constructor()` in CoinViewModel, CoinDetailViewModel

## Common Development Tasks

### Adding New API Field
1. Update CoinDto (data/remote/dto/)
2. Add to CoinEntity (data/local/) + migration in AppModule
3. Update Coin domain model (domain/model/)
4. Add mapper extension in CoinEntity.toCoin() + reverse
5. New Room query/update if needed

### Modifying WebSocket Strategy
1. BinanceWebSocketClient: Change stream URL or parsing logic
2. CoinRepositoryImpl: Adjust `startHybridMonitor()` thresholds (5s delay, 10s polling interval)
3. Test reconnect logic with network interception

### Debugging High-Frequency Updates
- Enable OkHttp body logging (already auto-enabled in debug)
- Check ViewModel's `_dbUpdateQueue` buffer (100 capacity, drops oldest on overflow)
- Verify `lastUpdate` timestamps in DB: `adb shell sqlite3 data/data/com.../databases/coin_db`

## Project Conventions

- **Naming**: Data models use `Entity` suffix for DB, `Dto` for network DTOs, domain models undecorated  
- **Scope Management**: Repository uses `CoroutineScope(Dispatchers.IO + SupervisorJob())` for background work; ViewModels use `viewModelScope`
- **File Organization**: Strict package hierarchy mirrors architecture layers (presentation/data/domain)
- **Resource<T>**: Sealed class for Success/Error/Loading, used in REST flows only; DB flow is direct List<>

## Performance & Profiling Skills

The project integrates AI-optimized instructions from `android/skills` to maintain high performance.

### 1. Perfetto Trace Analysis
- **Location**: `.skills/profilers/perfetto-trace-analysis.md`
- **Focus**: Systematic diagnosis of latency and jank using "Chain of Evidence" scratchpads.
- **Protocol**: Mandates wall-time vs. CPU-time verification and cross-process dependency tracking.

### 2. R8 Optimization
- **Location**: `.skills/performance/r8-analyzer.md`
- **Focus**: Minifying APK size and improving build-time performance by pruning redundant Proguard rules.

### 3. High-Frequency UI Optimization
- **Location**: `.skills/performance-profiler.md`
- **Focus**: Jetpack Compose specific strategies (Stability, `derivedStateOf`, and `key` usage) to handle 100+ ticker updates per second without UI stutter.

