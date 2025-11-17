package com.example.myapplication.ui.screens.anniversary

import android.app.TimePickerDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.Instant
import java.time.ZoneId
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.border
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Event
import com.example.myapplication.data.EventCategory
import com.example.myapplication.data.Profile
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
import java.time.LocalDateTime
import java.time.DayOfWeek
import java.time.YearMonth
import java.util.Locale
import kotlinx.coroutines.launch
import kotlin.math.abs

enum class FilterTab {
    ALL, ANNIVERSARY, BIRTHDAY, DATE, IMPORTANT, PAST
}

private val availableColors = listOf(
    Color(0xFFF5E6F0) to "#F5E6F0", // 핑크
    Color(0xFFF0E6F5) to "#F0E6F5", // 라벤더
    Color(0xFFF5F0E6) to "#F5F0E6", // 베이지
    Color(0xFFE6F5F0) to "#E6F5F0", // 민트
    Color(0xFFE6F0F5) to "#E6F0F5", // 하늘색
    Color(0xFFF5E6E6) to "#F5E6E6", // 연한 빨강
    Color(0xFFF5F5E6) to "#F5F5E6", // 연한 노랑
    Color(0xFFE6E6F5) to "#E6E6F5"  // 연한 보라
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnniversaryScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val eventDao = remember { database.eventDao() }
    val profileDao = remember { database.profileDao() }
    val scope = rememberCoroutineScope()

    val events by remember { eventDao.getAllEvents() }.collectAsState(initial = emptyList())
    val profile by remember { profileDao.getProfile() }.collectAsState(initial = null)

    val today = remember { LocalDate.now() }

    val manualAnniversaries = remember(events) {
        events.filter { it.isAnniversary }.mapNotNull { it.toAnniversaryItem() }
    }
    val manualDates = remember(manualAnniversaries) { manualAnniversaries.map { it.date }.toSet() }
    val autoAnniversaries = remember(profile, manualDates, today) {
        generateAutoAnniversaries(profile, manualDates, today)
    }
    val combined = remember(manualAnniversaries, autoAnniversaries) {
        (manualAnniversaries + autoAnniversaries).sortedBy { it.date }
    }

    var selectedFilter by remember { mutableStateOf(FilterTab.ALL) }
    var showAddDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<Event?>(null) }

    val filteredItems = remember(combined, selectedFilter, today) {
        when (selectedFilter) {
            FilterTab.ALL -> combined
            FilterTab.ANNIVERSARY -> combined.filter { it.category == EventCategory.ANNIVERSARY }
            FilterTab.BIRTHDAY -> combined.filter { it.category == EventCategory.BIRTHDAY }
            FilterTab.DATE -> combined.filter { it.category == EventCategory.DATE }
            FilterTab.IMPORTANT -> combined.filter { it.category == EventCategory.IMPORTANT }
            FilterTab.PAST -> combined.filter { it.date.isBefore(today) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                "기념일",
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = Color(0xFF8B4A6B)
                            )
                            Text("🎉", fontSize = 20.sp)
                        }
                        Text(
                            text = "우리의 소중한 날들을 기록해요 ✨",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF8B4A6B).copy(alpha = 0.7f)
                        )
                    }
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
                    contentDescription = "기념일 추가",
                    tint = Color.White
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CategoryTabs(
                selectedFilter = selectedFilter,
                onFilterSelected = { selectedFilter = it },
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
            )

            if (filteredItems.isEmpty()) {
                EmptyAnniversaryState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(items = filteredItems, key = { it.key }) { item ->
                        AnniversaryCard(
                            item = item,
                            today = today,
                            onClick = {
                                if (!item.isAuto && item.sourceEventId != null) {
                                    val event = events.find { it.id == item.sourceEventId }
                                    if (event != null) {
                                        editingEvent = event
                                    }
                                }
                            },
                            onDelete = { eventId ->
                                scope.launch {
                                    eventDao.deleteEvent(Event(id = eventId))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAnniversaryDialog(
            event = null,
            onDismiss = { showAddDialog = false },
            onSave = { title, startDate, endDate, startTime, endTime, isAllDay, memo, category, icon, color ->
                scope.launch {
                    eventDao.insertEvent(
                        Event(
                            title = title,
                            description = memo,
                            date = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            endDate = if (endDate != null) endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) else "",
                            time = if (!isAllDay && startTime != null && endTime != null) {
                                "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}-${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}"
                            } else "",
                            isAnniversary = true,
                            category = category.id,
                            icon = icon.id,
                            color = color
                        )
                    )
                }
                showAddDialog = false
            }
        )
    }

    editingEvent?.let { event ->
        AddAnniversaryDialog(
            event = event,
            onDismiss = { editingEvent = null },
            onSave = { title, startDate, endDate, startTime, endTime, isAllDay, memo, category, icon, color ->
                scope.launch {
                    eventDao.updateEvent(
                        event.copy(
                            title = title,
                            description = memo,
                            date = startDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                            endDate = if (endDate != null) endDate.format(DateTimeFormatter.ISO_LOCAL_DATE) else "",
                            time = if (!isAllDay && startTime != null && endTime != null) {
                                "${startTime.hour.toString().padStart(2, '0')}:${startTime.minute.toString().padStart(2, '0')}-${endTime.hour.toString().padStart(2, '0')}:${endTime.minute.toString().padStart(2, '0')}"
                            } else "",
                            category = category.id,
                            icon = icon.id,
                            color = color
                        )
                    )
                }
                editingEvent = null
            }
        )
    }
}

@Composable
private fun CategoryTabs(
    selectedFilter: FilterTab,
    onFilterSelected: (FilterTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        FilterTab.ALL to "전체",
        FilterTab.ANNIVERSARY to "기념일",
        FilterTab.BIRTHDAY to "생일",
        FilterTab.DATE to "데이트",
        FilterTab.IMPORTANT to "중요한 날",
        FilterTab.PAST to "지난 날"
    )

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tabs.forEach { (tab, label) ->
            FilterChip(
                selected = selectedFilter == tab,
                onClick = { onFilterSelected(tab) },
                label = { Text(label, fontSize = 14.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFFFF6B9D),
                    selectedLabelColor = Color.White,
                    containerColor = Color.White,
                    labelColor = Color(0xFF8B4A6B)
                ),
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyAnniversaryState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "등록된 기념일이 없어요",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "하단 + 버튼으로 직접 추가하거나 프로필 데이터를 입력해 자동 기념일을 확인해보세요.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun AnniversaryCard(
    item: AnniversaryItem,
    today: LocalDate,
    onClick: () -> Unit = {},
    onDelete: (Long) -> Unit
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.KOREAN)
    }
    val icon = AnniversaryIcon.fromId(item.icon)
    val category = item.category
    val cardColor = if (item.isAuto) {
        Color(0xFFF5E6F0)
    } else if (item.color.isNotBlank()) {
        try {
            Color(android.graphics.Color.parseColor(item.color))
        } catch (e: Exception) {
            Color(0xFFF5F0E6)
        }
    } else {
        when (category) {
            EventCategory.BIRTHDAY -> Color(0xFFF0E6F5)
            else -> Color(0xFFF5F0E6)
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (!item.isAuto && item.sourceEventId != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(12.dp),
        color = cardColor,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFD700).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = icon.drawableRes),
                    contentDescription = icon.label,
                    modifier = Modifier.size(40.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B4A6B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = cardColor.copy(alpha = 0.8f),
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Text(
                        text = category.displayName,
                        fontSize = 11.sp,
                        color = Color(0xFF8B4A6B).copy(alpha = 0.7f),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = item.date.format(dateFormatter),
                    fontSize = 12.sp,
                    color = Color(0xFF8B4A6B).copy(alpha = 0.6f)
                )
            }

            if (!item.isAuto && item.sourceEventId != null) {
                IconButton(
                    onClick = { 
                        onDelete(item.sourceEventId)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = Color(0xFF8B4A6B).copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddAnniversaryDialog(
    event: Event?,
    onDismiss: () -> Unit,
    onSave: (String, LocalDate, LocalDate?, LocalTime?, LocalTime?, Boolean, String, EventCategory, AnniversaryIcon, String) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("M월 d일 EEEE", Locale.KOREAN)
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("a h:mm", Locale.KOREAN)
    }
    val isEditMode = event != null
    
    // 시간 파싱 헬퍼 함수
    fun parseTime(timeStr: String?): Pair<LocalTime?, LocalTime?>? {
        if (timeStr.isNullOrBlank()) return null
        val parts = timeStr.split("-")
        if (parts.size != 2) return null
        val startParts = parts[0].split(":")
        val endParts = parts[1].split(":")
        if (startParts.size != 2 || endParts.size != 2) return null
        return try {
            Pair(
                LocalTime.of(startParts[0].toInt(), startParts[1].toInt()),
                LocalTime.of(endParts[0].toInt(), endParts[1].toInt())
            )
        } catch (e: Exception) {
            null
        }
    }
    
    var title by remember(event) { mutableStateOf(event?.title ?: "") }
    var memo by remember(event) { mutableStateOf(event?.description ?: "") }
    var startDate by remember(event) {
        mutableStateOf<LocalDate?>(
            event?.date?.toLocalDateOrNull() ?: LocalDate.now()
        )
    }
    var endDate by remember(event) {
        mutableStateOf<LocalDate?>(
            event?.endDate?.takeIf { it.isNotBlank() }?.toLocalDateOrNull()
        )
    }
    var isAllDay by remember(event) {
        mutableStateOf(event?.time.isNullOrBlank())
    }
    var startTime by remember(event) {
        val parsed = parseTime(event?.time)
        mutableStateOf<LocalTime?>(parsed?.first ?: LocalTime.of(15, 30))
    }
    var endTime by remember(event) {
        val parsed = parseTime(event?.time)
        mutableStateOf<LocalTime?>(parsed?.second ?: LocalTime.of(16, 30))
    }
    var selectedCategory by remember(event) {
        mutableStateOf(EventCategory.fromId(event?.category) ?: EventCategory.ANNIVERSARY)
    }
    var selectedIcon by remember(event) {
        mutableStateOf(AnniversaryIcon.fromId(event?.icon) ?: AnniversaryIcon.CAKE)
    }
    var selectedColor by remember(event) {
        mutableStateOf(
            event?.color?.takeIf { it.isNotBlank() } ?: availableColors.first().second
        )
    }
    
    // event가 변경될 때 state 업데이트
    androidx.compose.runtime.LaunchedEffect(event) {
        if (event != null) {
            title = event.title
            memo = event.description
            startDate = event.date.toLocalDateOrNull() ?: LocalDate.now()
            endDate = event.endDate.takeIf { it.isNotBlank() }?.toLocalDateOrNull()
            val parsed = parseTime(event.time)
            isAllDay = event.time.isBlank()
            startTime = parsed?.first ?: LocalTime.of(15, 30)
            endTime = parsed?.second ?: LocalTime.of(16, 30)
            selectedCategory = EventCategory.fromId(event.category) ?: EventCategory.ANNIVERSARY
            selectedIcon = AnniversaryIcon.fromId(event.icon) ?: AnniversaryIcon.CAKE
            selectedColor = event.color.takeIf { it.isNotBlank() } ?: availableColors.first().second
        }
    }
    var showError by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val openStartDatePicker = {
        showStartDatePicker = true
    }
    
    val openEndDatePicker = {
        showEndDatePicker = true
    }
    
    val openStartTimePicker = {
        val time = startTime ?: LocalTime.of(15, 30)
        TimePickerDialog(
            context,
            { _, hour, minute ->
                startTime = LocalTime.of(hour, minute)
            },
            time.hour,
            time.minute,
            false
        ).show()
    }
    
    val openEndTimePicker = {
        val time = endTime ?: LocalTime.of(16, 30)
        TimePickerDialog(
            context,
            { _, hour, minute ->
                endTime = LocalTime.of(hour, minute)
            },
            time.hour,
            time.minute,
            false
        ).show()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEditMode) "기념일 수정" else "기념일 추가") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (showError) showError = false
                    },
                    label = { Text("기념일 이름") },
                    singleLine = true,
                    isError = showError && title.isBlank()
                )

                Column {
                    Text(
                        text = "아이콘",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5D4037)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        AnniversaryIcon.values().forEach { icon ->
                            val isSelected = selectedIcon == icon
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFFFF6B9D).copy(alpha = 0.15f) else Color.Transparent
                                    )
                                    .border(
                                        width = if (isSelected) 0.dp else 1.dp,
                                        color = Color(0xFFE0E0E0),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = icon.drawableRes),
                                    contentDescription = icon.label,
                                    modifier = Modifier.size(40.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                // 시작 날짜/시간
                Column {
                    Text(
                        text = "만나는날",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5D4037)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = openStartDatePicker,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF5D4037)
                            )
                        ) {
                            Text(
                                text = startDate?.format(dateFormatter) ?: "만나는날",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (!isAllDay) {
                            TextButton(
                                onClick = openStartTimePicker,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFE91E63)
                                )
                            ) {
                                Text(
                                    text = startTime?.format(timeFormatter) ?: "시간 선택",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                // 종료 날짜/시간
                Column {
                    Text(
                        text = "끝나는날",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5D4037)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = openEndDatePicker,
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF5D4037)
                            )
                        ) {
                            Text(
                                text = endDate?.format(dateFormatter) ?: startDate?.format(dateFormatter) ?: "끝나는날",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        if (!isAllDay) {
                            TextButton(
                                onClick = openEndTimePicker,
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = Color(0xFFE91E63)
                                )
                            ) {
                                Text(
                                    text = endTime?.format(timeFormatter) ?: "시간 선택",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
                
                if (showError && startDate == null) {
                    Text(
                        text = "시작 날짜를 선택해주세요.",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Column {
                    Text(
                        text = "카테고리",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5D4037)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory.displayName,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                            },
                            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                                focusedTextColor = Color(0xFF5D4037),
                                unfocusedTextColor = Color(0xFF5D4037)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        DropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            EventCategory.values().forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category.displayName) },
                                    onClick = {
                                        selectedCategory = category
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "색상",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF5D4037)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableColors.forEach { (color, hex) ->
                            val isSelected = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(color)
                                    .then(
                                        if (isSelected) {
                                            Modifier.border(
                                                width = 2.dp,
                                                color = Color(0xFFE91E63),
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                        } else {
                                            Modifier
                                        }
                                    )
                                    .clickable { selectedColor = hex }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = memo,
                    onValueChange = { memo = it },
                    label = { Text("메모 (선택)") },
                    minLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank() || startDate == null) {
                        showError = true
                        return@Button
                    }
                    onSave(
                        title.trim(),
                        startDate!!,
                        endDate,
                        if (!isAllDay) startTime else null,
                        if (!isAllDay) endTime else null,
                        isAllDay,
                        memo.trim(),
                        selectedCategory,
                        selectedIcon,
                        selectedColor
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFE91E63)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("저장", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = Color(0xFF5D4037)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("취소")
            }
        }
    )
    
    // 시작 날짜 선택 다이얼로그
    if (showStartDatePicker) {
        val baseDate = startDate ?: LocalDate.now()
        var currentMonth by remember { mutableStateOf(YearMonth.from(baseDate)) }
        var selectedDate by remember { mutableStateOf<LocalDate?>(baseDate) }
        
        Dialog(
            onDismissRequest = { showStartDatePicker = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showStartDatePicker = false },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "만나는날",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = Color(0xFF8B4A6B)
                                )
                                selectedDate?.let {
                                    Text(
                                        text = it.format(dateFormatter),
                                        style = MaterialTheme.typography.bodyLarge.copy(
                                            fontSize = 16.sp
                                        ),
                                        color = Color(0xFFE91E63),
                                        fontWeight = FontWeight.Medium
                                    )
                                } ?: startDate?.let {
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
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "끝나는날",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF5D4037)
                                )
                                endDate?.let {
                                    Text(
                                        text = it.format(dateFormatter),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF5D4037)
                                    )
                                } ?: startDate?.let {
                                    Text(
                                        text = it.format(dateFormatter),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF5D4037)
                                    )
                                } ?: Text(
                                    text = "날짜 선택",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp
                                    ),
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                        
                        // 커스텀 캘린더
                        CustomCalendar(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            startDate = startDate,
                            endDate = endDate,
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
                                onClick = { showStartDatePicker = false },
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
                                        startDate = date
                                        if (endDate == null || endDate!!.isBefore(startDate)) {
                                            endDate = startDate
                                        }
                                        showStartDatePicker = false
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
    
    // 끝나는 날짜 선택 다이얼로그
    if (showEndDatePicker) {
        val baseDate = endDate ?: startDate ?: LocalDate.now()
        var currentMonth by remember { mutableStateOf(YearMonth.from(baseDate)) }
        var selectedDate by remember { mutableStateOf<LocalDate?>(baseDate) }
        
        Dialog(
            onDismissRequest = { showEndDatePicker = false },
            properties = DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable { showEndDatePicker = false },
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
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
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "만나는날",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 20.sp
                                    ),
                                    color = Color(0xFF8B4A6B)
                                )
                                startDate?.let {
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
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "끝나는날",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    ),
                                    color = Color(0xFF5D4037)
                                )
                                selectedDate?.let {
                                    Text(
                                        text = it.format(dateFormatter),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF5D4037)
                                    )
                                } ?: endDate?.let {
                                    Text(
                                        text = it.format(dateFormatter),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF5D4037)
                                    )
                                } ?: startDate?.let {
                                    Text(
                                        text = it.format(dateFormatter),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF5D4037)
                                    )
                                } ?: Text(
                                    text = "날짜 선택",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontSize = 14.sp
                                    ),
                                    color = Color(0xFF9E9E9E)
                                )
                            }
                        }
                        
                        // 커스텀 캘린더
                        CustomCalendar(
                            currentMonth = currentMonth,
                            selectedDate = selectedDate,
                            startDate = startDate,
                            endDate = endDate,
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
                                onClick = { showEndDatePicker = false },
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
                                        if (startDate == null || !date.isBefore(startDate)) {
                                            endDate = date
                                        }
                                        showEndDatePicker = false
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

private data class AnniversaryItem(
    val key: String,
    val title: String,
    val description: String,
    val date: LocalDate,
    val isAuto: Boolean,
    val autoTag: String = "",
    val category: EventCategory = EventCategory.ANNIVERSARY,
    val icon: String = "cake",
    val color: String = "",
    val sourceEventId: Long? = null
)

private fun Event.toAnniversaryItem(): AnniversaryItem? {
    val localDate = date.toLocalDateOrNull() ?: return null
    return AnniversaryItem(
        key = "manual-$id",
        title = title.ifBlank { "기념일" },
        description = description,
        date = localDate,
        isAuto = false,
        category = EventCategory.fromId(category),
        icon = icon,
        color = color,
        sourceEventId = id
    )
}

private fun generateAutoAnniversaries(
    profile: Profile?,
    existingDates: Set<LocalDate>,
    today: LocalDate
): List<AnniversaryItem> {
    val startDate = profile?.relationshipStartDate?.toLocalDateOrNull() ?: return emptyList()
    val limitDate = today.plusYears(2)
    val results = mutableListOf<AnniversaryItem>()

    fun tryAdd(
        date: LocalDate,
        title: String,
        tag: String,
        key: String,
        category: EventCategory = EventCategory.ANNIVERSARY,
        icon: String = "cake"
    ) {
        if (date.isAfter(limitDate)) return
        if (existingDates.contains(date)) return
        results.add(
            AnniversaryItem(
                key = key,
                title = title,
                description = "",
                date = date,
                isAuto = true,
                autoTag = tag,
                category = category,
                icon = icon
            )
        )
    }

    var year = 1
    while (true) {
        val anniversaryDate = startDate.plusYears(year.toLong())
        if (anniversaryDate.isAfter(limitDate)) break
        tryAdd(
            date = anniversaryDate,
            title = "${year}주년",
            tag = "자동·주년",
            key = "auto-year-$year",
            category = EventCategory.ANNIVERSARY,
            icon = "cake"
        )
        year++
    }

    var days = 100L
    while (true) {
        val date = startDate.plusDays(days)
        if (date.isAfter(limitDate)) break
        tryAdd(
            date = date,
            title = "${days}일",
            tag = "자동·${days}일",
            key = "auto-day-$days",
            category = EventCategory.ANNIVERSARY,
            icon = "balloon"
        )
        days += 100
    }

    profile?.birthday?.toLocalDateOrNull()?.let { birthday ->
        val month = birthday.monthValue
        val day = birthday.dayOfMonth
        var yearCursor = today.year - 1
        val endYear = limitDate.year
        while (yearCursor <= endYear) {
            val birthDate = runCatching { LocalDate.of(yearCursor, month, day) }.getOrNull()
            if (birthDate != null && !birthDate.isBefore(startDate)) {
                tryAdd(
                    date = birthDate,
                    title = "연인의 생일",
                    tag = "자동·생일",
                    key = "auto-birthday-$yearCursor",
                    category = EventCategory.BIRTHDAY,
                    icon = "cake"
                )
            }
            yearCursor++
        }
    }

    return results
}

private fun String.toLocalDateOrNull(): LocalDate? {
    if (isBlank()) return null
    return try {
        LocalDate.parse(this)
    } catch (e: DateTimeParseException) {
        null
    }
}

@Composable
private fun CustomCalendar(
    currentMonth: YearMonth,
    selectedDate: LocalDate?,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onMonthChange: (YearMonth) -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val monthYearFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy년 M월", Locale.KOREAN)
    }
    
    val firstDayOfMonth = currentMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek
    val daysInMonth = currentMonth.lengthOfMonth()
    
    // 요일 헤더
    val weekDays = listOf("S", "M", "T", "W", "T", "F", "S")
    
    var showYearMonthPicker by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // 월/년도 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    onMonthChange(currentMonth.minusMonths(1))
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "이전 달",
                    tint = Color(0xFF5D4037)
                )
            }
            
            Text(
                text = currentMonth.format(monthYearFormatter),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                color = Color(0xFF5D4037),
                modifier = Modifier.clickable {
                    showYearMonthPicker = true
                }
            )
            
            IconButton(
                onClick = {
                    onMonthChange(currentMonth.plusMonths(1))
                },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "다음 달",
                    tint = Color(0xFF5D4037)
                )
            }
        }
        
        // 년도/월 선택 다이얼로그
        if (showYearMonthPicker) {
            YearMonthPickerDialog(
                currentMonth = currentMonth,
                onDismiss = { showYearMonthPicker = false },
                onConfirm = { year, month ->
                    onMonthChange(YearMonth.of(year, month))
                    showYearMonthPicker = false
                }
            )
        }
        
        // 요일 헤더
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            weekDays.forEach { day ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 12.sp
                        ),
                        color = Color(0xFF5D4037).copy(alpha = 0.6f)
                    )
                }
            }
        }
        
        // 날짜 그리드
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            val firstDayOffset = (firstDayOfWeek.value % 7)
            var dayCounter = 1
            
            // 6주 표시
            for (week in 0..5) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayOfWeek in 0..6) {
                        val dayIndex = week * 7 + dayOfWeek
                        
                        if (week == 0 && dayOfWeek < firstDayOffset) {
                            // 이전 달의 날짜 (빈 칸)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                            )
                        } else if (dayCounter <= daysInMonth) {
                            val date = currentMonth.atDay(dayCounter)
                            val isSelected = selectedDate == date
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                                    .clickable {
                                        onDateSelected(date)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFFE91E63)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = dayCounter.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp
                                            ),
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Text(
                                        text = dayCounter.toString(),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontSize = 14.sp
                                        ),
                                        color = Color(0xFF5D4037)
                                    )
                                }
                            }
                            dayCounter++
                        } else {
                            // 다음 달의 날짜 (빈 칸)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}


