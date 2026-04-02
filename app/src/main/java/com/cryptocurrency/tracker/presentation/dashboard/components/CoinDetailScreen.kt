package com.cryptocurrency.tracker.presentation.dashboard.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cryptocurrency.tracker.presentation.dashboard.CoinDetailViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    viewModel: CoinDetailViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = state.coin?.symbol?.uppercase() ?: "",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { }) {
                        Icon(
                            imageVector = Icons.Default.StarBorder,
                            contentDescription = "Favorite"
                        )
                    }
                    IconButton(onClick = { }) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = {
            if (state.coin != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E1E1E)),
                        shape = RoundedCornerShape(28.dp),
                        border = ButtonDefaults.outlinedButtonBorder
                    ) {
                        Text("Sell", color = Color.White, fontSize = 18.sp)
                    }
                    Button(
                        onClick = { },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3D7FFF)),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text("Buy", color = Color.White, fontSize = 18.sp)
                    }
                }
            }
        },
        containerColor = Color.Black
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            state.coin?.let { coin ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Header Info
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(
                            model = coin.imageUrl,
                            contentDescription = coin.name,
                            modifier = Modifier.size(24.dp),
                            contentScale = ContentScale.Fit
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "${coin.name} (${coin.symbol.uppercase()})",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "$${formatCurrency(coin.priceUsd)}",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        val isPositive = coin.changePercent24Hr >= 0
                        Surface(
                            color = if (isPositive) Color(0xFF2EBD85).copy(alpha = 0.2f) else Color(
                                0xFFF6465D
                            ).copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "${if (isPositive) "+" else ""}${
                                    String.format(
                                        "%.2f",
                                        coin.changePercent24Hr
                                    )
                                }%",
                                color = if (isPositive) Color(0xFF2EBD85) else Color(0xFFF6465D),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Chart
                    if (coin.sparkline.isNotEmpty()) {
                        SparklineChart(
                            data = coin.sparkline,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(250.dp),
                            color = if (coin.changePercent24Hr >= 0) Color(0xFF2EBD85) else Color(
                                0xFFF6465D
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Market Status Header
                    Text(
                        text = "Market Status",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Market Status Grid
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MarketStatusItem(
                                label = "Market Cap",
                                value = "$${formatLargeNumber(coin.marketCap)}",
                                modifier = Modifier.weight(1f)
                            )
                            MarketStatusItem(
                                label = "24h Volume",
                                value = "$${formatLargeNumber(coin.totalVolume)}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MarketStatusItem(
                                label = "24h High",
                                value = "$${formatCurrency(coin.high24h)}",
                                valueColor = Color(0xFF2EBD85),
                                modifier = Modifier.weight(1f)
                            )
                            MarketStatusItem(
                                label = "24h Low",
                                value = "$${formatCurrency(coin.low24h)}",
                                valueColor = Color(0xFFF6465D),
                                modifier = Modifier.weight(1f)
                            )
                        }
                        Row(modifier = Modifier.fillMaxWidth()) {
                            MarketStatusItem(
                                label = "All-Time High",
                                value = "$${formatCurrency(coin.ath)}",
                                modifier = Modifier.weight(1f)
                            )
                            MarketStatusItem(
                                label = "All-Time Low",
                                value = "$${formatCurrency(coin.atl)}",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))

                    // About
                    Text(
                        text = "About ${coin.name}",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${coin.name} (${coin.symbol.uppercase()}) is a digital asset. Its current price is $${
                            formatCurrency(
                                coin.priceUsd
                            )
                        } with a market cap of $${formatLargeNumber(coin.marketCap)}.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            if (state.error != null) {
                Text(
                    text = state.error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .align(Alignment.Center)
                )
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}

@Composable
fun MarketStatusItem(
    label: String,
    value: String,
    valueColor: Color = Color.White,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.Gray, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = valueColor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun SparklineChart(
    data: List<Double>,
    modifier: Modifier = Modifier,
    color: Color = Color.Green
) {
    if (data.size < 2) return

    val strokeWidth = 2.dp

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val maxPrice = data.maxOrNull() ?: 0.0
        val minPrice = data.minOrNull() ?: 0.0
        val priceRange = (maxPrice - minPrice).coerceAtLeast(0.000001)

        val verticalPadding = 8.dp.toPx()
        val usableHeight = height - (verticalPadding * 2)

        val points = data.mapIndexed { index, price ->
            val x = index * (width / (data.size - 1))
            val y =
                verticalPadding + (usableHeight - ((price - minPrice) / priceRange * usableHeight)).toFloat()
            Offset(x, y)
        }

        val strokePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            for (i in 1 until points.size) {
                lineTo(points[i].x, points[i].y)
            }
        }

        val fillPath = Path().apply {
            addPath(strokePath)
            lineTo(width, height)
            lineTo(0f, height)
            close()
        }

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(color.copy(alpha = 0.3f), Color.Transparent),
                endY = height
            )
        )

        drawPath(
            path = strokePath,
            color = color,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}

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
