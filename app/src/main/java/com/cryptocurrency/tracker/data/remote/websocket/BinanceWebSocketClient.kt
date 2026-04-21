package com.cryptocurrency.tracker.data.remote.websocket

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import okhttp3.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinanceWebSocketClient @Inject constructor(
    private val client: OkHttpClient,
    private val json: Json
) {
    private var webSocket: WebSocket? = null
    private val _tickerFlow = MutableSharedFlow<BinanceTickerDto>(extraBufferCapacity = 10)
    val tickerFlow = _tickerFlow.asSharedFlow()

    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    fun connect(symbols: List<String>) {
        if (symbols.isEmpty()) return
        
        val streams = symbols.joinToString("/") { "${it.lowercase()}usdt@ticker" }
        val request = Request.Builder()
            .url("wss://stream.binance.com:9443/ws/$streams")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                _isConnected.value = true
                println("WebSocket Connected")
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                try {
                    val ticker = json.decodeFromString<BinanceTickerDto>(text)
                    _tickerFlow.tryEmit(ticker)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                _isConnected.value = false
                println("WebSocket Closed: $reason")
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                _isConnected.value = false
                t.printStackTrace()
                println("WebSocket Failure: ${t.message}")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "Normal closure")
        webSocket = null
        _isConnected.value = false
    }
}
