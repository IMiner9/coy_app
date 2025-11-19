package com.example.myapplication.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myapplication.navigation.Screen

data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)

@Composable
fun BottomNavigationBar(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem(
            route = Screen.Profile.route,
            title = "프로필",
            icon = Icons.Default.Person
        ),
        BottomNavItem(
            route = Screen.Favorites.route,
            title = "좋아하는것",
            icon = Icons.Default.Favorite
        ),
        BottomNavItem(
            route = Screen.Memories.route,
            title = "추억",
            icon = Icons.Default.Photo
        ),
        BottomNavItem(
            route = Screen.Anniversary.route,
            title = "기념일",
            icon = Icons.Default.Cake
        )
    )

    NavigationBar {
        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = { onNavigate(item.route) }
            )
        }
    }
}







