# CoinStream 📈

An SDE-2 level architectural showcase demonstrating a real-time, offline-first Android application.

CoinStream is a cryptocurrency dashboard that merges a traditional REST API baseline with high-frequency WebSocket streams, ensuring immediate UI reactivity without frame drops, all wrapped in a strictly modular Clean Architecture.

## 🏗 Architecture & Tech Stack
This project completely separates UI state from business logic and data ingestion.

* **UI/Presentation:** Pure Jetpack Compose, heavily utilizing `StateFlow` and precise recomposition scoping for high-frequency data updates.
* **Architecture:** MVVM with Uncle Bob's Clean Architecture (Presentation -> Domain -> Data).
* **Concurrency:** Kotlin Coroutines & Flows for handling asynchronous WebSocket emissions and database observing.
* **Network (REST):** Retrofit2 + OkHttp for fetching the initial market baseline (CoinGecko API).
* **Network (Live Data):** OkHttp WebSockets for establishing a persistent, real-time price stream (Binance/CoinCap).
* **Local Persistence (Offline-First):** Room Database as the single source of truth (SSOT). The app immediately loads from the local cache upon launch before syncing with the network.
* **Dependency Injection:** Dagger-Hilt for decoupled, testable module provision.

## 🚀 Key Engineering Challenges Solved
1. **High-Frequency Recomposition:** Engineered the Compose UI to handle sub-second WebSocket price updates without recomposing the entire heavy dashboard, ensuring a flawless 60fps frame rate.
2. **Offline-First Resilience:** Architected a robust repository pattern where Room serves as the SSOT. Network calls merely update the database, and the UI reacts purely to database emissions via Flow.
3. **Lifecycle-Aware Streams:** Safely managed the opening and closing of WebSocket connections strictly tied to the UI lifecycle to prevent memory leaks and unnecessary background battery drain.