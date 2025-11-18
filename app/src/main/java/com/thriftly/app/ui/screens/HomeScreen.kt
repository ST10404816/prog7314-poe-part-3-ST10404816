package com.thriftly.app.ui.screens

import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.thriftly.app.ui.theme.ThriftlyTheme
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.platform.testTag
import androidx.navigation.NavHostController
import com.thriftly.app.R
import com.thriftly.app.ConnectivityUtils
import com.thriftly.app.data.mock.MockCatalog
import com.thriftly.app.data.mock.ProductItem
import com.thriftly.app.ui.components.CompactWishlistToggle
import com.thriftly.app.data.entity.Listing
import androidx.compose.foundation.layout.Arrangement
import kotlinx.coroutines.launch
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring as ComposeSpring
import androidx.compose.animation.animateContentSize
import com.thriftly.app.ui.animations.bounceClick
import com.thriftly.app.ui.components.EnhancedAsyncImage
import com.thriftly.app.ui.components.ProductImage
import com.thriftly.app.ui.components.SkeletonGridItem
import com.thriftly.app.ui.animations.bounceClick
import com.thriftly.app.ui.animations.StaggeredListItem
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    nav: NavHostController,
    contentPadding: PaddingValues = PaddingValues()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    // Source of items
    val itemsState = MockCatalog.items
    
    // Connectivity state
    val isConnected by ConnectivityUtils.isConnected.collectAsState()
    var showOfflineMessage by remember { mutableStateOf(false) }
    
    // Search text
    var search by remember { mutableStateOf("") }
    
    // Show offline message when disconnected
    LaunchedEffect(isConnected) {
        if (!isConnected && !showOfflineMessage) {
            showOfflineMessage = true
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.offline_mode_message),
                    actionLabel = context.getString(R.string.retry),
                    duration = SnackbarDuration.Long
                )
            }
        } else if (isConnected && showOfflineMessage) {
            showOfflineMessage = false
        }
    }

    // Filters list as user types
    val filtered by remember(search, itemsState.size) {
        mutableStateOf(
            itemsState.filter { it.title.contains(search, ignoreCase = true) }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.semantics { heading() }
                    ) {
                        Text(
                            text = stringResource(R.string.tab_home),
                            style = MaterialTheme.typography.headlineSmall
                        )
                        if (!isConnected) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = stringResource(R.string.offline_indicator),
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                actions = {
                    if (!isConnected) {
                        IconButton(
                            onClick = {
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = context.getString(R.string.offline_mode_message)
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh"
                            )
                        }
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)          // respect Scaffold insets
                .padding(contentPadding) // respect parent padding (e.g., from MainScreen)
        ) {
            // Quick access to the Offline test screen
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.End) {
                Button(onClick = { nav.navigate("offline") }) { Text(stringResource(R.string.offline)) }
            }
            // ── Header: logo and app name with better accessibility ─
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .semantics { heading() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.logo),
                        contentDescription = stringResource(R.string.logo_desc),
                        modifier = Modifier.size(44.dp),
                        contentScale = ContentScale.Fit
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.app_name_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
            
            // ── Search box with enhanced accessibility ─────────────────────
            OutlinedTextField(
                value = search,
                onValueChange = { search = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("search_field")
                    .semantics {
                        contentDescription = "Search for clothing items"
                    },
                placeholder = { 
                    Text(
                        text = stringResource(R.string.search_placeholder),
                        style = MaterialTheme.typography.bodyLarge
                    ) 
                },
                leadingIcon = { 
                    Icon(
                        Icons.Default.Search, 
                        contentDescription = "Search icon",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    ) 
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            Spacer(Modifier.height(12.dp))

            // ── Grid or empty state ──────────────────────────────────────────
            if (filtered.isEmpty() && search.isEmpty()) {
                // Empty state: shows friendly message when no search is active
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (itemsState.isEmpty()) "Welcome to Thriftly!" else "No matching items",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (itemsState.isEmpty()) 
                                "Create your first listing to see items here" 
                            else 
                                "Try a different search term",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (itemsState.isEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = { nav.navigate("create") }
                            ) {
                                Text("Create First Listing")
                            }
                        }
                    }
                }
            } else if (filtered.isEmpty() && search.isNotEmpty()) {
                // Search returned no results
                Card(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp)
                        .semantics { 
                            contentDescription = "No search results found for: $search"
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No items found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = "Try searching for something else",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                // Product grid with enhanced layout
                ProductGrid(
                    items = filtered,
                    modifier = Modifier
                        .fillMaxSize()
                        .semantics {
                            contentDescription = "Grid of ${filtered.size} clothing items"
                        },
                    onClick = { product -> nav.navigate("detail/${product.id}") }
                )
            }
        }
    }
}

