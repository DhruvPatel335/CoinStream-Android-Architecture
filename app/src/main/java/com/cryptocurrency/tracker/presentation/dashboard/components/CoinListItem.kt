package com.cryptocurrency.tracker.presentation.dashboard.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.cryptocurrency.tracker.domain.model.Coin
import com.cryptocurrency.tracker.core.theme.CryptotrackerTheme

@Composable
fun CoinListItem(
    coin: Coin,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = coin.imageUrl,
            contentDescription = coin.name,
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Fit
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = coin.symbol.uppercase(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = coin.name,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        
        // Sparkline in the middle
        if (coin.sparkline.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .width(60.dp)
                    .height(30.dp)
                    .padding(horizontal = 8.dp)
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
                text = formatPrice(coin.priceUsd),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            val isPositive = coin.changePercent24Hr >= 0
            Text(
                text = "${if (isPositive) "+" else ""}${String.format("%.2f", coin.changePercent24Hr)}%",
                color = if (isPositive) Color(0xFF2EBD85) else Color(0xFFF6465D),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun formatPrice(price: Double): String {
    return if (price >= 1) {
        String.format("%.2f", price)
    } else {
        String.format("%.4f", price)
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