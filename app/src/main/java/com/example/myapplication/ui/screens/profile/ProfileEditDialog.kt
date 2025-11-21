package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.data.Profile
import com.example.myapplication.ui.screens.anniversary.CustomCalendar
import com.example.myapplication.ui.screens.anniversary.YearMonthPickerDialog
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileEditDialog(
    profile: Profile?,
    onDismiss: () -> Unit,
    onSave: (Profile) -> Unit
) {
    var name by remember { mutableStateOf(profile?.name ?: "") }
    var nickname by remember { mutableStateOf(profile?.nickname ?: "") }
    var relationshipStartDate by remember { mutableStateOf(profile?.relationshipStartDate ?: "") }
    var birthday by remember { mutableStateOf(profile?.birthday ?: "") }
    var phoneNumber by remember { mutableStateOf(profile?.phoneNumber ?: "") }
    var mbti by remember { mutableStateOf(profile?.mbti ?: "") }
    var favorites by remember { mutableStateOf(profile?.favorites ?: "") }
    var hobbies by remember { mutableStateOf(profile?.hobbies ?: "") }
    var mood by remember { mutableStateOf(profile?.mood ?: "") }
    var note by remember { mutableStateOf(profile?.note ?: "") }
    
    var showRelationshipDatePicker by remember { mutableStateOf(false) }
    var showBirthdayDatePicker by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = MaterialTheme.shapes.large,
            color = Color(0xFFF5F5DC),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // 헤더
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "프로필 편집",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }

                Divider()

                // 스크롤 가능한 폼
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 이름
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("이름") },
                        placeholder = { Text("이름을 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC)
                        )
                    )

                    // 별명
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("별명") },
                        placeholder = { Text("별명을 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC)
                        )
                    )

                    // 사귀기 시작한 날
                    OutlinedTextField(
                        value = relationshipStartDate,
                        onValueChange = { },
                        label = { Text("사귀기 시작한 날 💕") },
                        placeholder = { Text("날짜를 선택하세요") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showRelationshipDatePicker = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showRelationshipDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "달력")
                            }
                        },
                        singleLine = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC),
                            disabledContainerColor = Color(0xFFF5F5DC),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 생일
                    OutlinedTextField(
                        value = birthday,
                        onValueChange = { },
                        label = { Text("생일 📅") },
                        placeholder = { Text("날짜를 선택하세요") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBirthdayDatePicker = true },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showBirthdayDatePicker = true }) {
                                Icon(Icons.Default.CalendarToday, contentDescription = "달력")
                            }
                        },
                        singleLine = true,
                        enabled = false,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC),
                            disabledContainerColor = Color(0xFFF5F5DC),
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    // 연락처
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("연락처 ☎️") },
                        placeholder = { Text("010-1234-5678") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC)
                        )
                    )

                    // MBTI
                    OutlinedTextField(
                        value = mbti,
                        onValueChange = { mbti = it },
                        label = { Text("MBTI") },
                        placeholder = { Text("예: INFP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC)
                        )
                    )

                    Divider(modifier = Modifier.padding(vertical = 8.dp))

                    // 좋아하는 것
                    OutlinedTextField(
                        value = favorites,
                        onValueChange = { favorites = it },
                        label = { Text("좋아하는 것 💗") },
                        placeholder = { Text("커피향, 강아지, 저녁 산책을 좋아해요 🐶🌆") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC)
                        )
                    )

                    // 취미
                    OutlinedTextField(
                        value = hobbies,
                        onValueChange = { hobbies = it },
                        label = { Text("취미 ⬆️") },
                        placeholder = { Text("🎵☕🌸") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC)
                        )
                    )

                    // 현재 기분
                    OutlinedTextField(
                        value = mood,
                        onValueChange = { mood = it },
                        label = { Text("현재 기분 👁️") },
                        placeholder = { Text("😊") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC)
                        )
                    )

                    // 연인에게 한 줄 메모
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("연인에게 한 줄 메모 ✉️") },
                        placeholder = { Text("메모를 입력해주세요") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5DC),
                            unfocusedContainerColor = Color(0xFFF5F5DC)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 저장/취소 버튼
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                            .padding(vertical = 16.dp)
                            .padding(bottom = 32.dp)
                            .navigationBarsPadding()
                            .imePadding(),
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
                            val updatedProfile = Profile(
                                id = profile?.id ?: 0,
                                name = name,
                                nickname = nickname,
                                relationshipStartDate = relationshipStartDate,
                                birthday = birthday,
                                phoneNumber = phoneNumber,
                                mbti = mbti,
                                photoUri = profile?.photoUri ?: "",
                                favorites = favorites,
                                hobbies = hobbies,
                                mood = mood,
                                note = note
                            )
                            onSave(updatedProfile)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("저장")
                        }
                    }
                }
            }
        }
    }
    
    // 사귀기 시작한 날 DatePicker
    if (showRelationshipDatePicker) {
        val dateFormatter = remember {
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)
        }
        val baseDate = relationshipStartDate.takeIf { it.isNotEmpty() }?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                LocalDate.now()
            }
        } ?: LocalDate.now()
        var currentMonth by remember { mutableStateOf(YearMonth.from(baseDate)) }
        var selectedDate by remember { mutableStateOf<LocalDate?>(baseDate) }
        
        Dialog(
            onDismissRequest = { showRelationshipDatePicker = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showRelationshipDatePicker = false },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F5DC),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = false) { }
                ) {
                    Column {
                        // 상단 날짜 정보
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = "사귀기 시작한 날",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color(0xFF8B4A6B)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            selectedDate?.let {
                                Text(
                                    text = it.format(dateFormatter),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFFE91E63),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // 커스텀 캘린더
                        CustomCalendar(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            startDate = null,
                            endDate = null,
                            onMonthChange = { currentMonth = it },
                            onDateSelected = { date ->
                                selectedDate = date
                            }
                        )
                        
                        // 하단 버튼
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showRelationshipDatePicker = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFE0E0)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("취소", color = Color(0xFFE91E63), style = MaterialTheme.typography.bodyLarge)
                            }
                            Button(
                                onClick = {
                                    selectedDate?.let { date ->
                                        relationshipStartDate = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                showRelationshipDatePicker = false
            }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE91E63)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("확인", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // 생일 DatePicker
    if (showBirthdayDatePicker) {
        val dateFormatter = remember {
            DateTimeFormatter.ofPattern("yyyy년 M월 d일 EEEE", Locale.KOREAN)
        }
        val baseDate = birthday.takeIf { it.isNotEmpty() }?.let {
            try {
                LocalDate.parse(it)
            } catch (e: Exception) {
                LocalDate.now()
            }
        } ?: LocalDate.now()
        var currentMonth by remember { mutableStateOf(YearMonth.from(baseDate)) }
        var selectedDate by remember { mutableStateOf<LocalDate?>(baseDate) }
        
        Dialog(
            onDismissRequest = { showBirthdayDatePicker = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showBirthdayDatePicker = false },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF5F5DC),
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .clickable(enabled = false) { }
                ) {
                    Column {
                        // 상단 날짜 정보
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 20.dp)
                        ) {
                            Text(
                                text = "생일",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                ),
                                color = Color(0xFF8B4A6B)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            selectedDate?.let {
                                Text(
                                    text = it.format(dateFormatter),
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFFE91E63),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                        
                        // 커스텀 캘린더 (생일 선택: 1970년 ~ 현재 년도)
                        val currentYear = LocalDate.now().year
                        CustomCalendar(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            startDate = null,
                            endDate = null,
                            onMonthChange = { currentMonth = it },
                            onDateSelected = { date ->
                                selectedDate = date
                            },
                            minYear = 1970,
                            maxYear = currentYear
                        )
                        
                        // 하단 버튼
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showBirthdayDatePicker = false },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFFE0E0)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("취소", color = Color(0xFFE91E63), style = MaterialTheme.typography.bodyLarge)
                            }
                            Button(
                onClick = {
                                    selectedDate?.let { date ->
                                        birthday = date.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                        showBirthdayDatePicker = false
            }
        },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFE91E63)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("확인", color = Color.White, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
