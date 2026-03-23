package com.cryptocurrency.tracker.presentation.dashboard.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${coin.symbol}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = coin.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$${String.format("%.2f", coin.priceUsd)}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            val changeColor = if (coin.changePercent24Hr >= 0) Color.Green else Color.Red
            Text(
                text = "${if (coin.changePercent24Hr >= 0) "+" else ""}${String.format("%.2f", coin.changePercent24Hr)}%",
                color = changeColor,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.End
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoinListItemPreview() {
    CryptotrackerTheme {
        CoinListItem(
            coin = Coin(
                id = "bitcoin",
                name = "Bitcoin",
                symbol = "BTC",
                imageUrl = "https://assets.coingecko.com/coins/images/1/large/bitcoin.png",
                priceUsd = 65000.123,
                changePercent24Hr = 2.45
            )
        )
    }
}