package com.cryptocurrency.tracker.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cryptocurrency.tracker.presentation.dashboard.viewmodels.CoinDetailViewModel
import com.cryptocurrency.tracker.presentation.dashboard.viewmodels.CoinViewModel
import com.cryptocurrency.tracker.presentation.dashboard.components.CoinDetailScreen
import com.cryptocurrency.tracker.presentation.dashboard.components.CoinListScreen
import com.cryptocurrency.tracker.presentation.util.Screen

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.CoinList.route
    ) {
        composable(route = Screen.CoinList.route) {
            val viewModel: CoinViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            CoinListScreen(
                state = state,
                onCoinClick = { coinId ->
                    navController.navigate(Screen.CoinDetail.createRoute(coinId))
                },
                onRefresh = { viewModel.loadCoins() },
                onFilterSelected = { filter ->
                    viewModel.onFilterSelected(filter)
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
            val state by viewModel.state.collectAsState()
            
            CoinDetailScreen(
                state = state,
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}