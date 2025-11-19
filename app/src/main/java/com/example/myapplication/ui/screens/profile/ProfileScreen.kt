package com.example.myapplication.ui.screens.profile

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.yalantis.ucrop.UCrop
import java.io.File
import androidx.compose.foundation.Image
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.myapplication.R
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Profile
import com.example.myapplication.data.ProfileDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val profileDao = remember { database.profileDao() }
    
    var profile by remember { mutableStateOf<Profile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showPhotoSettingsDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    
    // 이미지 크롭을 위한 Launcher
    val cropImageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        when (result.resultCode) {
            android.app.Activity.RESULT_OK -> {
                result.data?.let { resultData ->
                    val croppedUri = UCrop.getOutput(resultData)
                    croppedUri?.let { uri ->
                        scope.launch {
                            try {
                                // 크롭된 이미지를 영구 저장소로 복사
                                val inputStream = context.contentResolver.openInputStream(uri)
                                val outputFile = File(context.filesDir, "profile_photo_${System.currentTimeMillis()}.jpg")
                                inputStream?.use { input ->
                                    outputFile.outputStream().use { output ->
                                        input.copyTo(output)
                                    }
                                }
                                
                                // FileProvider URI로 변환하여 저장
                                val photoUri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    outputFile
                                ).toString()
                                
                                val updatedProfile = if (profile != null) {
                                    profile!!.copy(photoUri = photoUri)
                                } else {
                                    Profile(
                                        id = 0,
                                        name = "",
                                        nickname = "",
                                        relationshipStartDate = "",
                                        birthday = "",
                                        phoneNumber = "",
                                        mbti = "",
                                        photoUri = photoUri,
                                        favorites = "",
                                        hobbies = "",
                                        mood = "",
                                        note = ""
                                    )
                                }
                                
                                if (profile == null) {
                                    val newId = profileDao.insertProfile(updatedProfile)
                                    profile = updatedProfile.copy(id = newId)
                                } else {
                                    profileDao.updateProfile(updatedProfile.copy(id = profile!!.id))
                                    profile = updatedProfile.copy(id = profile!!.id)
                                }
                                
                                // 저장 후 데이터베이스에서 다시 불러오기
                                profileDao.getProfile().first()?.let {
                                    profile = it
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
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
            val file = File(context.cacheDir, "cropped_profile_${System.currentTimeMillis()}.jpg")
            val cropImageUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            // UCrop 옵션 설정
            val options = UCrop.Options().apply {
                setHideBottomControls(false)
                setFreeStyleCropEnabled(false) // 자유 크롭 비활성화 (1:1 비율 고정)
                setCompressionQuality(90)
                setCompressionFormat(android.graphics.Bitmap.CompressFormat.JPEG)
                setToolbarTitle("프로필 사진 크롭")
                setToolbarColor(context.getColor(android.R.color.white))
                setStatusBarColor(context.getColor(android.R.color.white))
                setToolbarWidgetColor(context.getColor(android.R.color.black))
                setShowCropFrame(true)
                setShowCropGrid(true)
            }
            
            // UCrop 시작
            val uCrop = UCrop.of(selectedUri, cropImageUri)
                .withAspectRatio(1f, 1f) // 정사각형 비율 (1:1)
                .withMaxResultSize(1024, 1024) // 최대 크기
                .withOptions(options)
            
            val intent = uCrop.getIntent(context)
            intent?.let {
                it.addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                it.addFlags(android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                cropImageLauncher.launch(it)
            }
        }
    }
    
    // 프로필 데이터 불러오기
    LaunchedEffect(Unit) {
        try {
            profile = profileDao.getProfile().first()
            isLoading = false
        } catch (e: Exception) {
            isLoading = false
        }
    }
    
    // 저장 버튼 클릭 핸들러
    val onSaveProfile: (Profile) -> Unit = { updatedProfile ->
        scope.launch {
            try {
                if (profile == null) {
                    val newId = profileDao.insertProfile(updatedProfile)
                    profile = updatedProfile.copy(id = newId)
                } else {
                    profileDao.updateProfile(updatedProfile.copy(id = profile!!.id))
                    profile = updatedProfile.copy(id = profile!!.id)
                }
                // 저장 후 데이터베이스에서 다시 불러오기
                profileDao.getProfile().first()?.let {
                    profile = it
                }
            } catch (e: Exception) {
                // 에러 처리 (나중에 Snackbar로 표시할 수 있음)
                e.printStackTrace()
            }
        }
        showEditDialog = false
    }
    
    // 기본 프로필로 설정 핸들러
    val onSetDefaultProfile: () -> Unit = {
        scope.launch {
            try {
                val updatedProfile = if (profile != null) {
                    profile!!.copy(photoUri = "")
                } else {
                    Profile(
                        id = 0,
                        name = "",
                        nickname = "",
                        relationshipStartDate = "",
                        birthday = "",
                        phoneNumber = "",
                        mbti = "",
                        photoUri = "",
                        favorites = "",
                        hobbies = "",
                        mood = "",
                        note = ""
                    )
                }
                
                if (profile == null) {
                    val newId = profileDao.insertProfile(updatedProfile)
                    profile = updatedProfile.copy(id = newId)
                } else {
                    profileDao.updateProfile(updatedProfile.copy(id = profile!!.id))
                    profile = updatedProfile.copy(id = profile!!.id)
                }
                
                // 저장 후 데이터베이스에서 다시 불러오기
                profileDao.getProfile().first()?.let {
                    profile = it
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        showPhotoSettingsDialog = false
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            // 상단 프로필 영역
            ProfileHeader(
                profile = profile,
                onEditClick = { showEditDialog = true },
                onPhotoClick = { showPhotoSettingsDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 기본 정보 및 확장 섹션
            ProfileInfoSection(
                profile = profile,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // 편집 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("편집하기")
                }
            }
        }
    }
    
    // 편집 다이얼로그
    if (showEditDialog) {
        ProfileEditDialog(
            profile = profile,
            onDismiss = { showEditDialog = false },
            onSave = onSaveProfile
        )
    }
    
    // 사진 설정 다이얼로그
    if (showPhotoSettingsDialog) {
        PhotoSettingsDialog(
            onDismiss = { showPhotoSettingsDialog = false },
            onSelectFromAlbum = {
                showPhotoSettingsDialog = false
                imagePickerLauncher.launch("image/*")
            },
            onSetDefault = onSetDefaultProfile
        )
    }
}

@Composable
fun ProfileHeader(
    profile: Profile?,
    onEditClick: () -> Unit,
    onPhotoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            )
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 프로필 사진
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .border(4.dp, MaterialTheme.colorScheme.primary, CircleShape)
                .clickable(onClick = onPhotoClick),
            contentAlignment = Alignment.Center
        ) {
            if (profile?.photoUri?.isNotEmpty() == true) {
                val imageUri = try {
                    Uri.parse(profile.photoUri)
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
                        contentDescription = "프로필 사진",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.default_profile),
                        contentDescription = "기본 프로필 사진",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                Image(
                    painter = painterResource(id = R.drawable.default_profile),
                    contentDescription = "기본 프로필 사진",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
            
            // 편집 아이콘 오버레이
            Icon(
                Icons.Default.CameraAlt,
                contentDescription = "사진 변경",
                modifier = Modifier
                    .size(32.dp)
                    .align(Alignment.BottomEnd)
                    .padding(4.dp)
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(6.dp),
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
        
        // 이름
        Text(
            text = profile?.name?.takeIf { it.isNotEmpty() } ?: "이름을 입력해주세요",
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
        
        // 별명 (값이 없어도 표시)
        Text(
            text = profile?.nickname?.takeIf { it.isNotEmpty() } ?: "별명 미입력",
            style = MaterialTheme.typography.bodyLarge,
            color = if (profile?.nickname.isNullOrEmpty())
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
            else
                MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
        )
    }
}

@Composable
fun ProfileInfoSection(
    profile: Profile?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFF5F5DC))
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // 사귀기 시작한 날
        InfoRow(
            icon = "💕",
            label = "사귀기 시작한 날",
            value = profile?.relationshipStartDate?.takeIf { it.isNotEmpty() } ?: "미입력"
        )
        
        // 생일
        InfoRow(
            icon = "📅",
            label = "생일",
            value = profile?.birthday?.takeIf { it.isNotEmpty() } ?: "미입력"
        )
        
        // 연락처
        InfoRow(
            icon = "☎️",
            label = "연락처",
            value = profile?.phoneNumber?.takeIf { it.isNotEmpty() } ?: "미입력"
        )
        
        // MBTI
        InfoRow(
            icon = "",
            label = "MBTI",
            value = profile?.mbti?.takeIf { it.isNotEmpty() } ?: "미입력"
        )
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // 좋아하는 것 (항상 표시)
        InfoRow(
            icon = "💗",
            label = "좋아하는 것",
            value = profile?.favorites?.takeIf { it.isNotEmpty() } ?: "미입력",
            isLongText = true,
            isEmpty = profile?.favorites.isNullOrEmpty()
        )
        
        // 취미 (항상 표시)
        InfoRow(
            icon = "⬆️",
            label = "취미",
            value = profile?.hobbies?.takeIf { it.isNotEmpty() } ?: "미입력",
            isEmpty = profile?.hobbies.isNullOrEmpty()
        )
        
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        
        // 현재 기분 (항상 표시)
        InfoRow(
            icon = "👁️",
            label = "현재 기분",
            value = profile?.mood?.takeIf { it.isNotEmpty() } ?: "미입력",
            isEmpty = profile?.mood.isNullOrEmpty()
        )
        
        // 연인에게 한 줄 메모 (항상 표시)
        InfoRow(
            icon = "✉️",
            label = "연인에게 한 줄 메모",
            value = profile?.note?.takeIf { it.isNotEmpty() } ?: "메모를 입력해주세요",
            isLongText = true,
            isEmpty = profile?.note.isNullOrEmpty()
        )
    }
}

@Composable
fun InfoRow(
    icon: String,
    label: String,
    value: String,
    isLongText: Boolean = false,
    isEmpty: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = if (isLongText) Alignment.Top else Alignment.CenterVertically
    ) {
        // 아이콘
        if (icon.isNotEmpty()) {
            Text(
                text = icon,
                fontSize = 20.sp,
                modifier = Modifier.width(32.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(32.dp))
        }
        
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isEmpty) 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                else 
                    MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun PhotoSettingsDialog(
    onDismiss: () -> Unit,
    onSelectFromAlbum: () -> Unit,
    onSetDefault: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFF5F5DC),
        title = {
            Text(
                text = "사진 설정",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 앨범에서 선택
                Button(
                    onClick = onSelectFromAlbum,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE91E63)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("앨범에서 선택")
                }
                
                // 기본 프로필로 설정
                OutlinedButton(
                    onClick = onSetDefault,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF5D4037)
                    ),
                    border = BorderStroke(
                        1.dp,
                        Color(0xFF5D4037)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("기본 프로필로 설정")
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF5D4037)
                )
            ) {
                Text("취소")
            }
        }
    )
}
