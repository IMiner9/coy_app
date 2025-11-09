package com.example.myapplication.ui.screens.calendar

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.Event
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val database = remember { AppDatabase.getDatabase(context) }
    val eventDao = remember { database.eventDao() }
    val profileDao = remember { database.profileDao() }
    val scope = rememberCoroutineScope()
    
    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var events by remember { mutableStateOf<List<Event>>(emptyList()) }
    var relationshipStartDate by remember { mutableStateOf<LocalDate?>(null) }
    var showAddEventDialog by remember { mutableStateOf(false) }
    var selectedDateEvents by remember { mutableStateOf<List<Event>>(emptyList()) }
    
    // 프로필에서 사귀기 시작한 날 가져오기
    LaunchedEffect(Unit) {
        try {
            val profile = profileDao.getProfile().first()
            if (profile?.relationshipStartDate?.isNotEmpty() == true) {
                try {
                    relationshipStartDate = LocalDate.parse(
                        profile.relationshipStartDate,
                        DateTimeFormatter.ISO_LOCAL_DATE
                    )
                } catch (e: Exception) {
                    relationshipStartDate = null
                }
            }
        } catch (e: Exception) {
            relationshipStartDate = null
        }
    }
    
    // 이벤트 불러오기
    LaunchedEffect(Unit) {
        try {
            eventDao.getAllEvents().collect { eventList ->
                events = eventList
            }
        } catch (e: Exception) {
            events = emptyList()
        }
    }
    
    // 자동 기념일 계산
    val autoAnniversaries = remember(relationshipStartDate) {
        relationshipStartDate?.let { startDate ->
            calculateAutoAnniversariesForCalendar(startDate)
        } ?: emptyList()
    }
    
    // 전체 이벤트 (일반 이벤트 + 자동 기념일)
    val allEvents = remember(events, autoAnniversaries) {
        events + autoAnniversaries
    }
    
    // 선택된 날짜의 이벤트 필터링
    LaunchedEffect(selectedDate, allEvents) {
        selectedDateEvents = selectedDate?.let { date ->
            allEvents.filter { event ->
                event.resolveDateRange()?.let { (start, end) ->
                    !date.isBefore(start) && !date.isAfter(end)
                } ?: false
            }.sortedWith(
                compareBy<Event> { event ->
                    event.resolveDateRange()?.first ?: LocalDate.MAX
                }.thenBy { it.time }
            )
        } ?: emptyList()
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFF9F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
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
                        text = "캘린더",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        ),
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = "📅",
                        fontSize = 20.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "우리의 일정을 기록해요 ✨",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF5D4037)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 연도/월 선택
            YearMonthSelector(
                currentYearMonth = currentYearMonth,
                onYearMonthChange = { currentYearMonth = it }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 달력
            CalendarGrid(
                yearMonth = currentYearMonth,
                selectedDate = selectedDate,
                events = allEvents,
                onDateSelected = { date ->
                    selectedDate = if (selectedDate == date) null else date
                }
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // 선택된 날짜의 이벤트 목록
            if (selectedDate != null) {
                Text(
                    text = "${selectedDate!!.format(DateTimeFormatter.ofPattern("M월 d일"))} 일정",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color(0xFF5D4037)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                if (selectedDateEvents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "일정이 없습니다",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5D4037).copy(alpha = 0.5f)
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(selectedDateEvents) { event ->
                            EventCard(
                                event = event,
                                onDelete = if (event.id > 0) {
                                    {
                                        scope.launch {
                                            try {
                                                eventDao.deleteEvent(event)
                                            } catch (e: Exception) {
                                                e.printStackTrace()
                                            }
                                        }
                                    }
                                } else null
                            )
                        }
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "날짜를 선택해주세요",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF5D4037).copy(alpha = 0.5f)
                    )
                }
            }
        }
        
        FloatingActionButton(
            onClick = { showAddEventDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp),
            containerColor = Color(0xFFE91E63),
            contentColor = Color.White
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "일정 추가"
            )
        }
    }
    
    // 일정 추가 다이얼로그
    if (showAddEventDialog) {
        AddEventDialog(
            initialDate = selectedDate,
            onDismiss = { showAddEventDialog = false },
            onAdd = { event ->
                scope.launch {
                    try {
                        eventDao.insertEvent(event)
                        showAddEventDialog = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        )
    }
}

@Composable
fun YearMonthSelector(
    currentYearMonth: YearMonth,
    onYearMonthChange: (YearMonth) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    val newYearMonth = currentYearMonth.minusMonths(1)
                    if (newYearMonth.year >= 2025) {
                        onYearMonthChange(newYearMonth)
                    }
                },
                enabled = currentYearMonth > YearMonth.of(2025, 1)
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "이전 달",
                    tint = Color(0xFF5D4037)
                )
            }
            
            Text(
                text = "${currentYearMonth.year}년 ${currentYearMonth.monthValue}월",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color(0xFF5D4037)
            )
            
            IconButton(
                onClick = {
                    val newYearMonth = currentYearMonth.plusMonths(1)
                    if (newYearMonth.year <= 2035) {
                        onYearMonthChange(newYearMonth)
                    }
                },
                enabled = currentYearMonth < YearMonth.of(2035, 12)
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "다음 달",
                    tint = Color(0xFF5D4037)
                )
            }
        }
    }
}

