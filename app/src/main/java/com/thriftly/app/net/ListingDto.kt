package com.thriftly.app.net

// DTO sent to/received from the API (maps 1:1 with backend fields)
data class ListingDto(
    val id: String,                 // server id
    val title: String,              // item title
    val price: Double,              // price in Rands
    val category: String,           // e.g., Clothes / Shoes
    val size: String,               // e.g., S / M / L
    val condition: String,          // New / Like New / Good / Fair
    val description: String,        // long text
    val imageUrls: List<String>? = null, // list of image URLs from server
    val sellerName: String,         // display name of seller
    val createdAt: Long? = null,    // epoch ms (server-assigned)
    val isDraft: Boolean? = null,   // server may mark drafts
    val isFavorite: Boolean? = null // user-specific favorite flag (if provided)
)

/* 
References 

Android Developers. 2025. Android Architecture Guidelines. [Online]. Available at: https://developer.android.com/topic/architecture [Accessed 17 Nov 2025].
*/
