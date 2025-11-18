package com.thriftly.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

// Represents a marketplace listing for secondhand clothing
@Entity(tableName = "listings")
data class Listing(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val price: Double,
    val category: String,
    val size: String,
    val condition: String,
    val description: String,
    val imageUris: List<String> = emptyList(),
    val sellerName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isDraft: Boolean = false,
    val isFavorite: Boolean = false
)

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/