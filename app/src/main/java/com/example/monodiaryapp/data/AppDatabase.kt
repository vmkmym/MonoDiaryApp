package com.example.monodiaryapp.data

import android.content.Context
import android.net.Uri
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters


@Database(entities = [DiaryEntry::class], version = 1, exportSchema = false)
@TypeConverters(UriListTypeConverter::class)
abstract class DiaryDatabase : RoomDatabase() {
    abstract fun diaryDao(): DiaryDao
    companion object {
        @Volatile
        private var INSTANCE: DiaryDatabase? = null
        fun getDatabase(context: Context): DiaryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DiaryDatabase::class.java,
                    "diary_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
// 데이터베이스가 이 타입컨버터를 사용하여 데이터를 변환하게 함
// List<Uri> 타입을 문자열로 변환하고 데이터베이스에 저장하도록 지정 (역변환도 구현)
class UriListTypeConverter {
    @TypeConverter
    fun fromUriList(uriList: List<Uri>): String {
        return uriList.joinToString(separator = ",") { it.toString() }
    }

    @TypeConverter
    fun toUriList(uriListString: String): List<Uri> {
        return uriListString.split(",").map { Uri.parse(it) }
    }
}