package com.globuslens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.globuslens.navigation.Screen
import com.globuslens.ui.screens.*
import com.globuslens.ui.theme.GlobusLensTheme
import com.globuslens.viewmodel.FavoritesViewModel
import com.globuslens.viewmodel.ScannerViewModel
import com.globuslens.viewmodel.ShoppingListViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlobusLensTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GlobusLensApp()
                }
            }
        }
    }
}

@Composable
fun GlobusLensApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.Scanner.route
    ) {
        composable(Screen.Scanner.route) {
            val viewModel: ScannerViewModel = hiltViewModel()
            ScannerScreen(
                navController = navController,
                viewModel = viewModel
            )
        }

        composable(Screen.Result.route) { backStackEntry ->
            val scannedText = backStackEntry.arguments?.getString("scannedText") ?: ""
            ResultScreen(
                navController = navController,
                scannedText = scannedText
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

        composable(Screen.ProductDetail.route + "/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailScreen(
                navController = navController,
                productId = productId
            )
        }
    }
}