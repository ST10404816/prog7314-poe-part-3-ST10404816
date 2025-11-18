package com.thriftly.app

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface OfflineDao {
    @Insert
    suspend fun insert(action: OfflineAction): Long

    @Update
    suspend fun update(action: OfflineAction)

    @Query("SELECT * FROM offline_actions ORDER BY timestamp DESC")
    fun getAll(): Flow<List<OfflineAction>>

    @Query("SELECT * FROM offline_actions WHERE synced = 0 ORDER BY timestamp ASC")
    suspend fun getPendingOnce(): List<OfflineAction>

    @Query("DELETE FROM offline_actions WHERE id = :id")
    suspend fun deleteById(id: Long)
}

/* 
References 

Android Developers. 2025. Room Database Guide. [Online]. Available at: https://developer.android.com/training/data-storage/room [Accessed 17 Nov 2025].
*/
