package com.example.myapplication.ui.screens.favorites

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.input.pointer.pointerInput
import kotlinx.coroutines.awaitCancellation
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Favorite
import com.example.myapplication.navigation.Screen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// 카테고리 ID와 이름 매핑
val categoryNames = mapOf(
    "food" to "음식",
    "drinks" to "음료",
    "music" to "음악",
    "movies" to "영화",
    "travel" to "여행",
    "gifts" to "선물",
    "hobbies" to "취미",
    "words" to "말 / 표현"
)

// 아이콘 선택을 위한 Material Icons 리스트
data class IconOption(
    val name: String,
    val icon: ImageVector,
    val displayName: String
)

val availableIcons = listOf(
    IconOption("Restaurant", Icons.Default.Restaurant, "음식"),
    IconOption("LocalCafe", Icons.Default.LocalCafe, "커피"),
    IconOption("MusicNote", Icons.Default.MusicNote, "음악"),
    IconOption("Movie", Icons.Default.Movie, "영화"),
    IconOption("LocationOn", Icons.Default.LocationOn, "장소"),
    IconOption("CardGiftcard", Icons.Default.CardGiftcard, "선물"),
    IconOption("SportsEsports", Icons.Default.SportsEsports, "게임"),
    IconOption("ChatBubbleOutline", Icons.Default.ChatBubbleOutline, "대화"),
    IconOption("Favorite", Icons.Default.Favorite, "하트"),
    IconOption("Star", Icons.Default.Star, "별"),
    IconOption("EmojiEmotions", Icons.Default.EmojiEmotions, "이모지"),
    IconOption("Book", Icons.Default.Book, "책"),
    IconOption("Palette", Icons.Default.Palette, "예술"),
    IconOption("SportsSoccer", Icons.Default.SportsSoccer, "축구"),
    IconOption("DirectionsRun", Icons.Default.DirectionsRun, "운동"),
    IconOption("CameraAlt", Icons.Default.CameraAlt, "사진")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryDetailScreen(
    categoryId: String,
    isDislike: Boolean = false,
    navController: androidx.navigation.NavController
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val favoriteDao = remember { database.favoriteDao() }
    
    var favorites by remember { mutableStateOf<List<Favorite>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var showAddDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }
    var editingFavorite by remember { mutableStateOf<Favorite?>(null) }
    var flippedFavoriteId by remember { mutableStateOf<Long?>(null) }
    var memoTexts by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }
    val scope = rememberCoroutineScope()
    
    // 카테고리 이름 가져오기
    val categoryName = categoryNames[categoryId] ?: categoryId
    
    // 데이터 불러오기
    LaunchedEffect(categoryId, isDislike) {
        try {
            favoriteDao.getFavoritesByCategory(categoryId, isDislike).collect { list ->
                favorites = list
                isLoading = false
                // 메모 텍스트 초기화
                memoTexts = list.associate { it.id to it.description }
            }
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    // 저장 핸들러
    val onSaveFavorite: (Favorite) -> Unit = { favorite ->
        scope.launch {
            try {
                                val favoriteToSave = favorite.copy(isDislike = isDislike)
                                if (favoriteToSave.id == 0L) {
                                    favoriteDao.insertFavorite(favoriteToSave)
                                } else {
                                    favoriteDao.updateFavorite(favoriteToSave)
                                }
                // 자동으로 Flow가 업데이트되어 리스트가 갱신됨
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        showAddDialog = false
        editingFavorite = null
    }
    
    // 삭제 핸들러
    val onDeleteFavorite: (Favorite) -> Unit = { favorite ->
        scope.launch {
            try {
                favoriteDao.deleteFavorite(favorite)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isDislike) "싫어하는 $categoryName" else "좋아하는 $categoryName") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { isEditMode = !isEditMode }
                    ) {
                        Icon(
                            if (isEditMode) Icons.Default.Done else Icons.Default.Edit,
                            contentDescription = if (isEditMode) "완료" else "수정",
                            tint = if (isEditMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        },
        bottomBar = {
            // 새로운 항목 추가 버튼
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { showAddDialog = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF5F5DC)
                    )
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = null,
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDislike) "새로운 싫어하는 $categoryName 추가하기" else "새로운 $categoryName 추가하기",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF5D4037),
                            fontWeight = FontWeight.Medium
                        )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isDislike) Color(0xFF102B5A) else Color(0xFFF5F5DC)) // 싫어하는 것: 어두운 파란색, 좋아하는 것: 밝은색
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .padding(top = 24.dp)
                .padding(bottom = 16.dp)
        ) {
            // 헤더
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isDislike) "싫어하는 $categoryName" else "좋아하는 $categoryName",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp
                    ),
                    color = if (isDislike) Color(0xFFFFFFFF) else Color(0xFF5D4037)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = if (isDislike) "싫어하는 ${categoryName}을 기록해요" else "좋아하는 ${categoryName}을 기록해요",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isDislike) Color(0xFFE0E0E0) else Color(0xFF5D4037)
                    )
                    Text(
                        text = if (isDislike) "💔" else "♥", 
                        fontSize = 16.sp, 
                        color = if (isDislike) Color(0xFFFFB6C1) else Color(0xFFE91E63)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 리스트 레이아웃
            Box(modifier = Modifier.weight(1f)) {
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else if (favorites.isEmpty()) {
                    // 빈 상태
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = Color(0xFF5D4037).copy(alpha = 0.3f)
                            )
                            Text(
                                text = "아직 추가된 항목이 없어요",
                                style = MaterialTheme.typography.bodyLarge,
                                color = Color(0xFF5D4037).copy(alpha = 0.5f)
                            )
                        }
                    }
                } else {
                    val listState = rememberLazyListState()
                    
                    LazyColumn(
                        state = listState,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(favorites) { favorite ->
                            FavoriteItemCard(
                                favorite = favorite,
                                isEditMode = isEditMode,
                                isFlipped = false,
                                onFlip = { },
                                onEdit = {
                                    if (isEditMode) {
                                        editingFavorite = favorite
                                        showAddDialog = true
                                    }
                                },
                                onDelete = {
                                    onDeleteFavorite(favorite)
                                },
                                onSaveMemo = { memo ->
                                    memoTexts = memoTexts + (favorite.id to memo)
                                },
                                currentMemo = memoTexts[favorite.id] ?: favorite.description,
                                onMemoChange = { newMemo ->
                                    memoTexts = memoTexts + (favorite.id to newMemo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    // 추가/편집 다이얼로그
    if (showAddDialog) {
        AddFavoriteDialog(
            categoryId = categoryId,
            categoryName = categoryName,
            isDislike = isDislike,
            favorite = editingFavorite,
            onDismiss = { 
                showAddDialog = false
                editingFavorite = null
            },
            onSave = onSaveFavorite
        )
    }
}

@Composable
fun FavoriteItemCard(
    favorite: Favorite,
    isEditMode: Boolean,
    isFlipped: Boolean,
    onFlip: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onSaveMemo: (String) -> Unit,
    currentMemo: String,
    onMemoChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clickable(
                enabled = !isEditMode,
                onClick = {
                    if (!isEditMode) {
                        onFlip()
                    }
                }
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (favorite.isDislike) Color(0xFF102B5A) else Color(0xFFF5F5DC)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 왼쪽: 아이콘
            val iconToShow = getIconFromUri(favorite.photoUri)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF8B6F47).copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    iconToShow,
                    contentDescription = favorite.title,
                    modifier = Modifier.size(32.dp),
                    tint = Color(0xFF8B6F47).copy(alpha = 0.8f)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 오른쪽: 제목과 메모 공간
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = favorite.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    ),
                    color = Color(0xFF5D4037)
                )
                
                // 메모 미리보기
                if (currentMemo.isNotEmpty()) {
                    Text(
                        text = currentMemo,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 14.sp
                        ),
                        color = Color(0xFF5D4037).copy(alpha = 0.7f),
                        maxLines = 3,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "메모를 추가하려면 탭하세요",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF5D4037).copy(alpha = 0.4f),
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
            
            // 수정 모드일 때 삭제/수정 버튼
            if (isEditMode) {
                Spacer(modifier = Modifier.width(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Color(0xFFE91E63),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "삭제",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                    
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                Color(0xFF8B6F47),
                                RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "수정",
                            modifier = Modifier.size(18.dp),
                            tint = Color.White
                        )
                    }
                }
            }
        }
    }
}

