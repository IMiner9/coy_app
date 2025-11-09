package com.example.myapplication.ui.screens.favorites

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Profile
import com.example.myapplication.data.PreferencesManager
import com.example.myapplication.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class CategoryItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val iconColor: Color,
    val backgroundColor: Color
)

@Composable
fun FavoritesScreen(
    navController: NavController? = null
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val profileDao = remember { database.profileDao() }
    val preferencesManager = remember { PreferencesManager(context) }
    
    var profile by remember { mutableStateOf<Profile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isDislikeMode by remember { mutableStateOf(false) } // 좋아하는 것(false) 또는 싫어하는 것(true)
    
    // 프로필 데이터 불러오기 (별명 가져오기)
    LaunchedEffect(Unit) {
        try {
            profile = profileDao.getProfile().first()
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    val scope = rememberCoroutineScope()
    
    // 싫어하는 것 모드일 때 다크모드 활성화, 좋아하는 것 모드일 때 다크모드 해제
    LaunchedEffect(isDislikeMode) {
        try {
            scope.launch {
                preferencesManager.setDarkMode(isDislikeMode)
            }
        } catch (e: Exception) {
            // 에러 무시
        }
    }
    
    // 별명 가져오기 (없으면 "내사랑")
    val nickname = profile?.nickname?.takeIf { it.isNotEmpty() } ?: "내사랑"
    
    // 카테고리 목록
    val categories = remember {
        listOf(
            CategoryItem(
                id = "food",
                title = "음식",
                subtitle = "좋아하는 음식 모아보기",
                icon = Icons.Default.Restaurant,
                iconColor = Color(0xFF8B6F47),
                backgroundColor = Color(0xFFF5F5DC)
            ),
            CategoryItem(
                id = "drinks",
                title = "음료",
                subtitle = "커피, 차, 주스 등",
                icon = Icons.Default.LocalCafe,
                iconColor = Color(0xFF8B6F47),
                backgroundColor = Color(0xFFF5F5DC)
            ),
            CategoryItem(
                id = "music",
                title = "음악",
                subtitle = "좋아하는 노래 기록",
                icon = Icons.Default.MusicNote,
                iconColor = Color(0xFF9370DB),
                backgroundColor = Color(0xFFE6E6FA)
            ),
            CategoryItem(
                id = "movies",
                title = "영화",
                subtitle = "인생 영화 목록",
                icon = Icons.Default.Movie,
                iconColor = Color(0xFF8B6F47),
                backgroundColor = Color(0xFFF5F5DC)
            ),
            CategoryItem(
                id = "travel",
                title = "여행",
                subtitle = "가고 싶은 곳 / 다녀온 곳",
                icon = Icons.Default.LocationOn,
                iconColor = Color(0xFF87CEEB),
                backgroundColor = Color(0xFFF0F8FF)
            ),
            CategoryItem(
                id = "gifts",
                title = "선물",
                subtitle = "받고 싶은 선물 정리",
                icon = Icons.Default.CardGiftcard,
                iconColor = Color(0xFF8B6F47),
                backgroundColor = Color(0xFFFFFACD)
            ),
            CategoryItem(
                id = "hobbies",
                title = "취미",
                subtitle = "함께 즐기는 활동들",
                icon = Icons.Default.SportsEsports,
                iconColor = Color(0xFF90EE90),
                backgroundColor = Color(0xFFF0FFF0)
            ),
            CategoryItem(
                id = "words",
                title = "말 / 표현",
                subtitle = "좋아하는 대사나 문장",
                icon = Icons.Default.ChatBubbleOutline,
                iconColor = Color(0xFFFFB6C1),
                backgroundColor = Color(0xFFFFF0F5)
            )
        )
    }
    
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDislikeMode) Color(0xFF102B5A) else Color(0xFFF5F5DC)) // 싫어하는 것: 어두운 파란색, 좋아하는 것: 밝은색
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp)
        ) {
            // 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "${nickname}의 취향",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = if (isDislikeMode) Color(0xFFFFFFFF) else Color(0xFF5D4037)
                    )
                    Text(
                        text = if (isDislikeMode) "💔" else "💗",
                        fontSize = 20.sp
                    )
                }
                
                // 리버스 버튼
                IconButton(
                    onClick = { isDislikeMode = !isDislikeMode },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            Color(0xFFD4A574).copy(alpha = 0.5f),
                            RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = if (isDislikeMode) "좋아하는 것으로 전환" else "싫어하는 것으로 전환",
                        modifier = Modifier.size(24.dp),
                        tint = Color(0xFF5D4037)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isDislikeMode) "서로의 싫어하는 걸 기록해요" else "서로의 좋아하는 걸 기록해요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDislikeMode) Color(0xFFE0E0E0) else Color(0xFF5D4037)
                )
                Text(
                    text = if (isDislikeMode) "⚠️" else "✨",
                    fontSize = 16.sp
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 카테고리 그리드
            Box(modifier = Modifier.fillMaxSize()) {
                val gridState = rememberLazyGridState()
                val density = LocalDensity.current
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    state = gridState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(categories) { category ->
                        CategoryCard(
                            category = category,
                            onClick = {
                                navController?.navigate(Screen.CategoryDetail.createRoute(category.id, isDislikeMode))
                            }
                        )
                    }
                }
                
                // 커스텀 스크롤바
                CustomVerticalScrollbar(
                    gridState = gridState,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(8.dp)
                        .padding(end = 2.dp)
                )
            }
        }
    }
}

