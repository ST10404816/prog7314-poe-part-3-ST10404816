package com.thriftly.app.nav

// Central place for navigation route names
object Routes {
    const val Welcome = "welcome"                 // entry screen
    const val Login = "login"                     // login screen
    const val Signup = "signup"                   // sign-up screen
    const val Home = "home"                       // main/home tab
    const val Detail = "detail/{listingId}"       // detail screen with an argument
    
    // Routes for enhanced functionality
    const val SellerProfile = "seller_profile/{sellerId}"  // seller profile screen
    const val Chat = "chat/{threadId}"            // individual chat conversation
    const val MessagesList = "messages"           // list of all conversations
    const val Reviews = "reviews/{sellerId}"      // all reviews for a seller
    const val MakeOffer = "make_offer/{listingId}"  // make offer on item
    
}

/* 
References 

Kotlin Documentation. 2025. Coroutines and Flow. [Online]. Available at: https://kotlinlang.org/docs/coroutines-overview.html [Accessed 16 Nov 2025].
*/