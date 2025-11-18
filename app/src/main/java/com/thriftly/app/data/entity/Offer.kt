package com.thriftly.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "offers")
data class Offer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val listingId: String,             // Which item this offer is for
    val buyerId: String,               // Who made the offer
    val sellerId: String,              // Who owns the item
    val offerAmount: Double,           // Proposed price
    val originalPrice: Double,         // Original listing price for reference
    val message: String = "",          // Optional message to seller
    val status: OfferStatus = OfferStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val respondedAt: Long? = null,     // When seller responded
    val expiresAt: Long = System.currentTimeMillis() + 86400000 * 3  // 3 days default
)

enum class OfferStatus {
    PENDING,        // Waiting for seller response
    ACCEPTED,       // Seller accepted the offer  
    DECLINED,       // Seller rejected the offer
    COUNTERED,      // Seller made a counter offer
    EXPIRED,        // Offer expired without response
    WITHDRAWN       // Buyer withdrew the offer
}


//Counter offer entity for back-and-forth negotiation
@Entity(tableName = "counter_offers")
data class CounterOffer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val originalOfferId: String,      // Links back to original offer
    val counterAmount: Double,        // Seller's counter price
    val message: String = "",         // Seller's message with counter
    val status: OfferStatus = OfferStatus.PENDING,
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000  // 1 day for counter
)

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/