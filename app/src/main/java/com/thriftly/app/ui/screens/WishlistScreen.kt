package com.thriftly.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.thriftly.app.R
import com.thriftly.app.data.entity.WishlistItem
import com.thriftly.app.data.entity.WishlistNotificationSettings
import com.thriftly.app.ui.components.OfflineIndicator
import com.thriftly.app.ui.theme.ThriftlyTheme
import com.thriftly.app.viewmodel.WishlistViewModel
import com.thriftly.app.viewmodel.WishlistSortOption
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*
import com.thriftly.app.ui.components.ProductImage
import com.thriftly.app.ui.components.SkeletonListItem
import com.thriftly.app.ui.animations.bounceClick
import com.thriftly.app.ui.animations.StaggeredListItem
import com.thriftly.app.ui.animations.ExpandCollapse
import com.thriftly.app.ui.components.WishlistSettingsSheet
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType

/**
 * Main Wishlist Screen showing saved items with price monitoring
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(
    nav: NavHostController,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val viewModel = remember { WishlistViewModel(context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Collect state
    val uiState by viewModel.uiState.collectAsState()
    val wishlistItems by viewModel.wishlistItems.collectAsState(initial = emptyList())
    val wishlistCount by viewModel.wishlistCount.collectAsState(initial = 0)
    val notificationSettings by viewModel.notificationSettings.collectAsState(initial = WishlistNotificationSettings())
    val isConnected by viewModel.isConnected.collectAsState()
    
    // Handle UI messages
    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessage()
        }
    }
    
    // Filter and sort items
    val processedItems = remember(wishlistItems, uiState.filterCategory, uiState.sortOption) {
        var filtered = wishlistItems
        
        // Apply category filter
        uiState.filterCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }
        
        // Apply sorting
        when (uiState.sortOption) {
            WishlistSortOption.DATE_ADDED -> filtered.sortedByDescending { it.addedAt }
            WishlistSortOption.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.currentPrice }
            WishlistSortOption.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.currentPrice }
            WishlistSortOption.TITLE_A_TO_Z -> filtered.sortedBy { it.listingTitle }
            WishlistSortOption.CATEGORY -> filtered.sortedBy { it.category }
            WishlistSortOption.BIGGEST_PRICE_DROP -> filtered.sortedByDescending { 
                it.originalPrice - it.currentPrice 
            }
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            WishlistTopBar(
                title = "Wishlist ($wishlistCount)",
                onRefresh = { viewModel.refreshPrices() },
                onSettings = { viewModel.toggleSettings() },
                isRefreshing = uiState.isRefreshing,
                isConnected = isConnected
            )
        },
        floatingActionButton = {
            if (wishlistItems.isNotEmpty()) {
                FloatingActionButton(
                    onClick = { viewModel.refreshPrices() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh prices"
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(contentPadding)
        ) {
            Column {
                // Offline indicator
                OfflineIndicator(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    onRetry = { viewModel.refreshPrices() }
                )
                
                // Filter and sort controls
                if (wishlistItems.isNotEmpty()) {
                    WishlistControls(
                        currentSort = uiState.sortOption,
                        currentFilter = uiState.filterCategory,
                        availableCategories = wishlistItems.map { it.category }.distinct(),
                        onSortChanged = { viewModel.setSortOption(it) },
                        onFilterChanged = { viewModel.setFilterCategory(it) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                // Content
                if (processedItems.isEmpty()) {
                    EmptyWishlistContent(
                        onBrowseListings = { nav.navigate("home") },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = processedItems,
                            key = { it.id }
                        ) { item ->
                            WishlistItemCard(
                                item = item,
                                onRemove = { viewModel.removeFromWishlist(item.listingId) },
                                onSetAlert = { targetPrice -> 
                                    viewModel.setTargetPrice(item.id, targetPrice) 
                                },
                                onClick = { 
                                    nav.navigate("listing/${item.listingId}")
                                }
                            )
                        }
                        
                        // Add bottom padding for FAB
                        item {
                            Spacer(modifier = Modifier.height(80.dp))
                        }
                    }
                }
            }
            
            // Loading overlay
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
    
    // Settings bottom sheet
    if (uiState.showSettings) {
        WishlistSettingsSheet(
            settings = notificationSettings,
            onDismiss = { viewModel.toggleSettings() },
            onSettingsChanged = { viewModel.updateNotificationSettings(it) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WishlistTopBar(
    title: String,
    onRefresh: () -> Unit,
    onSettings: () -> Unit,
    isRefreshing: Boolean,
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { 
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold
            ) 
        },
        actions = {
            if (!isConnected) {
                Icon(
                    imageVector = Icons.Default.CloudOff,
                    contentDescription = "Offline",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            
            if (isRefreshing) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(24.dp)
                        .padding(end = 8.dp),
                    strokeWidth = 2.dp
                )
            } else {
                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh prices"
                    )
                }
            }
            
            IconButton(onClick = onSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )
            }
        },
        modifier = modifier
    )
}

@Composable
private fun WishlistControls(
    currentSort: WishlistSortOption,
    currentFilter: String?,
    availableCategories: List<String>,
    onSortChanged: (WishlistSortOption) -> Unit,
    onFilterChanged: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterMenu by remember { mutableStateOf(false) }
    
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sort button
        OutlinedButton(
            onClick = { showSortMenu = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = Icons.Default.Sort,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = currentSort.displayName,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Filter button
        OutlinedButton(
            onClick = { showFilterMenu = true },
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = if (currentFilter != null) Icons.Default.FilterList else Icons.Default.FilterListOff,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = currentFilter ?: "All Categories",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        
        // Clear filter button
        if (currentFilter != null) {
            IconButton(
                onClick = { onFilterChanged(null) }
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear filter",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
    
    // Sort dropdown menu
    DropdownMenu(
        expanded = showSortMenu,
        onDismissRequest = { showSortMenu = false }
    ) {
        WishlistSortOption.values().forEach { option ->
            DropdownMenuItem(
                text = { Text(option.displayName) },
                onClick = {
                    onSortChanged(option)
                    showSortMenu = false
                },
                leadingIcon = if (option == currentSort) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
    
    // Filter dropdown menu
    DropdownMenu(
        expanded = showFilterMenu,
        onDismissRequest = { showFilterMenu = false }
    ) {
        DropdownMenuItem(
            text = { Text("All Categories") },
            onClick = {
                onFilterChanged(null)
                showFilterMenu = false
            },
            leadingIcon = if (currentFilter == null) {
                { Icon(Icons.Default.Check, contentDescription = null) }
            } else null
        )
        
        availableCategories.forEach { category ->
            DropdownMenuItem(
                text = { Text(category) },
                onClick = {
                    onFilterChanged(category)
                    showFilterMenu = false
                },
                leadingIcon = if (category == currentFilter) {
                    { Icon(Icons.Default.Check, contentDescription = null) }
                } else null
            )
        }
    }
}

@Composable
private fun EmptyWishlistContent(
    onBrowseListings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            modifier = Modifier.size(96.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        
        Spacer(Modifier.height(16.dp))
        
        Text(
            text = "Your wishlist is empty",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(Modifier.height(8.dp))
        
        Text(
            text = "Save items you love and get notified when prices drop!",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        
        Spacer(Modifier.height(24.dp))
        
        Button(
            onClick = onBrowseListings,
            modifier = Modifier.fillMaxWidth(0.6f)
        ) {
            Text("Browse Listings")
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun WishlistScreenPreview() {
    ThriftlyTheme {
        WishlistScreen(
            nav = rememberNavController()
        )
    }
}

/**
 * Enhanced wishlist item card with modern design and interactions
 * 
 * Features:
 * - Bounce click animation for better user feedback
 * - Enhanced product image with shimmer loading
 * - Price tracking indicators and alerts
 * - Smooth content size animations
 * - Accessibility optimizations
 * 
 * @param item The wishlist item to display
 * @param onRemove Callback when item is removed from wishlist
 * @param onSetAlert Callback when price alert is set
 * @param onClick Callback when item is clicked
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WishlistItemCard(
    item: WishlistItem,
    onRemove: () -> Unit,
    onSetAlert: (Double) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showPriceAlert by remember { mutableStateOf(false) }
    val currencyFormat = NumberFormat.getCurrencyInstance(Locale("en", "ZA"))
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Enhanced product image with shimmer loading
                ProductImage(
                    imageUrl = item.imageUrl,
                    imageRes = null,
                    title = item.listingTitle,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    aspectRatio = 1f
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Item details
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.listingTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Price with formatting
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = currencyFormat.format(item.currentPrice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        // Price drop indicator
                        if (item.originalPrice > item.currentPrice) {
                            val discount = ((item.originalPrice - item.currentPrice) / item.originalPrice * 100).toInt()
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "-$discount%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Category and condition
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Text(
                            text = " â€¢ ",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = when (item.condition.lowercase()) {
                                "new" -> MaterialTheme.colorScheme.primaryContainer
                                "like new" -> MaterialTheme.colorScheme.secondaryContainer
                                "good" -> MaterialTheme.colorScheme.tertiaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ) {
                            Text(
                                text = item.condition,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = when (item.condition.lowercase()) {
                                    "new" -> MaterialTheme.colorScheme.onPrimaryContainer
                                    "like new" -> MaterialTheme.colorScheme.onSecondaryContainer
                                    "good" -> MaterialTheme.colorScheme.onTertiaryContainer
                                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                    
                    // Added date
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Added ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(item.addedAt))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Actions column
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Remove button
                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove from wishlist",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    // Price alert button
                    IconButton(
                        onClick = { showPriceAlert = !showPriceAlert },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (item.targetPrice != null) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                            contentDescription = if (item.targetPrice != null) "Price alert active" else "Set price alert",
                            tint = if (item.targetPrice != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Price alert section (expandable)
            ExpandCollapse(expanded = showPriceAlert) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Divider(modifier = Modifier.padding(bottom = 16.dp))
                    
                    Text(
                        text = "Set Price Alert",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    var targetPriceText by remember { 
                        mutableStateOf(item.targetPrice?.toString() ?: "")
                    }
                    
                    OutlinedTextField(
                        value = targetPriceText,
                        onValueChange = { targetPriceText = it },
                        label = { Text("Target Price (R)") },
                        leadingIcon = { Text("R") },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPriceAlert = false }) {
                            Text("Cancel")
                        }
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        Button(
                            onClick = {
                                targetPriceText.toDoubleOrNull()?.let { price ->
                                    onSetAlert(price)
                                    showPriceAlert = false
                                }
                            },
                            enabled = targetPriceText.toDoubleOrNull() != null
                        ) {
                            Text("Set Alert")
                        }
                    }
                }
            }
        }
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
