package com.example.myapplication.ui.screens.calendar

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.TextButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Event
import com.example.myapplication.data.EventCategory
import com.example.myapplication.data.Profile
import com.example.myapplication.ui.screens.anniversary.AddAnniversaryDialog
import com.example.myapplication.ui.screens.anniversary.AnniversaryIcon
import com.example.myapplication.ui.screens.anniversary.YearMonthPickerDialog
import java.time.LocalDate
import java.time.LocalTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlinx.coroutines.launch

data class CalendarAnniversaryItem(
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

@Composable
fun CalendarScreen(
    anniversaryItems: List<CalendarAnniversaryItem>? = null
) {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val eventDao = remember { database.eventDao() }
    val profileDao = remember { database.profileDao() }
    val scope = rememberCoroutineScope()

    val events by remember { eventDao.getAllEvents() }.collectAsState(initial = emptyList())
    val profile by remember { profileDao.getProfile() }.collectAsState(initial = null)

    val today = remember { LocalDate.now() }

    // anniversaryItems가 전달되지 않으면 내부에서 데이터 가져오기
    val combined = remember(anniversaryItems, events, profile, today) {
        if (anniversaryItems != null) {
            anniversaryItems
        } else {
            val manualAnniversaries = events.filter { it.isAnniversary }.mapNotNull { it.toCalendarAnniversaryItem() }
            val manualDates = manualAnniversaries.map { it.date }.toSet()
            val autoAnniversaries = generateAutoAnniversaries(profile, manualDates, today)
            (manualAnniversaries + autoAnniversaries).sortedBy { it.date }
        }
    }

    var currentMonth by remember { mutableStateOf(YearMonth.from(today)) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var editingEvent by remember { mutableStateOf<Event?>(null) }
    var deletingEventId by remember { mutableStateOf<Long?>(null) }
    
    // 날짜별로 기념일을 그룹화 (기간 기념일 포함)
    val eventsByDate = remember(events, combined) {
        val dateMap = mutableMapOf<LocalDate, MutableList<CalendarAnniversaryItem>>()
        
        // 수동으로 추가한 기념일 (기간 포함)
        events.filter { it.isAnniversary }.forEach { event ->
            val startDate = event.date.toLocalDateOrNull() ?: return@forEach
            val endDateStr = event.endDate.takeIf { it.isNotBlank() } ?: event.date
            val endDate = endDateStr.toLocalDateOrNull() ?: startDate
            
            val category = EventCategory.fromId(event.category)
            val eventColor = if (event.color.isNotBlank()) {
                try {
                    Color(android.graphics.Color.parseColor(event.color))
                } catch (e: Exception) {
                    when (category) {
                        EventCategory.BIRTHDAY -> Color(0xFFF0E6F5)
                        else -> Color(0xFFF5F0E6)
                    }
                }
            } else {
                when (category) {
                    EventCategory.BIRTHDAY -> Color(0xFFF0E6F5)
                    else -> Color(0xFFF5F0E6)
                }
            }
            
            // 시작일부터 종료일까지 모든 날짜에 추가
            var currentDate = startDate
            while (!currentDate.isAfter(endDate)) {
                val item = CalendarAnniversaryItem(
                    key = "manual-${event.id}-${currentDate}",
                    title = event.title.ifBlank { "기념일" },
                    description = event.description,
                    date = currentDate,
                    isAuto = false,
                    category = category,
                    icon = event.icon,
                    color = event.color,
                    sourceEventId = event.id
                )
                dateMap.getOrPut(currentDate) { mutableListOf() }.add(item)
                currentDate = currentDate.plusDays(1)
            }
        }
        
        // 자동 생성 기념일 (단일 날짜)
        combined.filter { it.isAuto }.forEach { item ->
            dateMap.getOrPut(item.date) { mutableListOf() }.add(item)
        }
        
        dateMap
    }
    
    // 날짜별 배경색 결정 (첫 번째 기념일의 색상 사용)
    val dateColors = remember(eventsByDate) {
        eventsByDate.mapValues { (_, items) ->
            val firstItem = items.firstOrNull()
            if (firstItem != null) {
                if (firstItem.isAuto) {
                    Color(0xFFF5E6F0)
                } else if (firstItem.color.isNotBlank()) {
                    try {
                        Color(android.graphics.Color.parseColor(firstItem.color))
                    } catch (e: Exception) {
                        when (firstItem.category) {
                            EventCategory.BIRTHDAY -> Color(0xFFF0E6F5)
                            else -> Color(0xFFF5F0E6)
                        }
                    }
                } else {
                    when (firstItem.category) {
                        EventCategory.BIRTHDAY -> Color(0xFFF0E6F5)
                        else -> Color(0xFFF5F0E6)
                    }
                }
            } else {
                Color(0xFFF5F0E6)
            }
        }
    }
    
    // 선택된 날짜의 기념일 목록
    val selectedDateEvents = remember(selectedDate, combined, events) {
        selectedDate?.let { date ->
            combined.filter { item ->
                if (item.isAuto) {
                    // 자동 생성 기념일은 정확한 날짜만
                    item.date == date
                } else {
                    // 수동 기념일은 기간 내에 포함되는지 확인
                    val event = events.find { it.id == item.sourceEventId }
                    if (event != null) {
                        val startDate = event.date.toLocalDateOrNull() ?: return@filter false
                        val endDateStr = event.endDate.takeIf { it.isNotBlank() } ?: event.date
                        val endDate = endDateStr.toLocalDateOrNull() ?: startDate
                        !date.isBefore(startDate) && !date.isAfter(endDate)
                    } else {
                        item.date == date
                    }
                }
            }.distinctBy { it.sourceEventId }
        } ?: emptyList()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5DC))
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        // 달력 표시
        CalendarView(
            currentMonth = currentMonth,
            today = today,
            eventsByDate = eventsByDate,
            dateColors = dateColors,
            selectedDate = selectedDate,
            onMonthChange = { currentMonth = it },
            onDateSelected = { date ->
                selectedDate = if (selectedDate == date) null else date
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 선택된 날짜의 기념일 목록
        if (selectedDate != null && selectedDateEvents.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                selectedDateEvents.forEach { item ->
                    CalendarEventCard(
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
                            deletingEventId = eventId
                        }
                    )
                }
            }
        } else if (selectedDate != null && selectedDateEvents.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                shadowElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
        contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "이 날에는 등록된 기념일이 없습니다.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8B4A6B).copy(alpha = 0.6f)
                    )
                }
            }
        }
    }
    
    // 상세 정보 다이얼로그
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
    
    // 삭제 확인 다이얼로그
    deletingEventId?.let { eventId ->
        AlertDialog(
            onDismissRequest = { deletingEventId = null },
            title = {
                Text(
                    text = "기념일 삭제",
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
                            eventDao.deleteEvent(Event(id = eventId))
                        }
                        deletingEventId = null
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
                    onClick = { deletingEventId = null },
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
private fun CalendarView(
    currentMonth: YearMonth,
    today: LocalDate,
    eventsByDate: Map<LocalDate, List<CalendarAnniversaryItem>>,
    dateColors: Map<LocalDate, Color>,
    selectedDate: LocalDate?,
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
    val weekDays = listOf("일", "월", "화", "수", "목", "금", "토")
    
    var showYearMonthPicker by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 월/년도 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        tint = Color(0xFF8B4A6B)
                    )
                }
                
            Text(
                    text = currentMonth.format(monthYearFormatter),
                    style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color(0xFF8B4A6B),
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
                        tint = Color(0xFF8B4A6B)
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
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 요일 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            ),
                            color = Color(0xFF8B4A6B).copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 날짜 그리드
            Column(
                modifier = Modifier.fillMaxWidth(),
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
                                val isToday = date == today
                                val isSelected = selectedDate == date
                                val hasEvents = eventsByDate.containsKey(date)
                                
                                // 연속된 기념일 날짜 확인
                                val prevDate = if (dayCounter > 1) currentMonth.atDay(dayCounter - 1) else null
                                val nextDate = if (dayCounter < daysInMonth) currentMonth.atDay(dayCounter + 1) else null
                                val hasPrevEvent = prevDate?.let { eventsByDate.containsKey(it) } ?: false
                                val hasNextEvent = nextDate?.let { eventsByDate.containsKey(it) } ?: false
                                
                                // padding 조정: 연속된 날짜는 padding 제거하여 사각형으로 이어지게
                                val startPadding = when {
                                    hasEvents && hasPrevEvent -> 0.dp // 앞에 기념일이 있으면 왼쪽 padding 제거
                                    else -> 4.dp
                                }
                                val endPadding = when {
                                    hasEvents && hasNextEvent -> 0.dp // 뒤에 기념일이 있으면 오른쪽 padding 제거
                                    else -> 4.dp
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .padding(start = startPadding, end = endPadding, top = 4.dp, bottom = 4.dp)
                                        .clickable {
                                            onDateSelected(date)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        // 날짜 텍스트
                                        Text(
                                            text = dayCounter.toString(),
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontSize = 14.sp,
                                                fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                ),
                color = Color(0xFF5D4037)
            )
                                    }
                                    
                                    // 기념일이 있는 날짜 배경 색칠 (형광펜 느낌, 카드 색상과 동일, 이어지는 느낌)
                                    if (hasEvents && !isSelected) {
                                        val backgroundColor = dateColors[date] ?: Color(0xFFF5F0E6)
                                        val shape = when {
                                            hasPrevEvent && hasNextEvent -> RoundedCornerShape(0.dp) // 중간: 직사각형
                                            hasPrevEvent -> RoundedCornerShape(topEnd = 4.dp, bottomEnd = 4.dp) // 끝: 오른쪽만 둥글게
                                            hasNextEvent -> RoundedCornerShape(topStart = 4.dp, bottomStart = 4.dp) // 시작: 왼쪽만 둥글게
                                            else -> RoundedCornerShape(4.dp) // 단독: 모두 둥글게
                                        }
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(shape)
                                                .background(
                                                    backgroundColor.copy(alpha = 0.6f)
                                                )
                                        )
                                    }
                                    
                                    // 선택된 날짜 원 테두리만
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(CircleShape)
                                                .border(
                                                    width = 2.dp,
                                                    color = Color(0xFFE91E63),
                                                    shape = CircleShape
                                                )
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
}

@Composable
private fun CalendarEventCard(
    item: CalendarAnniversaryItem,
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

private fun Event.toCalendarAnniversaryItem(): CalendarAnniversaryItem? {
    val localDate = date.toLocalDateOrNull() ?: return null
    return CalendarAnniversaryItem(
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
): List<CalendarAnniversaryItem> {
    val startDate = profile?.relationshipStartDate?.toLocalDateOrNull() ?: return emptyList()
    val limitDate = today.plusYears(2)
    val results = mutableListOf<CalendarAnniversaryItem>()

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
            CalendarAnniversaryItem(
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
