package com.example.monodiaryapp.data

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

//다이어리 데이터를 표현하는 엔터티 클래스를 정의
@Entity
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    @ColumnInfo("title") var title: String? = null,
    @ColumnInfo("content") var content: String? = null,
    @ColumnInfo("image") var image: String? = null, // Change the type to String
    @ColumnInfo("songTitle") var songTitle: String? = null,
    @ColumnInfo("artist") var artist: String? = null,
    @ColumnInfo("date") var date: String = "",
)

