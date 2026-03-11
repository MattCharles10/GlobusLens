package com.globuslens.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.globuslens.ui.screens.FavoritesScreen
import com.globuslens.ui.screens.LandingScreen
import com.globuslens.ui.screens.PrivacyPolicyScreen
import com.globuslens.ui.screens.ProductDetailScreen
import com.globuslens.ui.screens.ResultScreen
import com.globuslens.ui.screens.ScannerScreen
import com.globuslens.ui.screens.ShoppingListScreen
import com.globuslens.viewmodel.FavoritesViewModel
import com.globuslens.viewmodel.ProductDetailViewModel
import com.globuslens.viewmodel.ResultViewModel
import com.globuslens.viewmodel.ScannerViewModel
import com.globuslens.viewmodel.ShoppingListViewModel

sealed class Screen(val route: String) {

    object PrivacyPolicy : Screen("privacy_policy")
    object Landing : Screen("landing")
    object Scanner : Screen("scanner")
    object Result : Screen("result/{productId}") {
        fun passProductId(productId: Long): String = "result/$productId"
    }
    object Favorites : Screen("favorites")
    object ShoppingList : Screen("shopping_list")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun passProductId(productId: Long): String = "product_detail/$productId"
    }

    companion object {
        val bottomNavigationScreens = listOf(
            Scanner.route,
            Favorites.route,
            ShoppingList.route
        )
    }
}

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController(),
    modifier: Modifier = Modifier,
    startDestination: String = Screen.Landing.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {

        // Landing Screen
        composable(Screen.Landing.route) {
            LandingScreen(
                navController = navController
            )
        }
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(navController = navController)
        }

        // Scanner Screen
        composable(Screen.Scanner.route) {
            val viewModel: ScannerViewModel = hiltViewModel()
            ScannerScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // Result Screen (after scanning)
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            val viewModel: ResultViewModel = hiltViewModel()

            if (productId > 0L) {
                ResultScreen(
                    navController = navController,
                    viewModel = viewModel,
                    productId = productId
                )
            } else {
                // Navigate back if invalid productId
                navController.popBackStack()
            }
        }

        // Favorites Screen
        composable(Screen.Favorites.route) {
            val viewModel: FavoritesViewModel = hiltViewModel()
            FavoritesScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // Shopping List Screen
        composable(Screen.ShoppingList.route) {
            val viewModel: ShoppingListViewModel = hiltViewModel()
            ShoppingListScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        // Product Detail Screen
        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") {
                    type = NavType.LongType
                    defaultValue = 0L
                }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getLong("productId") ?: 0L
            val viewModel: ProductDetailViewModel = hiltViewModel()

            if (productId > 0L) {
                ProductDetailScreen(
                    navController = navController,
                    viewModel = viewModel,
                    productId = productId
                )
            } else {
                // Navigate back if invalid productId
                navController.popBackStack()
            }
        }
    }
}

// Extension function to navigate and clear back stack
fun NavHostController.navigateAndClearBackStack(route: String) {
    navigate(route) {
        popUpTo(graph.startDestinationId) {
            inclusive = true
        }
        launchSingleTop = true
    }
}

// Extension function to navigate with options
fun NavHostController.navigateToResult(productId: Long) {
    navigate(Screen.Result.passProductId(productId)) {
        launchSingleTop = true
        restoreState = true
    }
}

fun NavHostController.navigateToProductDetail(productId: Long) {
    navigate(Screen.ProductDetail.passProductId(productId)) {
        launchSingleTop = true
        restoreState = true
    }
}