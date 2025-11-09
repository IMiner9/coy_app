package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class Memory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: String = "", // 날짜 문자열 (YYYY-MM-DD)
    val title: String = "",
    val description: String = "",
    val photoUri: String = ""
)









