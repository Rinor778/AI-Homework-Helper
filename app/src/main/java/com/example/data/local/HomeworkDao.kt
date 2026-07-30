package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework_items ORDER BY timestamp DESC")
    fun getAllHomework(): Flow<List<HomeworkItem>>

    @Query("SELECT * FROM homework_items WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteHomework(): Flow<List<HomeworkItem>>

    @Query("SELECT * FROM homework_items WHERE id = :id")
    suspend fun getHomeworkById(id: Long): HomeworkItem?

    @Query("SELECT * FROM homework_items WHERE question LIKE '%' || :query || '%' OR explanation LIKE '%' || :query || '%' OR subject LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchHomework(query: String): Flow<List<HomeworkItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(item: HomeworkItem): Long

    @Query("UPDATE homework_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavoriteStatus(id: Long, isFavorite: Boolean)

    @Delete
    suspend fun deleteHomework(item: HomeworkItem)

    @Query("DELETE FROM homework_items")
    suspend fun clearAll()
}
