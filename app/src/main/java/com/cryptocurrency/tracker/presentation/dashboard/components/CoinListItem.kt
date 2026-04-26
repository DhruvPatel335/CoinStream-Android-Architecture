package com.cryptocurrency.tracker.presentation.dashboard.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.core.theme.CryptotrackerTheme
import java.util.Locale

@Composable
fun CoinListItem(
    coin: Coin,
    isStale: Boolean = false,
    modifier: Modifier = Modifier
) {
    val alpha = if (isStale) 0.5f else 1f
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(contentAlignment = Alignment.BottomEnd) {
            AsyncImage(
                model = coin.imageUrl,
                contentDescription = coin.name,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .alpha(alpha),
                contentScale = ContentScale.Fit
            )
            if (isStale) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = remember(coin.symbol) { coin.symbol.uppercase() },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = alpha)
            )
            Text(
                text = coin.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray.copy(alpha = alpha)
            )
        }
        
        if (coin.sparkline.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(30.dp)
                    .padding(horizontal = 8.dp)
                    .alpha(alpha)
            ) {
                SparklineChart(
                    data = coin.sparkline,
                    modifier = Modifier.fillMaxSize(),
                    color = if (coin.changePercent24Hr >= 0) Color(0xFF2EBD85) else Color(0xFFF6465D),
                    showFill = false,
                    strokeWidth = 1.5.dp
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = remember(coin.priceUsd) { formatPrice(coin.priceUsd) },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = alpha)
            )
            val isPositive = coin.changePercent24Hr >= 0
            Text(
                text = remember(coin.changePercent24Hr) { 
                    val sign = if (isPositive) "+" else ""
                    "$sign${String.format(Locale.US, "%.2f", coin.changePercent24Hr)}%"
                },
                color = (if (isPositive) Color(0xFF2EBD85) else Color(0xFFF6465D)).copy(alpha = alpha),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 1) {
        String.format(Locale.US, "%.2f", price)
    } else {
        String.format(Locale.US, "%.4f", price)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun CoinListItemPreview() {
    CryptotrackerTheme {
        CoinListItem(
            coin = Coin(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                imageUrl = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
                priceUsd = 19392.92,
                changePercent24Hr = -2.09,
                sparkline = listOf(1.0, 1.2, 1.1, 1.3, 1.2, 1.5)
            )
        )
    }
}