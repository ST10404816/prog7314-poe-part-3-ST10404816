// MockCatalog.kt
package com.thriftly.app.data.mock

import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateListOf

// Simple in-memory product list for previews/demos
object MockCatalog {
    // Compose-observable list so UI updates when items change
    val items = mutableStateListOf<ProductItem>()

    // Add one product (either pass a drawable res or a URI)
    fun add(
        title: String,
        price: Double,
        category: String,
        condition: String,
        @DrawableRes imageRes: Int? = null,
        imageUri: String? = null
    ) {
        items.add(
            ProductItem(
                title = title,
                price = price,
                category = category,
                condition = condition,
                imageRes = imageRes,
                imageUri = imageUri
            )
        )
    }

    // Remove all demo items
    fun clear() = items.clear()
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/