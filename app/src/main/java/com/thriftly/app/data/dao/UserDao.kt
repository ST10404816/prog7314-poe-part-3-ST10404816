package com.thriftly.app.data.dao

import androidx.room.*
import com.thriftly.app.data.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    // Stream a user by their unique ID
    @Query("SELECT * FROM users WHERE uid = :uid LIMIT 1")
    fun stream(uid: String): Flow<User?>

    // Insert or update a user
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(user: User)
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/