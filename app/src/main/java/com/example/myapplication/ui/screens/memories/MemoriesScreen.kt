package com.example.myapplication.ui.screens.memories

import android.app.DatePickerDialog
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Memory
import com.yalantis.ucrop.UCrop
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MemoriesScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val memoryDao = remember { database.memoryDao() }
    val scope = rememberCoroutineScope()
    
    val memories by remember { memoryDao.getAllMemories() }.collectAsState(initial = emptyList())
    
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMemory by remember { mutableStateOf<Memory?>(null) }
    var deletingMemoryId by remember { mutableStateOf<Long?>(null) }
    var selectedMemory by remember { mutableStateOf<Memory?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF5F5DC)
                ),
                title = {
                    Text(
                        text = "함께한 추억",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF8B4A6B)
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFFFF6B9D)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "일기 추가",
                    tint = Color.White
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5DC))
                .padding(paddingValues)
        ) {
            if (memories.isEmpty()) {
                EmptyMemoriesState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    items(memories) { memory ->
                        MemoryCard(
                            memory = memory,
                            onClick = { selectedMemory = memory },
                            onEdit = { editingMemory = memory },
                            onDelete = { deletingMemoryId = memory.id }
                        )
                    }
                }
            }
        }
    }
    
    // 일기 추가/수정 다이얼로그
    if (showAddDialog || editingMemory != null) {
        MemoryEditDialog(
            memory = editingMemory,
            onDismiss = {
                showAddDialog = false
                editingMemory = null
            },
            onSave = { memory ->
                scope.launch {
                    if (editingMemory == null) {
                        memoryDao.insertMemory(memory)
                    } else {
                        memoryDao.updateMemory(memory.copy(id = editingMemory!!.id))
                    }
                }
                showAddDialog = false
                editingMemory = null
            }
        )
    }
    
    // 일기 상세 보기 다이얼로그
    selectedMemory?.let { memory ->
        MemoryDetailDialog(
            memory = memory,
            onDismiss = { selectedMemory = null },
            onEdit = {
                selectedMemory = null
                editingMemory = memory
            },
            onDelete = {
                selectedMemory = null
                deletingMemoryId = memory.id
            }
        )
    }
    
    // 삭제 확인 다이얼로그
    deletingMemoryId?.let { memoryId ->
        AlertDialog(
            onDismissRequest = { deletingMemoryId = null },
            title = {
                Text(
                    text = "일기 삭제",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B4A6B)
                )
            },
            text = {
                Text(
                    text = "정말 삭제하시겠습니까?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF5D4037)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            memoryDao.deleteMemory(Memory(id = memoryId))
                        }
                        deletingMemoryId = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("삭제", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingMemoryId = null },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = Color(0xFF5D4037)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("취소")
                }
            },
            containerColor = Color(0xFFF5F5DC)
        )
    }
}

