package com.example.monodiaryapp.data

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

//다이어리 데이터를 표현하는 엔터티 클래스를 정의
@Entity
@TypeConverters(UriListTypeConverter::class)
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val uid: Long = 0,
    @ColumnInfo("title") var title: String,
    @ColumnInfo("content") var content: String,
    @ColumnInfo(name = "image") var image: List<Uri>,
    @ColumnInfo("bgm") var bgm: String,
    @ColumnInfo("date") var date: String,
)

