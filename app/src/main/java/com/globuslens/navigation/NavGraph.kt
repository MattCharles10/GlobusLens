package com.globuslens.navigation


import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.globuslens.ui.screens.FavoritesScreen
import com.globuslens.ui.screens.ProductDetailScreen
import com.globuslens.ui.screens.ResultScreen
import com.globuslens.ui.screens.ScannerScreen
import com.globuslens.ui.screens.ShoppingListScreen
import com.globuslens.viewmodel.FavoritesViewModel
import com.globuslens.viewmodel.ScannerViewModel
import com.globuslens.viewmodel.ShoppingListViewModel

sealed class Screen(val route: String) {
    object Scanner : Screen("scanner")
    object Result : Screen("result/{productId}") {
        fun passProductId(productId: Int): String = "result/$productId"
    }
    object Favorites : Screen("favorites")
    object ShoppingList : Screen("shopping_list")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun passProductId(productId: Int): String = "product_detail/$productId"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Scanner.route,
        modifier = modifier
    ) {
        composable(Screen.Scanner.route) {
            val viewModel: ScannerViewModel = hiltViewModel()
            ScannerScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.Result.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
            val viewModel: ScannerViewModel = hiltViewModel()
            ResultScreen(
                navController = navController,
                viewModel = viewModel,
                productId = productId
            )
        }

        composable(Screen.Favorites.route) {
            val viewModel: FavoritesViewModel = hiltViewModel()
            FavoritesScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.ShoppingList.route) {
            val viewModel: ShoppingListViewModel = hiltViewModel()
            ShoppingListScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.ProductDetail.route) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")?.toIntOrNull() ?: 0
            val viewModel: FavoritesViewModel = hiltViewModel()
            ProductDetailScreen(
                navController = navController,
                viewModel = viewModel,
                productId = productId
            )
        }
    }
}