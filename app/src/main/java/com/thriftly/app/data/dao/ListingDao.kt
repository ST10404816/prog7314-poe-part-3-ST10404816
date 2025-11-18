package com.thriftly.app.data.dao

import androidx.room.*
import com.thriftly.app.data.entity.Listing
import kotlinx.coroutines.flow.Flow

@Dao
interface ListingDao {
    // Stream all published listings
    @Query("SELECT * FROM listings WHERE isDraft = 0 ORDER BY createdAt DESC")
    fun streamAll(): Flow<List<Listing>>

    // Get a single listing by ID
    @Query("SELECT * FROM listings WHERE id = :id LIMIT 1")
    suspend fun get(id: String): Listing?

    // Search listings by title and optional category filter
    @Query("SELECT * FROM listings WHERE (title LIKE '%' || :q || '%') AND (:category = '' OR category = :category) AND isDraft = 0 ORDER BY createdAt DESC")
    fun search(q: String, category: String): Flow<List<Listing>>

    // Insert or update a listing
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(listing: Listing)

    // Update an existing listing
    @Update suspend fun update(listing: Listing)

    // Toggle favorite status for a listing
    @Query("UPDATE listings SET isFavorite = :fav WHERE id = :id")
    suspend fun setFavorite(id: String, fav: Boolean)
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/