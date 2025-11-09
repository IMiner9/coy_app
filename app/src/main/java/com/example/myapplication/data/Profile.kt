package com.example.myapplication.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String = "",
    val nickname: String = "",
    val relationshipStartDate: String = "", // 사귀기 시작한 날 (예: "2024-01-01")
    val birthday: String = "",
    val phoneNumber: String = "",
    val mbti: String = "",
    val photoUri: String = "",
    val favorites: String = "", // 좋아하는 것들 (예: "커피향, 강아지, 저녁 산책을 좋아해요 🐶🌆")
    val hobbies: String = "", // 취향 아이콘들 (예: "🎵☕🌸")
    val mood: String = "", // 현재 기분 이모티콘 (예: "😊")
    val note: String = "" // 연인에게 한 줄 메모
)



