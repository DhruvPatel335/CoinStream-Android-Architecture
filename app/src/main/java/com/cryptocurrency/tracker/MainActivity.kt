package com.cryptocurrency.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.cryptocurrency.tracker.core.theme.CryptotrackerTheme
import com.cryptocurrency.tracker.domain.model.SparklineData
import com.cryptocurrency.tracker.presentation.dashboard.components.SparklineChart
import com.cryptocurrency.tracker.presentation.navigation.NavGraph
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CryptotrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Optimization: Warm up Skia shaders by drawing a tiny, invisible chart
                    // This eliminates ~250ms of shader compilation jank on the first frame.
                    WarmUpShaders()
                    
                    NavGraph()
                }
            }
        }
    }
}

@Composable
private fun WarmUpShaders() {
    SparklineChart(
        data = SparklineData(listOf(1.0, 2.0)),
        modifier = Modifier
            .size(1.dp)
            .alpha(0.01f)
    )
}