@Composable
private fun ProductGrid(
    items: List<ProductItem>,
    modifier: Modifier = Modifier,
    onClick: (ProductItem) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 160.dp), // Responsive grid
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier
            .testTag("product_grid")
            .semantics {
                contentDescription = "Grid showing ${items.size} items"
            }
    ) {
        items(items.size) { index ->
            val product = items[index]
            StaggeredListItem(
                index = index,
                visible = true,
                delayMillis = 50
            ) {
                ProductCard(product = product) { onClick(product) }
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: ProductItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .bounceClick(onClick = onClick)
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = ComposeSpring.DampingRatioMediumBouncy,
                    stiffness = ComposeSpring.StiffnessLow
                )
            )
            .semantics {
                contentDescription = "${product.title}, R${String.format("%.0f", product.price)}, ${product.condition} condition"
            }
            .testTag("product_card_${product.id}"),
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 6.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column {
            // Image area
            // Enhanced image area with proper Coil loading
            Box(
                modifier = Modifier
                    .height(140.dp)
                    .fillMaxWidth()
                    .clip(MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                // Use enhanced product image component with shimmer loading
                ProductImage(
                    imageUrl = product.imageUri,
                    imageRes = product.imageRes,
                    title = product.title,
                    modifier = Modifier.fillMaxSize()
                )
                
                // Wishlist toggle button with semi-transparent background for visibility
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f),
                            shape = CircleShape
                        )
                ) {
                    CompactWishlistToggle(
                        listing = product.toListing(),
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Product info
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = product.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "R${String.format("%.0f", product.price)}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = when (product.condition) {
                            "New" -> MaterialTheme.colorScheme.primaryContainer
                            "Like New" -> MaterialTheme.colorScheme.secondaryContainer
                            "Good" -> MaterialTheme.colorScheme.tertiaryContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        },
                        modifier = Modifier.padding(horizontal = 0.dp)
                    ) {
                        Text(
                            text = product.condition,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            color = when (product.condition) {
                                "New" -> MaterialTheme.colorScheme.onPrimaryContainer
                                "Like New" -> MaterialTheme.colorScheme.onSecondaryContainer
                                "Good" -> MaterialTheme.colorScheme.onTertiaryContainer
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
                
                Spacer(Modifier.height(2.dp))
                Text(
                    text = product.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Extension function to convert ProductItem to Listing for wishlist functionality
 */
private fun ProductItem.toListing(): Listing {
    return Listing(
        id = this.id,
        title = this.title,
        price = this.price,
        category = this.category,
        condition = this.condition,
        description = "", // ProductItem doesn't have description
        sellerName = "Seller", // Default seller name
        imageUris = listOfNotNull(this.imageUri),
        size = "Unknown", // ProductItem doesn't have size
        isDraft = false,
        isFavorite = false
    )
}

@Preview(
    name = "Home – Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    device = "id:pixel_7"
)
@Composable
fun HomeScreenPreviewLight() {
    ThriftlyTheme {
        val nav = rememberNavController()
        HomeScreen(nav = nav)
    }
}

@Preview(
    name = "Home – Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    device = "id:pixel_7"
)
@Composable
fun HomeScreenPreviewDark() {
    ThriftlyTheme {
        val nav = rememberNavController()
        HomeScreen(nav = nav)
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
