package com.thriftly.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.thriftly.app.data.entity.Review
import com.thriftly.app.ui.theme.ThriftlyTheme

/**
 * Simple star rating display component
 * Shows average rating with star icons
 * Used throughout app to display seller ratings
 */
@Composable
fun StarRating(
    rating: Float,
    maxStars: Int = 5,
    starSize: androidx.compose.ui.unit.Dp = 16.dp,
    showRating: Boolean = true
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        // Display stars
        repeat(maxStars) { index ->
            val isFilled = index < rating.toInt()
            val isHalfFilled = index == rating.toInt() && rating % 1 != 0f
            
            Icon(
                imageVector = if (isFilled || isHalfFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = null,
                tint = if (isFilled || isHalfFilled) {
                    Color(0xFFFFB000)  // Gold color for stars
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier.size(starSize)
            )
        }
        
        // Show numerical rating if requested
        if (showRating) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = String.format("%.1f", rating),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Interactive star rating component for leaving reviews
 * Allows users to tap stars to set rating
 */
@Composable
fun InteractiveStarRating(
    rating: Int,
    onRatingChanged: (Int) -> Unit,
    maxStars: Int = 5,
    starSize: androidx.compose.ui.unit.Dp = 24.dp
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(maxStars) { index ->
            val starNumber = index + 1
            val isFilled = starNumber <= rating
            
            Icon(
                imageVector = if (isFilled) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = "Rate $starNumber star${if (starNumber > 1) "s" else ""}",
                tint = if (isFilled) {
                    Color(0xFFFFB000)  // Gold for selected stars
                } else {
                    MaterialTheme.colorScheme.outline
                },
                modifier = Modifier
                    .size(starSize)
                    .clickable { onRatingChanged(starNumber) }
                    .padding(2.dp)  // Make clickable area larger
            )
        }
    }
}

/**
 * Compact seller rating display
 * Shows rating summary in a small format
 * Perfect for listing cards and search results
 */
@Composable
fun SellerRatingBadge(
    averageRating: Float,
    reviewCount: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Filled.Star,
                contentDescription = null,
                tint = Color(0xFFFFB000),
                modifier = Modifier.size(14.dp)
            )
            
            Text(
                text = String.format("%.1f", averageRating),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium
            )
            
            Text(
                text = "($reviewCount)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Individual review card component
 * Shows reviewer info, rating, and comment
 */
@Composable
fun ReviewCard(
    review: Review,
    reviewerName: String,  // In real app, fetch from user table
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Reviewer info row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reviewer avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = reviewerName.take(1).uppercase(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reviewerName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    Text(
                        text = formatReviewDate(review.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Show verification badge if verified purchase
                if (review.isVerifiedPurchase) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "Verified",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            
            // Rating display
            StarRating(
                rating = review.rating.toFloat(),
                showRating = false
            )
            
            // Review comment if provided
            if (review.comment.isNotBlank()) {
                Text(
                    text = review.comment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * Reviews list component
 * Shows all reviews for a seller with summary
 */
@Composable
fun ReviewsList(
    reviews: List<Review>,
    averageRating: Float,
    totalReviews: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = String.format("%.1f", averageRating),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                
                StarRating(
                    rating = averageRating,
                    showRating = false,
                    starSize = 20.dp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "$totalReviews review${if (totalReviews != 1) "s" else ""}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Individual reviews
        if (reviews.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No reviews yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(reviews) { review ->
                    ReviewCard(
                        review = review,
                        reviewerName = "User ${review.buyerId.take(4)}"  // Mock name
                    )
                }
            }
        }
    }
}

/**
 * Simple date formatting for reviews
 * Shows relative time for recent reviews
 */
private fun formatReviewDate(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 86400000 -> "Today"                    // Less than 24 hours
        diff < 604800000 -> "${diff / 86400000} days ago"  // Less than 1 week
        diff < 2592000000 -> "${diff / 604800000} weeks ago" // Less than 1 month
        else -> "${diff / 2592000000} months ago"      // Months ago
    }
}

@Preview(showBackground = true)
@Composable
private fun StarRatingPreview() {
    ThriftlyTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StarRating(rating = 4.3f)
            SellerRatingBadge(averageRating = 4.7f, reviewCount = 23)
            InteractiveStarRating(rating = 3, onRatingChanged = {})
        }
    }
}