package com.example.monodiaryapp.data

import android.content.Context
import android.net.Uri
import androidx.room.Database
import androidx.room.ProvidedTypeConverter
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import java.time.LocalDate

@TypeConverters(UriListTypeConverter::class)
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



@ProvidedTypeConverter
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
                )
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}


