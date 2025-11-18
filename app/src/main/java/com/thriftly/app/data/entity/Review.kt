package com.thriftly.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "reviews")
data class Review(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sellerId: String,              // ID of seller being reviewed
    val buyerId: String,               // ID of buyer who wrote review  
    val orderId: String? = null,       // Optional: link to specific transaction
    val rating: Int,                   // Star rating 1-5
    val comment: String = "",          // Optional written review
    val createdAt: Long = System.currentTimeMillis(),
    val isVerifiedPurchase: Boolean = false  // True if linked to actual transaction
)

@Entity(tableName = "seller_ratings")
data class SellerRating(
    @PrimaryKey val sellerId: String,
    val averageRating: Float,          // Average of all ratings (e.g., 4.3)
    val totalReviews: Int,             // Total number of reviews
    val oneStarCount: Int = 0,         // Breakdown by rating
    val twoStarCount: Int = 0,
    val threeStarCount: Int = 0,
    val fourStarCount: Int = 0,
    val fiveStarCount: Int = 0,
    val lastUpdated: Long = System.currentTimeMillis()
)

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/