package com.example.monodiaryapp.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// 다이어리 엔터티의 조작을 위한 DAO 인터페이스를 생성
@Dao
interface DiaryDao {
    @Query("SELECT * FROM DiaryEntry ORDER BY date DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM DiaryEntry WHERE uid = :entryId")
    fun getEntryById(entryId: Long): Flow<DiaryEntry?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DiaryEntry)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(diaryEntry: DiaryEntry)

    @Delete
    suspend fun deleteEntry(entry: DiaryEntry)

    @Update
    suspend fun updateEntry(entry: DiaryEntry)
}

