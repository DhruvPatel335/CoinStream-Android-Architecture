package com.cryptocurrency.tracker.data.remote.websocket

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.pow

@Singleton
class BinanceWebSocketClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    private var webSocket: WebSocket? = null
    private val _tickerFlow = MutableSharedFlow<BinanceTickerDto>(extraBufferCapacity = 64)
    val tickerFlow = _tickerFlow.asSharedFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private var currentSymbols: List<String> = emptyList()
    private var retryAttempt = 0
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var reconnectJob: Job? = null
    private var isExplicitlyDisconnected = false

    fun connect(symbols: List<String>) {
        if (symbols.isEmpty()) return
        currentSymbols = symbols
        isExplicitlyDisconnected = false
        
        val streams = symbols.joinToString("/") { "${it.lowercase()}usdt@ticker" }
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/$streams")
            .build()

        webSocket?.close(1000, null)

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
                retryAttempt = 0 
                reconnectJob?.cancel()
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                scope.launch(Dispatchers.Default) {
                    try {
                        val ticker = json.decodeFromString<BinanceTickerDto>(text)
                        _tickerFlow.emit(ticker)
                    } catch (e: Exception) {
                        // Silently fail on malformed JSON
                    }
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                handleReconnect()
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                handleReconnect()
            }
        })
    }

    private fun handleReconnect() {
        if (isExplicitlyDisconnected) return
        
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            val delayMillis = min(
                (2.0.pow(retryAttempt.toDouble()) * 1000).toLong(),
                30000L
            )
            delay(delayMillis)
            retryAttempt++
            connect(currentSymbols)
        }
    }

    fun disconnect() {
        isExplicitlyDisconnected = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        _isConnected.value = false
    }
}
