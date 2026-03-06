package com.globuslens.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.globuslens.R

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
        BottomNavItem.ShoppingList
    )

    NavigationBar(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedRoute == item.route,
                onClick = { onItemSelected(item.route) },
                icon = {
                    Icon(
                        imageVector = if (selectedRoute == item.route)
                            ImageVector.vectorResource(id = item.selectedIconId)
                        else
                            ImageVector.vectorResource(id = item.iconId),
                        contentDescription = item.title
                    )
                },
                label = { Text(text = item.title) }
            )
        }
    }
}