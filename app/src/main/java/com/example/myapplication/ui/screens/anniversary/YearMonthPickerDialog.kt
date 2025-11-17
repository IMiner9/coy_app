package com.example.myapplication.ui.screens.anniversary

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.time.YearMonth

@Composable
fun YearMonthPickerDialog(
    currentMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit
) {
    var selectedYear by remember { mutableStateOf(currentMonth.year) }
    var selectedMonth by remember { mutableStateOf(currentMonth.monthValue) }
    
    val years = (2000..2100).toList()
    val months = (1..12).toList()
    val monthNames = listOf("1월", "2월", "3월", "4월", "5월", "6월", "7월", "8월", "9월", "10월", "11월", "12월")
    
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable { onDismiss() },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clickable(enabled = false) { }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Text(
                        text = "년도와 월을 선택하세요",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = Color(0xFF5D4037),
                        modifier = Modifier.padding(bottom = 20.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 년도 선택
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "년도",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF5D4037),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF5F5F5),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    items(years.size) { index ->
                                        val year = years[index]
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (selectedYear == year) Color(0xFFE91E63).copy(alpha = 0.2f) else Color.Transparent
                                                )
                                                .clickable { selectedYear = year }
                                                .padding(vertical = 8.dp, horizontal = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "${year}년",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (selectedYear == year) Color(0xFFE91E63) else Color(0xFF5D4037),
                                                fontWeight = if (selectedYear == year) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        
                        // 월 선택
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "월",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color(0xFF5D4037),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFF5F5F5),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    items(months.size) { index ->
                                        val month = months[index]
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(
                                                    if (selectedMonth == month) Color(0xFFE91E63).copy(alpha = 0.2f) else Color.Transparent
                                                )
                                                .clickable { selectedMonth = month }
                                                .padding(vertical = 8.dp, horizontal = 12.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = monthNames[index],
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = if (selectedMonth == month) Color(0xFFE91E63) else Color(0xFF5D4037),
                                                fontWeight = if (selectedMonth == month) FontWeight.Bold else FontWeight.Normal
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    // 확인/취소 버튼
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onDismiss,
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
                                onConfirm(selectedYear, selectedMonth)
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

