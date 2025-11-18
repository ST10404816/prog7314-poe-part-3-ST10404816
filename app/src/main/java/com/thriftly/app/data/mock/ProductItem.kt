
// ProductItem.kt
package com.thriftly.app.data.mock

import androidx.annotation.DrawableRes
import java.util.UUID

// Mock data model for product items in the marketplace
data class ProductItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val price: Double,
    val category: String,
    val condition: String,
    @DrawableRes val imageRes: Int? = null,
    val imageUri: String? = null
)
