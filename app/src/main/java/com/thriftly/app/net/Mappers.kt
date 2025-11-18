package com.thriftly.app.net

import com.thriftly.app.data.entity.Listing

// Map API DTO -> domain entity (used by UI / database)
fun ListingDto.toDomain(): Listing = Listing(
    id = id,
    title = title,
    price = price,
    category = category,
    size = size,
    condition = condition,
    description = description,
    imageUris = imageUrls ?: emptyList(),      // API "imageUrls" -> entity "imageUris" (fallback to empty)
    sellerName = sellerName,
    createdAt = createdAt ?: System.currentTimeMillis(), // default to "now" if server didn’t send one
    isDraft = isDraft ?: false,                // safe defaults for nullable flags
    isFavorite = isFavorite ?: false
)

// Map domain entity -> API DTO (send to server)
fun Listing.toDto(): ListingDto = ListingDto(
    id = id,
    title = title,
    price = price,
    category = category,
    size = size,
    condition = condition,
    description = description,
    imageUrls = imageUris,
    sellerName = sellerName,
    createdAt = createdAt,
    isDraft = isDraft,
    isFavorite = isFavorite
)

/* 
References 

Android Developers. 2025. Android Architecture Guidelines. [Online]. Available at: https://developer.android.com/topic/architecture [Accessed 17 Nov 2025].
*/
