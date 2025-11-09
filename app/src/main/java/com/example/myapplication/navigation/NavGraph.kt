package com.example.myapplication.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.anniversary.AnniversaryScreen
import com.example.myapplication.ui.screens.calendar.CalendarScreen
import com.example.myapplication.ui.screens.favorites.CategoryDetailScreen
import com.example.myapplication.ui.screens.favorites.FavoritesScreen
import com.example.myapplication.ui.screens.memories.MemoriesScreen
import com.example.myapplication.ui.screens.profile.ProfileScreen

sealed class Screen(val route: String) {
    object Profile : Screen("profile")
    object Favorites : Screen("favorites")
    object Memories : Screen("memories")
    object Calendar : Screen("calendar")
    object Anniversary : Screen("anniversary")
    
    object CategoryDetail : Screen("category/{categoryId}/{isDislike}") {
        fun createRoute(categoryId: String, isDislike: Boolean = false) = "category/$categoryId/$isDislike"
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Profile.route,
        modifier = modifier
    ) {
        composable(Screen.Profile.route) {
            ProfileScreen()
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController = navController)
        }
        composable(Screen.Memories.route) {
            MemoriesScreen()
        }
        composable(Screen.Calendar.route) {
            CalendarScreen()
        }
        composable(Screen.Anniversary.route) {
            AnniversaryScreen()
        }
        composable(
            route = Screen.CategoryDetail.route,
            arguments = listOf(
                navArgument("categoryId") { type = NavType.StringType },
                navArgument("isDislike") { 
                    type = NavType.BoolType
                    defaultValue = false
                }
            )
        ) { backStackEntry ->
            val categoryId = backStackEntry.arguments?.getString("categoryId") ?: ""
            val isDislike = backStackEntry.arguments?.getBoolean("isDislike") ?: false
            CategoryDetailScreen(
                categoryId = categoryId,
                isDislike = isDislike,
                navController = navController
            )
        }
    }
}