@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    events: List<Event>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // 일요일을 0으로
    val daysInMonth = yearMonth.lengthOfMonth()
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // 요일 헤더
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = when (day) {
                                "일" -> Color(0xFFE91E63)
                                "토" -> Color(0xFF2196F3)
                                else -> Color(0xFF5D4037)
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 날짜 그리드
            var dayCounter = 1
            for (week in 0..5) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (dayOfWeek in 0..6) {
                        val dayIndex = week * 7 + dayOfWeek
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayIndex >= firstDayOfWeek && dayCounter <= daysInMonth) {
                                val date = yearMonth.atDay(dayCounter)
                                val dateEvents = events.filter { event ->
                                    event.resolveDateRange()?.let { (start, end) ->
                                        !date.isBefore(start) && !date.isAfter(end)
                                    } ?: false
                                }
                                val hasEvent = dateEvents.isNotEmpty()
                                val hasAnniversary = dateEvents.any { it.isAnniversary }
                                val hasRegularEvent = dateEvents.any { !it.isAnniversary }
                                val isSelected = date == selectedDate
                                val isToday = date == LocalDate.now()

                                val regularColors = dateEvents
                                    .filter { !it.isAnniversary }
                                    .map { it.resolveEventColor() }
                                val primaryRegularColor = regularColors.firstOrNull()
                                val anniversaryBaseColor = parseColorOrFallback(
                                    ANNIVERSARY_COLOR_HEX,
                                    Color(0xFFFFE0E0)
                                ).copy(alpha = 0.45f)
                                val regularBaseColor = primaryRegularColor
                                    ?.copy(alpha = 0.35f)
                                    ?: DEFAULT_REGULAR_EVENT_COLOR
                                val mixedBaseColor = if (primaryRegularColor != null) {
                                    primaryRegularColor.copy(alpha = 0.25f)
                                } else {
                                    Color(0xFFFFD0E0)
                                }
                                
                                val multiDayRanges = dateEvents.mapNotNull { it.resolveDateRange() }
                                    .filter { it.first < it.second }
                                val isRangeStart = multiDayRanges.any { it.first == date }
                                val isRangeEnd = multiDayRanges.any { it.second == date }
                                val isRangeMiddle = multiDayRanges.any { date.isAfter(it.first) && date.isBefore(it.second) }
                                
                                val baseColor = when {
                                    isSelected -> Color(0xFFE91E63)
                                    hasAnniversary && hasRegularEvent -> mixedBaseColor
                                    hasAnniversary -> anniversaryBaseColor
                                    hasRegularEvent -> regularBaseColor
                                    isToday -> Color(0xFFE91E63).copy(alpha = 0.12f)
                                    else -> Color.Transparent
                                }
                                
                                val rangeShape = when {
                                    isSelected || !hasEvent -> RoundedCornerShape(16.dp)
                                    isRangeStart && isRangeEnd -> RoundedCornerShape(16.dp)
                                    isRangeStart -> RoundedCornerShape(
                                        topStart = 16.dp,
                                        topEnd = 0.dp,
                                        bottomStart = 16.dp,
                                        bottomEnd = 0.dp
                                    )
                                    isRangeEnd -> RoundedCornerShape(
                                        topStart = 0.dp,
                                        topEnd = 16.dp,
                                        bottomStart = 0.dp,
                                        bottomEnd = 16.dp
                                    )
                                    isRangeMiddle -> RoundedCornerShape(0.dp)
                                    else -> RoundedCornerShape(16.dp)
                                }
                                
                                val cellModifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 2.dp, vertical = 4.dp)
                                    .clip(rangeShape)
                                    .background(baseColor)
                                    .clickable { onDateSelected(date) }
                                
                                Box(
                                    modifier = cellModifier,
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isToday && !isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .matchParentSize()
                                                .clip(rangeShape)
                                                .border(
                                                    width = 1.dp,
                                                    color = Color(0xFFE91E63).copy(alpha = 0.35f),
                                                    shape = rangeShape
                                                )
                                        )
                                    }
                                    
                                    Text(
                                        text = dayCounter.toString(),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = when {
                                            isSelected -> Color.White
                                            dayOfWeek == 0 -> Color(0xFFE91E63)
                                            dayOfWeek == 6 -> Color(0xFF2196F3)
                                            else -> Color(0xFF5D4037)
                                        },
                                        fontWeight = if (isSelected || hasEvent) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                                
                                dayCounter++
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EventCard(
    event: Event,
    onDelete: (() -> Unit)?
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val dateRange = event.resolveDateRange()
    val dateText = dateRange?.let { (start, end) ->
        val formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
        if (start == end) {
            start.format(formatter)
        } else {
            "${start.format(formatter)} ~ ${end.format(formatter)}"
        }
    }
    
    val eventColor = event.resolveEventColor()
    val cardColor = if (event.isAnniversary) {
        eventColor.copy(alpha = 0.35f)
    } else {
        eventColor.copy(alpha = 0.25f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardColor
        ),
        border = BorderStroke(
            width = 1.dp,
            color = eventColor.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(12.dp))
                    .background(eventColor)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 기념일 아이콘
                if (event.isAnniversary) {
                    Icon(
                        Icons.Default.Cake,
                        contentDescription = "기념일",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = Color(0xFF5D4037)
                    )
                    dateText?.let {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037).copy(alpha = 0.7f)
                        )
                    }
                    if (event.description.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = event.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF5D4037).copy(alpha = 0.7f)
                        )
                    }
                }
            }
            
            // 삭제 버튼 (자동 생성된 기념일은 삭제 불가)
            if (onDelete != null) {
                Spacer(modifier = Modifier.width(12.dp))
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "삭제",
                        tint = Color(0xFFE91E63),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
    
    if (showDeleteConfirm && onDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(if (event.isAnniversary) "기념일 삭제" else "일정 삭제") },
            text = { Text(if (event.isAnniversary) "이 기념일을 삭제하시겠습니까?" else "이 일정을 삭제하시겠습니까?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    }
                ) {
                    Text("삭제")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("취소")
                }
            }
        )
    }
}

