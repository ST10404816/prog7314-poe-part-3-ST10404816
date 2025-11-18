package com.thriftly.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.thriftly.app.data.entity.Listing
import com.thriftly.app.viewmodel.WishlistViewModel

/**
 * Floating heart button for wishlist toggle
 */
@Composable
fun WishlistToggleButton(
    listing: Listing,
    viewModel: WishlistViewModel = WishlistViewModel(LocalContext.current),
    modifier: Modifier = Modifier
) {
    val isInWishlist by viewModel.isInWishlist(listing.id).collectAsState(initial = false)
    val uiState by viewModel.uiState.collectAsState()
    
    FloatingActionButton(
        onClick = { viewModel.toggleWishlist(listing) },
        modifier = modifier.size(48.dp),
        containerColor = if (isInWishlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        contentColor = if (isInWishlist) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isInWishlist) "Remove from wishlist" else "Add to wishlist",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Compact heart icon button for use in cards
 */
@Composable
fun CompactWishlistToggle(
    listing: Listing,
    viewModel: WishlistViewModel = WishlistViewModel(LocalContext.current),
    modifier: Modifier = Modifier
) {
    val isInWishlist by viewModel.isInWishlist(listing.id).collectAsState(initial = false)
    val uiState by viewModel.uiState.collectAsState()
    
    IconButton(
        onClick = { viewModel.toggleWishlist(listing) },
        modifier = modifier.size(36.dp)
    ) {
        if (uiState.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
        } else {
            Icon(
                imageVector = if (isInWishlist) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                contentDescription = if (isInWishlist) "Remove from wishlist" else "Add to wishlist",
                tint = if (isInWishlist) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Wishlist counter for navigation
 */
@Composable
fun WishlistCounter(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val viewModel = remember { WishlistViewModel(context) }
    val count by viewModel.wishlistCount.collectAsState(initial = 0)
    
    if (count > 0) {
        Badge(
            modifier = modifier,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

/* References
Google. (n.d.). Compose UI. https://developer.android.com/jetpack/compose
*/