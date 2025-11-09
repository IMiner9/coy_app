package com.example.myapplication.ui.screens.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.myapplication.data.Profile
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

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
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 이름
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("이름") },
                        placeholder = { Text("이름을 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 별명
                    OutlinedTextField(
                        value = nickname,
                        onValueChange = { nickname = it },
                        label = { Text("별명") },
                        placeholder = { Text("별명을 입력하세요") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                        singleLine = true
                    )

                    // MBTI
                    OutlinedTextField(
                        value = mbti,
                        onValueChange = { mbti = it },
                        label = { Text("MBTI") },
                        placeholder = { Text("예: INFP") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
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
                        maxLines = 4
                    )

                    // 취미
                    OutlinedTextField(
                        value = hobbies,
                        onValueChange = { hobbies = it },
                        label = { Text("취미 ⬆️") },
                        placeholder = { Text("🎵☕🌸") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 현재 기분
                    OutlinedTextField(
                        value = mood,
                        onValueChange = { mood = it },
                        label = { Text("현재 기분 👁️") },
                        placeholder = { Text("😊") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    // 연인에게 한 줄 메모
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("연인에게 한 줄 메모 ✉️") },
                        placeholder = { Text("메모를 입력해주세요") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4
                    )
                }

                Divider()

                // 하단 버튼
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
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
    
    // 사귀기 시작한 날 DatePicker
    if (showRelationshipDatePicker) {
        DatePickerModal(
            title = "사귀기 시작한 날을 선택해주세요",
            onDismiss = { showRelationshipDatePicker = false },
            onDateSelected = { selectedDate ->
                relationshipStartDate = selectedDate
                showRelationshipDatePicker = false
            }
        )
    }
    
    // 생일 DatePicker
    if (showBirthdayDatePicker) {
        DatePickerModal(
            title = "생일을 선택해주세요",
            onDismiss = { showBirthdayDatePicker = false },
            onDateSelected = { selectedDate ->
                birthday = selectedDate
                showBirthdayDatePicker = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    title: String,
    onDismiss: () -> Unit,
    onDateSelected: (String) -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val localDate = Instant.ofEpochMilli(millis)
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()
                        onDateSelected(localDate.format(DateTimeFormatter.ISO_LOCAL_DATE))
                    }
                }
            ) {
                Text("확인")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 8.dp)
            )
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null
            )
        }
    }
}
