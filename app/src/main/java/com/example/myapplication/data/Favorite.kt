package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class Favorite(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String = "", // 음식, 취미, 장소 등
    val title: String = "",
    val description: String = "",
    val photoUri: String = "",
    val isDislike: Boolean = false // 좋아하는 것(false) 또는 싫어하는 것(true)
)



