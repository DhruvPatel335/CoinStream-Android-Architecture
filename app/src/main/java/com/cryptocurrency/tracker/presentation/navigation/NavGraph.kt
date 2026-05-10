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
import androidx.paging.compose.collectAsLazyPagingItems
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
            val pagedCoins = viewModel.pagedCoins.collectAsLazyPagingItems()
            val livePrices by viewModel.livePrices.collectAsState()
            val lastUpdateMap by viewModel.lastUpdateMap.collectAsState()
            
            CoinListScreen(
                state = state,
                pagedCoins = pagedCoins,
                livePrices = livePrices,
                lastUpdateMap = lastUpdateMap,
                onCoinClick = { coinId ->
                    navController.navigate(Screen.CoinDetail.createRoute(coinId))
                },
                onRefresh = { pagedCoins.refresh() },
                onFilterSelected = { filter ->
                    viewModel.onFilterSelected(filter)
                },
                onVisibleCoinsChanged = { symbols ->
                    viewModel.onVisibleCoinsChanged(symbols)
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