package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.navigation.NavGraph
import com.example.myapplication.navigation.Screen
import com.example.myapplication.ui.components.BottomNavigationBar
import com.example.myapplication.ui.theme.MyApplicationTheme
import com.example.myapplication.utils.NotificationHelper
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        try {
            // 알림 채널 생성
            NotificationHelper.createNotificationChannel(this)
        } catch (e: Exception) {
            // 알림 채널 생성 실패해도 계속 진행
            e.printStackTrace()
        }
        
        val preferencesManager = PreferencesManager(applicationContext)
        
        setContent {
            var darkMode by remember { mutableStateOf(false) }
            val scope = rememberCoroutineScope()
            
            // 다크모드 설정 불러오기 및 실시간 업데이트
            LaunchedEffect(Unit) {
                try {
                    preferencesManager.darkMode.collect { isDark ->
                        darkMode = isDark
                    }
                } catch (e: Exception) {
                    darkMode = false
                }
            }
            
            MyApplicationTheme(darkTheme = darkMode) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                
                // Favorites 화면 또는 카테고리 상세 화면이 아닐 때 다크모드 해제
                LaunchedEffect(currentRoute) {
                    val isFavoritesScreen = currentRoute == Screen.Favorites.route || 
                                           currentRoute?.startsWith("category/") == true
                    if (!isFavoritesScreen && darkMode) {
                        scope.launch {
                            try {
                                preferencesManager.setDarkMode(false)
                            } catch (e: Exception) {
                                // 에러 무시
                            }
                        }
                    }
                }
                
                Scaffold(
                    topBar = {
                        // 카테고리 상세 화면일 때는 TopAppBar 숨기기 (자체 TopAppBar 사용)
                        if (currentRoute?.startsWith("category/") != true) {
                            TopAppBar(
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color(0xFFF5F5DC)
                                ),
                                title = { 
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                    Text(
                                        when (currentRoute) {
                                            Screen.Profile.route -> "애인의 프로필"
                                            Screen.Favorites.route -> "좋아하는 것들"
                                            Screen.Memories.route -> "함께한 추억"
                                            Screen.Calendar.route -> "일정 캘린더"
                                            Screen.Anniversary.route -> "기념일"
                                            else -> "연인 프로필 앱"
                                        }
                                    )
                                    }
                                },
                                actions = {
                                    // 다크모드 설정 버튼 제거
                                }
                            )
                        }
                    },
                    bottomBar = {
                        // 카테고리 상세 화면일 때는 하단 네비게이션 바 숨기기
                        if (currentRoute?.startsWith("category/") != true) {
                            BottomNavigationBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    try {
                                        navController.navigate(route) {
                                            popUpTo(navController.graph.startDestinationId) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    } catch (e: Exception) {
                                        // 네비게이션 에러 무시
                                    }
                                }
                            )
                        }
                    }
                ) { paddingValues ->
                    NavGraph(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}
