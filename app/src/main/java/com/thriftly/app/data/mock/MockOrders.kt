// MockOrders.kt
package com.thriftly.app.data.mock

import androidx.annotation.DrawableRes
import androidx.compose.runtime.mutableStateListOf
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Simple finite set of order states for UI logic
enum class OrderStatus { Processing, Shipped, Delivered, Completed }

// Format today's date as "dd MMM yyyy" (API 24 safe)
private fun currentDateString(): String {
    val fmt = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return fmt.format(Date())
}

// Lightweight order model for demos/previews
data class Order(
    val id: String = UUID.randomUUID().toString(), // unique id for keys
    val title: String,                              // product name
    val price: Double,                              // price in Rands
    val status: OrderStatus = OrderStatus.Processing,
    val orderNo: String = "#${(10000..99999).random()}", // mock order number
    val date: String = currentDateString(),        // created date string
    val seller: String = "User",                   // seller display name
    val address: String? = null,                   // optional shipping address
    @DrawableRes val imageRes: Int? = null,        // optional local drawable
    val imageUri: String? = null                   // optional URI string
)

// In-memory list of orders (Compose observes changes)
object MockOrders {
    val orders = mutableStateListOf<Order>()

    // Add a new mock order (either pass drawable or URI for image)
    fun add(
        title: String,
        price: Double,
        status: OrderStatus = OrderStatus.Processing,
        seller: String = "User",
        address: String? = null,
        @DrawableRes imageRes: Int? = null,
        imageUri: String? = null
    ) {
        orders.add(
            Order(
                title = title,
                price = price,
                status = status,
                seller = seller,
                address = address,
                imageRes = imageRes,
                imageUri = imageUri
            )
        )
    }
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/