// photoUri에서 아이콘 이름을 추출하여 ImageVector 반환
fun getIconFromUri(uri: String): ImageVector {
    if (uri.isEmpty()) {
        return Icons.Default.Favorite
    }
    
    // URI가 아이콘 이름인 경우 (예: "icon:Restaurant")
    if (uri.startsWith("icon:")) {
        val iconName = uri.removePrefix("icon:")
        return availableIcons.find { it.name == iconName }?.icon ?: Icons.Default.Favorite
    }
    
    // 실제 이미지 URI인 경우 (나중에 이미지 로딩 구현)
    return Icons.Default.Favorite
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFavoriteDialog(
    categoryId: String,
    categoryName: String,
    isDislike: Boolean = false,
    favorite: Favorite? = null,
    onDismiss: () -> Unit,
    onSave: (Favorite) -> Unit
) {
    var title by remember { mutableStateOf(favorite?.title ?: "") }
    var selectedIconName by remember { mutableStateOf(favorite?.photoUri?.removePrefix("icon:") ?: "") }
    var showIconPicker by remember { mutableStateOf(false) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }
    
    // favorite가 있으면 편집 모드
    val isEditMode = favorite != null
    
    // 다이얼로그가 열릴 때 포커스 요청
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (isEditMode) {
                        if (isDislike) "싫어하는 것 수정하기" else "취향 수정하기"
                    } else {
                        if (isDislike) "싫어하는 것 설정하기" else "취향 설정하기"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                // 아이콘 선택
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "아이콘 선택",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF5F5DC))
                            .clickable { showIconPicker = true },
                        contentAlignment = Alignment.Center
                    ) {
                        val selectedIcon = availableIcons.find { it.name == selectedIconName }
                        if (selectedIcon != null) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    selectedIcon.icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(40.dp),
                                    tint = Color(0xFF8B6F47)
                                )
                                Text(
                                    text = selectedIcon.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5D4037)
                                )
                            }
                        } else {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color(0xFF5D4037).copy(alpha = 0.5f)
                                )
                                Text(
                                    text = "아이콘 선택하기",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF5D4037).copy(alpha = 0.5f)
                                )
                            }
                        }
                    }
                }
                
                // 이름 입력
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("$categoryName 이름") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    placeholder = { Text("예: 치킨, 피자 등") },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                        onDone = {
                            keyboardController?.hide()
                            if (title.isNotBlank()) {
                                val iconUri = if (selectedIconName.isNotEmpty()) "icon:$selectedIconName" else ""
                                onSave(
                                    Favorite(
                                        id = favorite?.id ?: 0L,
                                        category = categoryId,
                                        title = title,
                                        photoUri = iconUri,
                                        isDislike = isDislike
                                    )
                                )
                            }
                        }
                    )
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            if (title.isNotBlank()) {
                                val iconUri = if (selectedIconName.isNotEmpty()) "icon:$selectedIconName" else ""
                                onSave(
                                    Favorite(
                                        id = favorite?.id ?: 0L,
                                        category = categoryId,
                                        title = title,
                                        photoUri = iconUri,
                                        isDislike = isDislike
                                    )
                                )
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = title.isNotBlank()
                    ) {
                        Text(if (isEditMode) "수정" else "저장")
                    }
                }
            }
        }
    }
    
    // 아이콘 선택 다이얼로그
    if (showIconPicker) {
        IconPickerDialog(
            onDismiss = { showIconPicker = false },
            onIconSelected = { iconName ->
                selectedIconName = iconName
                showIconPicker = false
            },
            selectedIconName = selectedIconName
        )
    }
}

@Composable
fun IconPickerDialog(
    onDismiss: () -> Unit,
    onIconSelected: (String) -> Unit,
    selectedIconName: String
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "아이콘 선택",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(300.dp)
                ) {
                    gridItems(availableIcons) { iconOption ->
                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (iconOption.name == selectedIconName) 
                                        MaterialTheme.colorScheme.primaryContainer 
                                    else 
                                        Color(0xFFF5F5DC)
                                )
                                .clickable { onIconSelected(iconOption.name) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    iconOption.icon,
                                    contentDescription = iconOption.displayName,
                                    modifier = Modifier.size(32.dp),
                                    tint = if (iconOption.name == selectedIconName)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        Color(0xFF8B6F47)
                                )
                                Text(
                                    text = iconOption.displayName,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 10.sp,
                                    color = if (iconOption.name == selectedIconName)
                                        MaterialTheme.colorScheme.onPrimaryContainer
                                    else
                                        Color(0xFF5D4037)
                                )
                            }
                        }
                    }
                }
                
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("확인")
                }
            }
        }
    }
}

