package com.example.monodiaryapp.data

import java.time.LocalDate

data class Diary(
    val uid: Long, // uid 필드 추가
    val title: String,
    val content: String,
    val songTitle: String,
    val artist: String,
    val date: LocalDate
)