// 캘린더용 자동 기념일 계산 함수
fun calculateAutoAnniversariesForCalendar(startDate: LocalDate): List<Event> {
    val today = LocalDate.now()
    val anniversaries = mutableListOf<Event>()
    
    // 100일, 200일, 300일, ... 단위 기념일
    for (days in 100..1000 step 100) {
        val anniversaryDate = startDate.plusDays(days.toLong())
        if (!anniversaryDate.isBefore(today.minusYears(1))) { // 1년 전까지 표시
            anniversaries.add(
                Event(
                    id = 0, // 자동 생성된 이벤트는 id = 0
                    title = "${days}일",
                    date = anniversaryDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate = anniversaryDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    isAnniversary = true,
                    color = ANNIVERSARY_COLOR_HEX
                )
            )
        }
    }
    
    // 주년 기념일 (1주년, 2주년, ...)
    for (year in 1..10) {
        val anniversaryDate = startDate.plusYears(year.toLong())
        if (!anniversaryDate.isBefore(today.minusYears(1))) { // 1년 전까지 표시
            anniversaries.add(
                Event(
                    id = 0, // 자동 생성된 이벤트는 id = 0
                    title = "${year}주년",
                    date = anniversaryDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    endDate = anniversaryDate.format(DateTimeFormatter.ISO_LOCAL_DATE),
                    isAnniversary = true,
                    color = ANNIVERSARY_COLOR_HEX
                )
            )
        }
    }
    
    return anniversaries
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEventDialog(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onAdd: (Event) -> Unit
) {
    val zoneId = ZoneId.systemDefault()
    val isoFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    val displayFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일")
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf(initialDate ?: LocalDate.now()) }
    var durationInput by remember { mutableStateOf("1") }
    var selectedColor by remember { mutableStateOf(EVENT_COLOR_OPTIONS.first()) }
    var showStartDatePicker by remember { mutableStateOf(false) }
    
    val durationDays = durationInput.toIntOrNull()?.coerceAtLeast(1) ?: 1
    val endDate = startDate.plusDays((durationDays - 1).coerceAtLeast(0).toLong())
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "일정 추가",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("일정 제목") },
                    placeholder = { Text("예: 데이트, 기념일") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                OutlinedTextField(
                    value = startDate.format(displayFormatter),
                    onValueChange = { },
                    label = { Text("시작일") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showStartDatePicker = true },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Default.CalendarMonth,
                            contentDescription = "시작일 선택",
                            tint = Color(0xFFE91E63)
                        )
                    }
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    IconButton(
                        onClick = {
                            val current = durationDays
                            if (current > 1) {
                                durationInput = (current - 1).toString()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Default.Remove,
                            contentDescription = "기간 줄이기",
                            tint = Color(0xFFE91E63)
                        )
                    }
                    
                    OutlinedTextField(
                        value = durationInput,
                        onValueChange = { value ->
                            if (value.isBlank()) {
                                durationInput = ""
                            } else if (value.all { it.isDigit() }) {
                                durationInput = value.take(3)
                            }
                        },
                        label = { Text("기간 (일)") },
                        placeholder = { Text("예: 3") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        trailingIcon = {
                            Text(
                                text = "일",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF5D4037).copy(alpha = 0.7f)
                            )
                        }
                    )
                    
                    IconButton(
                        onClick = {
                            val current = durationDays
                            val next = (current + 1).coerceAtMost(999)
                            durationInput = next.toString()
                        }
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "기간 늘리기",
                            tint = Color(0xFFE91E63)
                        )
                    }
                }
                
                Text(
                    text = if (durationDays <= 1) {
                        "기간: ${startDate.format(displayFormatter)}"
                    } else {
                        "기간: ${startDate.format(displayFormatter)} ~ ${endDate.format(displayFormatter)}"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF5D4037).copy(alpha = 0.7f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "색상 선택",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = Color(0xFF5D4037)
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(EVENT_COLOR_OPTIONS) { colorHex ->
                        val color = parseColorOrFallback(colorHex, Color(0xFFFFF4E0))
                        val isSelected = selectedColor == colorHex

                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 40.dp else 36.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    width = if (isSelected) 3.dp else 1.dp,
                                    color = if (isSelected) Color.White else Color(0x335D4037),
                                    shape = CircleShape
                                )
                                .clickable { selectedColor = colorHex },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(0xFF5D4037),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("메모 (선택)") },
                    placeholder = { Text("일정에 대한 메모를 입력하세요") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    maxLines = 4
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (title.isNotEmpty()) {
                        onAdd(
                            Event(
                                title = title,
                                description = description,
                                date = startDate.format(isoFormatter),
                                endDate = endDate.format(isoFormatter),
                                color = selectedColor
                            )
                        )
                    }
                },
                enabled = title.isNotEmpty()
            ) {
                Text("추가")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
    
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.atStartOfDay(zoneId).toInstant().toEpochMilli()
        )
        
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newStart = Instant.ofEpochMilli(millis)
                                .atZone(zoneId)
                                .toLocalDate()
                            startDate = newStart
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("확인")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("취소")
                }
            }
        ) {
            DatePicker(
                state = datePickerState,
                title = null,
                headline = null
            )
        }
    }
}

