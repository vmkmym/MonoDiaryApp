package com.example.monodiaryapp.data

import android.content.Context
import android.net.Uri
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

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


/* 이미지의 Uri들을 String 형태로 변환하여 저장하고,
필요할 때 다시 Uri로 변환하여 사용하는 것이 일반적인 방법입니다.
타입 컨버터를 이용하여 Uri 리스트를 String으로 변환하고
다시 String을 Uri 리스트로 변환하는 방식으로 해결 */
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

/* @TypeConverter를 사용하여 LocalDate를 데이터베이스에서 사용할 수 있는 형태로 변환 */
class LocalDateTypeConverter {
    @TypeConverter
    fun fromLocalDate(localDate: LocalDate): String {
        return localDate.toString()
    }

    @TypeConverter
    fun toLocalDate(localDateString: String): LocalDate {
        return LocalDate.parse(localDateString)
    }
}
