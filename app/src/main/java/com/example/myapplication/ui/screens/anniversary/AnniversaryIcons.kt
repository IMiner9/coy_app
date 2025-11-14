package com.example.myapplication.ui.screens.anniversary

import androidx.annotation.DrawableRes
import com.example.myapplication.R

enum class AnniversaryIcon(
    val id: String,
    val label: String,
    @DrawableRes val drawableRes: Int
) {
    CAKE("cake", "케이크", R.drawable.anniv_cake),
    BALLOON("balloon", "풍선", R.drawable.anniv_balloon),
    HEART("heart", "하트", R.drawable.anniv_heart),
    STAR("star", "별", R.drawable.anniv_star),
    FOOD("food", "맛있는 날", R.drawable.anniv_food),
    DRINK("drink", "카페/술", R.drawable.anniv_drink),
    MOVIE("movie", "영화/공연", R.drawable.anniv_movie),
    MUSIC("music", "음악", R.drawable.anniv_music_note),
    HOBBY("hobby", "취미", R.drawable.anniv_hobby),
    CHAT("chat", "대화", R.drawable.anniv_speech_bubble),
    BURGER("burger", "간편식", R.drawable.anniv_burger);

    companion object {
        fun fromId(id: String?): AnniversaryIcon {
            return values().firstOrNull { it.id == id } ?: CAKE
        }
    }
}



