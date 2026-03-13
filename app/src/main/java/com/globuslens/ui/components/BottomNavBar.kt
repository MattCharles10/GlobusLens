package com.globuslens.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.globuslens.R
import androidx.compose.material3.MaterialTheme

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val iconId: Int,
    val selectedIconId: Int
) {
    object Scanner : BottomNavItem(
        route = "scanner",
        title = "Scanner",
        iconId = R.drawable.ic_scan,
        selectedIconId = R.drawable.ic_scan_filled
    )

    object Favorites : BottomNavItem(
        route = "favorites",
        title = "Favorites",
        iconId = R.drawable.ic_favorite,
        selectedIconId = R.drawable.ic_favorite_filled
    )

    object BarcodeScanner : BottomNavItem(
        route = "barcode_scanner",
        title = "Barcode",
        iconId = R.drawable.ic_barcode,
        selectedIconId = R.drawable.ic_barcode_filled
    )

    object ShoppingList : BottomNavItem(
        route = "shopping_list",
        title = "Shopping List",
        iconId = R.drawable.ic_shopping_cart,
        selectedIconId = R.drawable.ic_shopping_cart_filled
    )
}

@Composable
fun BottomNavBar(
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        BottomNavItem.Scanner,
        BottomNavItem.Favorites,
        BottomNavItem.BarcodeScanner,
        BottomNavItem.ShoppingList
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { item ->
            val selected = selectedRoute == item.route

            NavigationBarItem(
                selected = selected,
                onClick = { onItemSelected(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selected)
                            ImageVector.vectorResource(id = item.selectedIconId)
                        else
                            ImageVector.vectorResource(id = item.iconId),
                        contentDescription = item.title
                    )
                },
                label = {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    unselectedTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}