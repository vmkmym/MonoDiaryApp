package com.example.monodiaryapp.data

import java.time.LocalDate

data class Diary(
    val uid: Long,
    val title: String,
    val content: String,
    val songTitle: String,
    val artist: String,
    val date: LocalDate
)
