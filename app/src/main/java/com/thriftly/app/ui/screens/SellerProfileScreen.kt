package com.thriftly.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.thriftly.app.R
import com.thriftly.app.data.mock.MockCatalog
import com.thriftly.app.data.mock.ProductItem
import com.thriftly.app.data.entity.Review
import com.thriftly.app.ui.components.StarRating
import com.thriftly.app.ui.components.ReviewCard
import com.thriftly.app.ui.theme.ThriftlyTheme

/**
 * Simple seller profile screen to build trust in marketplace
 * Shows basic seller info + their active listings
 * This helps buyers know who they're buying from
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerProfileScreen(
    nav: NavController,
    sellerId: String = "default_seller",  // In real app, this would come from navigation args
    sellerName: String = "Sarah M.",       // In real app, fetch from database
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    
    // In a real app, you'd fetch seller data from repository
    // For demo purposes, we're using hardcoded data with comments
    val mockSellerRating = 4.7f  // Average rating from buyers
    val mockTotalSales = 47      // Number of completed sales
    val mockJoinDate = "March 2023"  // When seller joined platform
    
    // Filter mock catalog to show only this seller's items
    // In real app, you'd query database by seller ID
    val sellerListings = remember {
        // Mock data - in real app this would be: 
        // listingRepository.getListingsBySeller(sellerId)
        MockCatalog.items.take(3)  // Just show first 3 items as seller's listings
    }
    
    // Mock reviews for demonstration
    val mockReviews = remember {
        listOf(
            Review(
                sellerId = sellerId,
                buyerId = "buyer_1",
                rating = 5,
                comment = "Great seller! Item exactly as described and fast shipping.",
                isVerifiedPurchase = true,
                createdAt = System.currentTimeMillis() - 86400000  // 1 day ago
            ),
            Review(
                sellerId = sellerId,
                buyerId = "buyer_2", 
                rating = 4,
                comment = "Good quality item, would buy again.",
                isVerifiedPurchase = true,
                createdAt = System.currentTimeMillis() - 604800000  // 1 week ago
            ),
            Review(
                sellerId = sellerId,
                buyerId = "buyer_3",
                rating = 5,
                comment = "Excellent communication and quick delivery!",
                isVerifiedPurchase = false,
                createdAt = System.currentTimeMillis() - 1209600000  // 2 weeks ago
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        // Top app bar with back navigation
        TopAppBar(
            title = { Text("Seller Profile") },
            navigationIcon = {
                IconButton(onClick = { nav.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Seller info card at the top
            item {
                SellerInfoCard(
                    sellerName = sellerName,
                    rating = mockSellerRating,
                    totalSales = mockTotalSales,
                    joinDate = mockJoinDate
                )
            }

            // Section header for listings
            item {
                Text(
                    text = "Active Listings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            // Show seller's listings
            if (sellerListings.isEmpty()) {
                item {
                    // Empty state if seller has no active listings
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(24.dp)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No active listings",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                // Display each listing in a simple card
                items(sellerListings) { item ->
                    SellerListingCard(
                        item = item,
                        onItemClick = { 
                            // Navigate to listing details
                            // In real app: nav.navigate("detail/${item.id}")
                        }
                    )
                }
            }
            
            // Reviews section
            item {
                Text(
                    text = "Reviews",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
            
            // Display recent reviews
            items(mockReviews.take(3)) { review ->  // Show only 3 most recent
                ReviewCard(
                    review = review,
                    reviewerName = "Buyer ${review.buyerId.takeLast(1)}"  // Simple mock name
                )
            }
            
            // Show more reviews button if there are more than 3
            if (mockReviews.size > 3) {
                item {
                    OutlinedButton(
                        onClick = { 
                            // Navigate to full reviews screen
                            // nav.navigate("seller_reviews/${sellerId}")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("View All ${mockReviews.size} Reviews")
                    }
                }
            }
        }
    }
}

/**
 * Card showing seller's basic information and reputation
 * Key for building trust in marketplace transactions
 */
@Composable
private fun SellerInfoCard(
    sellerName: String,
    rating: Float,
    totalSales: Int,
    joinDate: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Seller avatar placeholder
            // In real app, this would be user's profile photo
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = sellerName.take(2).uppercase(),  // Show initials as placeholder
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Seller name
            Text(
                text = sellerName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Rating with star icon - now using our StarRating component
            StarRating(
                rating = rating,
                starSize = 18.dp,
                showRating = true
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = "($totalSales sales)",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Member since date - helps establish trust
            Text(
                text = "Member since $joinDate",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Simple card for displaying seller's listing
 * Shows key info buyers need to make decision
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SellerListingCard(
    item: ProductItem,
    onItemClick: () -> Unit
) {
    Card(
        onClick = onItemClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item image
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Show product image if available, otherwise placeholder
                when {
                    item.imageRes != null -> {
                        Image(
                            painter = painterResource(item.imageRes),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    item.imageUri != null -> {
                        AsyncImage(
                            model = item.imageUri,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    else -> {
                        // Simple placeholder for missing images
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "IMG",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Item details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = item.category,  // Show category for context
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Price prominently displayed
            Text(
                text = "R${String.format("%.0f", item.price)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SellerProfileScreenPreview() {
    ThriftlyTheme {
        SellerProfileScreen(
            nav = rememberNavController(),
            sellerId = "preview_seller",
            sellerName = "Sarah M."
        )
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/