@Composable
fun EmptyMemoriesState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Photo,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = Color(0xFF8B4A6B).copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "아직 기록된 추억이 없어요",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8B4A6B)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "하단 + 버튼을 눌러\n함께한 하루를 기록해보세요",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF5D4037).copy(alpha = 0.7f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
fun MemoryCard(
    memory: Memory,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)
    }
    val date = try {
        LocalDate.parse(memory.date)
    } catch (e: Exception) {
        null
    }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            // 사진이 있으면 표시
            if (memory.photoUri.isNotEmpty()) {
                val imageUri = try {
                    Uri.parse(memory.photoUri)
                } catch (e: Exception) {
                    null
                }
                
                if (imageUri != null) {
                    Image(
                        painter = rememberAsyncImagePainter(
                            ImageRequest.Builder(LocalContext.current)
                                .data(imageUri)
                                .build()
                        ),
                        contentDescription = "추억 사진",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
            
        Column(
                modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 날짜
                date?.let {
                    Text(
                        text = it.format(dateFormatter),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8B4A6B).copy(alpha = 0.7f)
                    )
                }
                
                // 제목
                if (memory.title.isNotEmpty()) {
                    Text(
                        text = memory.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF8B4A6B),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 내용
                if (memory.description.isNotEmpty()) {
                    Text(
                        text = memory.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF5D4037),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryDetailDialog(
    memory: Memory,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)
    }
    val date = try {
        LocalDate.parse(memory.date)
    } catch (e: Exception) {
        null
    }
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5DC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 상단 액션 바
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "닫기",
                            tint = Color(0xFF8B4A6B)
                        )
                    }
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "수정",
                                tint = Color(0xFF8B4A6B)
                            )
                        }
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "삭제",
                                tint = Color(0xFFE91E63)
                            )
                        }
                    }
                }
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 날짜
                    date?.let {
                        Text(
                            text = it.format(dateFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF8B4A6B).copy(alpha = 0.7f)
                        )
                    }
                    
                    // 제목
                    if (memory.title.isNotEmpty()) {
                        Text(
                            text = memory.title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = Color(0xFF8B4A6B)
                        )
                    }
                    
                    // 사진
                    if (memory.photoUri.isNotEmpty()) {
                        val imageUri = try {
                            Uri.parse(memory.photoUri)
                        } catch (e: Exception) {
                            null
                        }
                        
                        if (imageUri != null) {
                            Spacer(modifier = Modifier.height(12.dp))
                            Image(
                                painter = rememberAsyncImagePainter(
                                    ImageRequest.Builder(LocalContext.current)
                                        .data(imageUri)
                                        .build()
                                ),
                                contentDescription = "추억 사진",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(300.dp)
                                    .clip(RoundedCornerShape(16.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    
                    Divider(color = Color(0xFF8B4A6B).copy(alpha = 0.2f))
                    
                    // 내용
                    if (memory.description.isNotEmpty()) {
            Text(
                            text = memory.description,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color(0xFF5D4037),
                            lineHeight = 24.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MemoryEditDialog(
    memory: Memory?,
    onDismiss: () -> Unit,
    onSave: (Memory) -> Unit
) {
    val context = LocalContext.current
    val isEditMode = memory != null
    
    var title by remember(memory) { mutableStateOf(memory?.title ?: "") }
    var description by remember(memory) { mutableStateOf(memory?.description ?: "") }
    var selectedDate by remember(memory) {
        mutableStateOf(
            try {
                memory?.date?.let { LocalDate.parse(it) } ?: LocalDate.now()
            } catch (e: Exception) {
                LocalDate.now()
            }
        )
    }
    var photoUri by remember(memory) { mutableStateOf(memory?.photoUri ?: "") }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)
    }
    
    // 이미지 크롭을 위한 Launcher
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                result.data?.let { resultData ->
                    val croppedUri = UCrop.getOutput(resultData)
                    croppedUri?.let { uri ->
                        try {
                            // 크롭된 이미지를 영구 저장소로 복사
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val outputFile = File(context.filesDir, "memory_photo_${System.currentTimeMillis()}.jpg")
                            inputStream?.use { input ->
                                outputFile.outputStream().use { output ->
                                    input.copyTo(output)
                                }
                            }
                            
                            // FileProvider URI로 변환하여 저장
                            val fileProviderUri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                outputFile
                            ).toString()
                            
                            photoUri = fileProviderUri
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            UCrop.RESULT_ERROR -> {
                result.data?.let { data ->
                    val cropError = UCrop.getError(data)
                    cropError?.printStackTrace()
                }
            }
        }
    }
    
    // 이미지 선택을 위한 Launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            // 크롭된 이미지를 저장할 임시 파일 경로
            val file = File(context.cacheDir, "cropped_memory_${System.currentTimeMillis()}.jpg")
            val cropImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // UCrop 옵션 설정 (자유 비율 크롭)
            val options = UCrop.Options().apply {
                setHideBottomControls(false)
                setFreeStyleCropEnabled(true) // 자유 크롭 활성화
                setCompressionQuality(90)
                setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
                setToolbarTitle("사진 크롭")
                setToolbarColor(context.getColor(android.R.color.white))
                setStatusBarColor(context.getColor(android.R.color.white))
                setToolbarWidgetColor(context.getColor(android.R.color.black))
                setShowCropFrame(true)
                setShowCropGrid(true)
            }
            
            // UCrop 시작 (자유 비율)
            val uCrop = UCrop.of(selectedUri, cropImageUri)
                .withMaxResultSize(1920, 1080)
                .withOptions(options)
            
            val intent = uCrop.getIntent(context)
            intent?.let {
                it.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                it.addFlags(android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                cropImageLauncher.launch(it)
            }
        }
    }
    
    // 날짜 선택 다이얼로그
    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
        },
        selectedDate.year,
        selectedDate.monthValue - 1,
        selectedDate.dayOfMonth
    )
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF5F5DC)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // 제목
                Text(
                    text = if (isEditMode) "일기 수정" else "일기 작성",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF5D4037),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 날짜 선택
                    Column {
                        Text(
                            text = "날짜",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        TextButton(
                            onClick = {
                                showDatePicker = true
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF8B4A6B)
                            )
                        ) {
                            Text(
                                text = selectedDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                    
                    // 제목 입력
                    OutlinedTextField(
                        value = title,
                        onValueChange = {
                            title = it
                            if (showError) showError = false
                        },
                        label = { Text("제목") },
                        singleLine = true,
                        isError = showError && title.isBlank(),
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF5D4037),
                            unfocusedTextColor = Color(0xFF5D4037)
                        )
                    )
                    
                    // 사진 선택
                    Column {
                        Text(
                            text = "사진",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color(0xFF5D4037)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 사진 미리보기
                            if (photoUri.isNotEmpty()) {
                                val imageUri = try {
                                    Uri.parse(photoUri)
                                } catch (e: Exception) {
                                    null
                                }
                                
                                if (imageUri != null) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(240.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFFE0E0E0))
                                    ) {
                                        Image(
                                            painter = rememberAsyncImagePainter(
                                                ImageRequest.Builder(context)
                                                    .data(imageUri)
                                                    .build()
                                            ),
                                            contentDescription = "선택한 사진",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        IconButton(
                                            onClick = { photoUri = "" },
                                            modifier = Modifier.align(Alignment.TopEnd)
                                        ) {
                                            Icon(
                                                Icons.Default.Close,
                                                contentDescription = "사진 삭제",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { imagePickerLauncher.launch("image/*") },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Color(0xFF8B4A6B)
                                    )
                                ) {
                                    Icon(
                                        Icons.Default.AddPhotoAlternate,
                                        contentDescription = null,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("사진 추가")
                                }
                            }
                        }
                    }
                    
                    // 내용 입력
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("내용") },
                        minLines = 8,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color(0xFF5D4037),
                            unfocusedTextColor = Color(0xFF5D4037)
                        )
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // 버튼
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = Color(0xFF5D4037)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("취소")
                    }
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                showError = true
                                return@Button
                            }
                            onSave(
                                Memory(
                                    id = memory?.id ?: 0,
                                    date = selectedDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                                    title = title.trim(),
                                    description = description.trim(),
                                    photoUri = photoUri
                                )
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFE91E63)
                        ),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("저장", color = Color.White)
                    }
                }
            }
        }
    }
    
    if (showDatePicker) {
        datePicker.show()
    }
}
