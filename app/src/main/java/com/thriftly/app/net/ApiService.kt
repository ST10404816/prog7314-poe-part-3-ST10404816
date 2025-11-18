// app/src/main/java/com/thriftly/app/net/ApiService.kt
package com.thriftly.app.net

import retrofit2.http.*

// Retrofit interface for REST endpoints (suspend = call on coroutine)
interface ApiService {

    // GET /listings → fetch all listings
    @GET("listings")
    suspend fun getListings(): List<ListingDto>

    // GET /listings/{id} → fetch one listing by id
    @GET("listings/{id}")
    suspend fun getListing(@Path("id") id: Long): ListingDto

    // POST /listings → create a new listing (body is JSON)
    @POST("listings")
    suspend fun createListing(@Body body: ListingDto): ListingDto

    // PUT /listings/{id} → update existing listing
    @PUT("listings/{id}")
    suspend fun updateListing(
        @Path("id") id: Long,
        @Body body: ListingDto
    ): ListingDto

    // DELETE /listings/{id} → remove a listing (no body returned)
    @DELETE("listings/{id}")
    suspend fun deleteListing(@Path("id") id: Long)
}

/* 
References 

Android Developers. 2025. Android Architecture Guidelines. [Online]. Available at: https://developer.android.com/topic/architecture [Accessed 17 Nov 2025].
*/
