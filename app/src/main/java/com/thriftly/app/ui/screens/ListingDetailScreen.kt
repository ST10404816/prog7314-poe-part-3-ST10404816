package com.thriftly.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.thriftly.app.data.entity.Listing
import com.thriftly.app.data.repo.ThriftlyRepository
import com.thriftly.app.ui.components.MakeOfferButton
import com.thriftly.app.data.mock.MockCatalog
import com.thriftly.app.data.mock.ProductItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ListingDetailScreen(
    listingId: String,                 // id passed from navigation
    nav: NavHostController,            // for back navigation
    repo: ThriftlyRepository? = null   // allow DI in previews/tests
) {
    // Create/keep a repository tied to current context
    val ctx = LocalContext.current
    val repository = remember(ctx, repo) { repo ?: ThriftlyRepository(ctx) }

    // Observe all listings; Compose will recompose when data changes
    val listings: List<Listing> by repository.listingsFlow.collectAsState(initial = emptyList())
    
    // Check MockCatalog for ProductItems
    val mockProduct: ProductItem? = remember(MockCatalog.items.size, listingId) {
        MockCatalog.items.firstOrNull { it.id == listingId }
    }

    // Find the item to display for this screen (null if not found)
    val item: Listing? = remember(listings, listingId) {
        listings.firstOrNull { it.id == listingId }
    }

    Scaffold(
        // Simple top app bar with a back button and title
        topBar = {
            TopAppBar(
                title = { Text(text = item?.title ?: stringResource(com.thriftly.app.R.string.listing)) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(com.thriftly.app.R.string.back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        // Basic content: shows error if not found, else detailed listing view
        if (item == null && mockProduct == null) {
            Text(
                text = stringResource(com.thriftly.app.R.string.listing_not_found),
                modifier = Modifier.padding(innerPadding)
            )
        } else if (mockProduct != null) {
            // Show ProductItem details from MockCatalog
            ProductDetailContent(
                product = mockProduct,
                repository = repository,
                nav = nav,
                modifier = Modifier.padding(innerPadding)
            )
        } else if (item != null) {
            // Enhanced listing details with seller information
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main listing info
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "R ${String.format("%.0f", item.price)}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "${item.category} • ${item.condition}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        if (item.size.isNotBlank()) {
                            Text(
                                text = "Size: ${item.size}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (item.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.description,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                // Seller info section - key for marketplace trust
                SellerInfoSection(
                    sellerName = item.sellerName,
                    onViewProfile = { 
                        // Navigate to seller profile
                        // nav.navigate("seller_profile/${sellerId}")
                    },
                    onMessageSeller = {
                        // Navigate to messaging
                        // nav.navigate("chat/${sellerId}")
                    }
                )
                
                // Action buttons for marketplace interaction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Make offer button - enables price negotiation
                    MakeOfferButton(
                        originalPrice = item.price,
                        onOfferSubmitted = { amount, message ->
                            // In real app: offerRepository.createOffer(...)
                            // For now, just show a success message
                        },
                        modifier = Modifier.weight(1f)
                    )
                    
                    // Purchase button - adds item to orders
                    Button(
                        onClick = {
                            // Create order and navigate to orders screen
                            repository.createOrder(
                                listingId = item.id,
                                title = item.title,
                                price = item.price,
                                seller = item.sellerName
                            )
                            nav.navigate("orders") {
                                popUpTo("home") { inclusive = false }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Purchase")
                    }
                }
            }
        }
    }
}

/**
 * Simple seller information section for building marketplace trust
 * Shows who is selling the item with quick action buttons
 */
@Composable
private fun SellerInfoSection(
    sellerName: String,
    onViewProfile: () -> Unit,
    onMessageSeller: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Sold by",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Simple seller avatar placeholder
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = sellerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    // In a real app, you'd show seller rating here
                    Text(
                        text = "Trusted seller",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // Action buttons for marketplace interaction
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // View seller profile button
                    OutlinedButton(
                        onClick = onViewProfile,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "View Profile",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    
                    // Message seller button - primary action
                    Button(
                        onClick = onMessageSeller,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Message",
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}

/**
 * Product detail content for MockCatalog items
 */
@Composable
private fun ProductDetailContent(
    product: ProductItem,
    repository: ThriftlyRepository,
    nav: NavHostController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main product info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "R ${String.format("%.0f", product.price)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "${product.category} • ${product.condition}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Purchase button
        Button(
            onClick = {
                // Create order and navigate to orders screen
                repository.createOrder(
                    listingId = product.id,
                    title = product.title,
                    price = product.price,
                    seller = "Store",
                    imageRes = product.imageRes,
                    imageUri = product.imageUri
                )
                nav.navigate("orders") {
                    popUpTo("home") { inclusive = false }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Purchase")
        }
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
