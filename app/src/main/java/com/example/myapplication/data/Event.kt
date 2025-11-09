package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val date: String = "", // 시작 날짜 문자열 (YYYY-MM-DD)
    val endDate: String = "", // 종료 날짜 문자열 (YYYY-MM-DD), 비어있으면 단일 일정
    val time: String = "", // 시간 문자열 (HH:mm)
    val isAnniversary: Boolean = false, // 기념일 여부
    val notifyEnabled: Boolean = false, // 알림 활성화 여부
    val color: String = "" // 이벤트 테마 색상 (HEX)
)








