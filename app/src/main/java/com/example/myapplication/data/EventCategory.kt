package com.example.myapplication.data

/**
 * 사용자가 정의하는 기념일/일정의 유형을 나타냅니다.
 *
 * 저장소에는 정수 ID만 저장하고, 화면에서는 이를 사람이 읽을 수 있는 라벨로 바꿔서 사용합니다.
 */
enum class EventCategory(val id: Int, val displayName: String) {
    ANNIVERSARY(1, "기념일"),
    BIRTHDAY(2, "생일"),
    DATE(3, "데이트"),
    IMPORTANT(4, "중요한 날");

    companion object {
        fun fromId(id: Int?): EventCategory {
            return values().firstOrNull { it.id == id } ?: ANNIVERSARY
        }
    }
}



