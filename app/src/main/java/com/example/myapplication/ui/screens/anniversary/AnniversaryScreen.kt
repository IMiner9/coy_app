package com.example.myapplication.ui.screens.anniversary

import android.app.DatePickerDialog
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
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
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
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
            onSave = { title, date, memo, category, icon, color ->
                scope.launch {
                    eventDao.insertEvent(
                        Event(
                            title = title,
                            description = memo,
                            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
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
            onSave = { title, date, memo, category, icon, color ->
                scope.launch {
                    eventDao.updateEvent(
                        event.copy(
                            title = title,
                            description = memo,
                            date = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
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
    onSave: (String, LocalDate, String, EventCategory, AnniversaryIcon, String) -> Unit
) {
    val context = LocalContext.current
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN)
    }
    val isEditMode = event != null
    
    var title by remember(event) { mutableStateOf(event?.title ?: "") }
    var memo by remember(event) { mutableStateOf(event?.description ?: "") }
    var selectedDate by remember(event) {
        mutableStateOf<LocalDate?>(
            event?.date?.toLocalDateOrNull() ?: null
        )
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
            selectedDate = event.date.toLocalDateOrNull()
            selectedCategory = EventCategory.fromId(event.category) ?: EventCategory.ANNIVERSARY
            selectedIcon = AnniversaryIcon.fromId(event.icon) ?: AnniversaryIcon.CAKE
            selectedColor = event.color.takeIf { it.isNotBlank() } ?: availableColors.first().second
        }
    }
    var showError by remember { mutableStateOf(false) }
    var categoryExpanded by remember { mutableStateOf(false) }

    val openDatePicker = remember(selectedDate) {
        {
            val base = selectedDate ?: LocalDate.now()
            DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                },
                base.year,
                base.monthValue - 1,
                base.dayOfMonth
            ).show()
        }
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
                    Text(text = "아이콘", style = MaterialTheme.typography.labelMedium)
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
                                    .size(if (isSelected) 56.dp else 52.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color(0xFFFF6B9D).copy(alpha = 0.2f) else Color(0xFFFFD700).copy(alpha = 0.1f)
                                    )
                                    .padding(if (isSelected) 3.dp else 2.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color.White)
                                    .clickable { selectedIcon = icon },
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = icon.drawableRes),
                                    contentDescription = icon.label,
                                    modifier = Modifier.size(if (isSelected) 40.dp else 36.dp),
                                    contentScale = ContentScale.Fit
                                )
                            }
                        }
                    }
                }

                Column {
                    Text(text = "날짜", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextButton(onClick = openDatePicker) {
                        Text(
                            text = selectedDate?.format(dateFormatter) ?: "날짜 선택",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    if (showError && selectedDate == null) {
                        Text(
                            text = "날짜를 선택해주세요.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Column {
                    Text(text = "카테고리", style = MaterialTheme.typography.labelMedium)
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
                    Text(text = "색상", style = MaterialTheme.typography.labelMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        availableColors.forEach { (color, hex) ->
                            val isSelected = selectedColor == hex
                            Box(
                                modifier = Modifier
                                    .size(if (isSelected) 44.dp else 40.dp)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        if (isSelected) Color.White else Color.Transparent
                                    )
                                    .padding(if (isSelected) 2.dp else 0.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(color)
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
            TextButton(
                onClick = {
                    val picked = selectedDate
                    if (title.isBlank() || picked == null) {
                        showError = true
                        return@TextButton
                    }
                    onSave(title.trim(), picked, memo.trim(), selectedCategory, selectedIcon, selectedColor)
                }
            ) {
                Text("저장")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
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


