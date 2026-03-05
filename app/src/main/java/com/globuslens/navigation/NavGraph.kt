package com.globuslens.navigation

sealed class Screen(val route: String) {
    object Scanner : Screen("scanner")
    object Result : Screen("result/{scannedText}") {
        fun passText(text: String): String = "result/$text"
    }
    object Favorites : Screen("favorites")
    object ShoppingList : Screen("shopping_list")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun passId(id: String): String = "product_detail/$id"
    }
}