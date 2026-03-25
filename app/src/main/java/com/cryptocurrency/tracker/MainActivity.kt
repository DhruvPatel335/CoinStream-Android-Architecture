package com.cryptocurrency.tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cryptocurrency.tracker.core.theme.CryptotrackerTheme
import com.cryptocurrency.tracker.presentation.dashboard.CoinDetailViewModel
import com.cryptocurrency.tracker.presentation.dashboard.CoinViewModel
import com.cryptocurrency.tracker.presentation.dashboard.components.CoinDetailScreen
import com.cryptocurrency.tracker.presentation.dashboard.components.CoinListScreen
import com.cryptocurrency.tracker.presentation.util.Screen
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
                    Navigation()
                }
            }
        }
    }
}

@Composable
fun Navigation() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = Screen.CoinList.route
    ) {
        composable(route = Screen.CoinList.route) {
            val viewModel: CoinViewModel = hiltViewModel()
            CoinListScreen(
                viewModel = viewModel,
                onCoinClick = { coinId ->
                    navController.navigate(Screen.CoinDetail.createRoute(coinId))
                }
            )
        }
        composable(
            route = Screen.CoinDetail.route,
            arguments = listOf(
                navArgument("coinId") {
                    type = NavType.StringType
                }
            )
        ) {
            val viewModel: CoinDetailViewModel = hiltViewModel()
            CoinDetailScreen(
                viewModel = viewModel,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}