@Composable
fun CategoryCard(
    category: CategoryItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = category.backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.title,
                modifier = Modifier.size(48.dp),
                tint = category.iconColor
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = category.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = Color(0xFF5D4037)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = category.subtitle,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = Color(0xFF5D4037).copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun CustomVerticalScrollbar(
    gridState: LazyGridState,
    modifier: Modifier = Modifier
) {
    val layoutInfo = gridState.layoutInfo
    val firstVisibleItemIndex = layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: 0
    val totalItems = layoutInfo.totalItemsCount
    val visibleItems = layoutInfo.visibleItemsInfo.size
    
    if (totalItems <= visibleItems) {
        // 스크롤할 필요가 없으면 스크롤바 표시 안 함
        return
    }
    
    // 스크롤 중인지 확인
    val isScrolling = gridState.isScrollInProgress
    
    // 스크롤 중일 때만 반투명하게 나타나도록 애니메이션
    val alpha by animateFloatAsState(
        targetValue = if (isScrolling) 0.5f else 0.0f,
        animationSpec = tween(
            durationMillis = 300,
            easing = FastOutSlowInEasing
        ),
        label = "scrollbar_alpha"
    )
    
    val scrollbarColor = Color(0xFFCCCCCC).copy(alpha = alpha)
    val trackColor = Color(0xFFCCCCCC).copy(alpha = alpha * 0.4f)
    
    // 알파가 0이면 표시하지 않음
    if (alpha <= 0f) {
        return
    }
    
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(Color.Transparent)
    ) {
        // 전체 스크롤바 트랙
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(6.dp)
                .align(Alignment.Center)
                .background(trackColor)
        )
        
        // 스크롤바 핸들
        val density = LocalDensity.current
        val scrollProgress = if (totalItems > visibleItems) {
            firstVisibleItemIndex.toFloat() / (totalItems - visibleItems).toFloat()
        } else {
            0f
        }
        
        val viewportHeight = layoutInfo.viewportSize.height
        val handleHeightPx = (visibleItems.toFloat() / totalItems.toFloat() * viewportHeight).coerceAtLeast(20f)
        val handleHeight = with(density) { handleHeightPx.toDp() }
        
        val maxOffsetPx = viewportHeight - handleHeightPx
        val offsetY = with(density) { (scrollProgress * maxOffsetPx).toDp() }
        
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = offsetY)
                .width(6.dp)
                .height(handleHeight)
                .background(scrollbarColor)
        )
    }
}