private fun Event.resolveDateRange(): Pair<LocalDate, LocalDate>? {
    val formatter = DateTimeFormatter.ISO_LOCAL_DATE
    val start = runCatching { LocalDate.parse(date, formatter) }.getOrNull() ?: return null
    val end = if (endDate.isNotBlank()) {
        runCatching { LocalDate.parse(endDate, formatter) }.getOrNull()
    } else {
        null
    } ?: start
    val normalizedEnd = if (end.isBefore(start)) start else end
    return start to normalizedEnd
}

private fun Event.resolveEventColor(): Color {
    val fallback = if (isAnniversary) {
        parseColorOrFallback(ANNIVERSARY_COLOR_HEX, Color(0xFFFFE0E0))
    } else {
        DEFAULT_REGULAR_EVENT_COLOR
    }
    val hex = when {
        color.isNotBlank() -> color
        isAnniversary -> ANNIVERSARY_COLOR_HEX
        else -> DEFAULT_EVENT_COLOR_HEX
    }
    return parseColorOrFallback(hex, fallback)
}

private fun parseColorOrFallback(hex: String, fallback: Color): Color {
    return runCatching { Color(AndroidColor.parseColor(hex)) }.getOrElse { fallback }
}

private val DEFAULT_REGULAR_EVENT_COLOR = Color(0xFFFFF4E0)
private const val ANNIVERSARY_COLOR_HEX = "#FFE0E0"
private const val DEFAULT_EVENT_COLOR_HEX = "#FFE0B2"
private val EVENT_COLOR_OPTIONS = listOf(
    "#FFCDD2",
    "#F8BBD0",
    "#E1BEE7",
    "#D1C4E9",
    "#C5CAE9",
    "#BBDEFB",
    "#B2EBF2",
    "#C8E6C9",
    "#FFF9C4",
    "#FFE0B2",
    "#FFCCBC"
)
