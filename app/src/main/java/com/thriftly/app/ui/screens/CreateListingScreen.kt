package com.thriftly.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.thriftly.app.R
import com.thriftly.app.data.entity.Listing
import com.thriftly.app.data.repo.ThriftlyRepository
import com.thriftly.app.data.mock.MockCatalog
import com.thriftly.app.ui.theme.ThriftlyTheme
import kotlinx.coroutines.launch
import org.json.JSONObject
import com.thriftly.app.ConnectivityUtils
import com.thriftly.app.OfflineRepository
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.imePadding
import com.thriftly.app.ui.components.OfflineIndicator
import androidx.compose.material3.SnackbarHostState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateListingScreen(
    nav: NavHostController,
    contentPadding: PaddingValues = PaddingValues()
) {
    // Repository
    val repo = remember { ThriftlyRepository(nav.context) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = nav.context
    val isConnected by ConnectivityUtils.isConnected.collectAsState()

    // --- Form state ---
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Clothes") }
    var size by remember { mutableStateOf("S") }
    var condition by remember { mutableStateOf("New") }
    var description by remember { mutableStateOf("") }
    var images by remember { mutableStateOf(listOf<Uri>()) }

    // Dropdown expand states
    var catOpen by remember { mutableStateOf(false) }
    var sizeOpen by remember { mutableStateOf(false) }
    var condOpen by remember { mutableStateOf(false) }

    // Dropdown options
    val catOptions = listOf("Clothes", "Shoes", "Accessories", "Bags", "Other")
    val sizeOptions = listOf("XS", "S", "M", "L", "XL")
    val condOptions = listOf("New", "Like New", "Good", "Fair")

    // System image picker
    val picker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia()
    ) { uris -> if (uris.isNotEmpty()) images = uris }

    // UI helpers
    val scroll = rememberScrollState()
    val shape = RoundedCornerShape(14.dp)
    val borderColor = MaterialTheme.colorScheme.outlineVariant

    // Very simple validation
    val isValid = title.isNotBlank() && (price.toDoubleOrNull() ?: 0.0) > 0.0

    Scaffold(
        // Combine system bars
        contentWindowInsets = WindowInsets.systemBars.union(WindowInsets.ime),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)          // from Scaffold
                .padding(contentPadding) // from parent screen (e.g., Scaffold with bottom bar)
                .verticalScroll(scroll)
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // ===== Header =====
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo block
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                            painter = painterResource(id = R.drawable.logo),
                            contentDescription = stringResource(R.string.logo_desc),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        contentScale = ContentScale.Fit
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.create_listing_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // ===== Offline Indicator =====
            OfflineIndicator(
                modifier = Modifier.padding(bottom = 16.dp),
                onRetry = {
                    // Optionally retry connectivity
                }
            )

            // ===== Images (two small thumbs + one large main) =====
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Side thumbs (optional)
                if (images.size > 1) {
                    Column(
                        modifier = Modifier.width(84.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ThumbBox(
                            uri = images.getOrNull(1),
                            shape = shape,
                            borderColor = borderColor
                        ) {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        ThumbBox(
                            uri = images.getOrNull(2),
                            shape = shape,
                            borderColor = borderColor
                        ) {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                }

                // Main image slot
                Box(
                    modifier = Modifier
                        .height(120.dp)
                        .weight(1f)
                        .clip(shape)
                        .border(1.dp, borderColor, shape)
                        .clickable {
                            picker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val main = images.firstOrNull()
                    if (main != null) {
                        // Placeholder preview; replace with Coil if desired
                        AsyncImageCompat(main)
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.add_photo))
                            Spacer(Modifier.height(4.dp))
                            Text(
                                stringResource(R.string.tap_to_add_photo),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            // ===== Title =====
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(stringResource(R.string.label_title)) },
                shape = shape,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(10.dp))

            // ===== Description =====
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.label_description)) },
                shape = shape,
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Spacer(Modifier.height(10.dp))

            // ===== Dropdowns =====
            ExposedDropdown(
                label = stringResource(R.string.label_category),
                value = category,
                expanded = catOpen,
                onExpandedChange = { catOpen = it },
                options = catOptions,
                onSelect = { category = it; catOpen = false },
                shape = shape
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdown(
                label = stringResource(R.string.label_size),
                value = size,
                expanded = sizeOpen,
                onExpandedChange = { sizeOpen = it },
                options = sizeOptions,
                onSelect = { size = it; sizeOpen = false },
                shape = shape
            )
            Spacer(Modifier.height(8.dp))

            ExposedDropdown(
                label = stringResource(R.string.label_condition),
                value = condition,
                expanded = condOpen,
                onExpandedChange = { condOpen = it },
                options = condOptions,
                onSelect = { condition = it; condOpen = false },
                shape = shape
            )

            Spacer(Modifier.height(10.dp))

            // ===== Price =====
            OutlinedTextField(
                value = price,
                onValueChange = { price = it },
                label = { Text(stringResource(R.string.label_price)) },
                prefix = { Text("R") }, // South African Rand
                shape = shape,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(Modifier.height(16.dp))

            // ===== Post button =====
            Button(
                onClick = {
                    val p = price.toDoubleOrNull() ?: 0.0
                    val listing = Listing(
                        title = title.trim(),
                        price = p,
                        category = category.trim(),
                        size = size.trim(),
                        condition = condition.trim(),
                        description = description.trim(),
                        imageUris = images.map { it.toString() },
                        sellerName = "You",
                        isDraft = false
                    )
                    scope.launch {
                        try {
                            if (!isConnected) {
                                // Offline mode - enqueue for later sync
                                val payload = JSONObject().apply {
                                    put("type", "create_listing")
                                    put("title", listing.title)
                                    put("price", listing.price)
                                    put("category", listing.category)
                                    put("size", listing.size)
                                    put("condition", listing.condition)
                                    put("description", listing.description)
                                    put("images", listing.imageUris.joinToString(","))
                                }.toString()
                                OfflineRepository.addAction(context, payload)
                                
                                // Show offline success message
                                snackbarHostState.showSnackbar(
                                    context.getString(R.string.listing_saved_offline),
                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                )
                            } else {
                                // Online mode - save immediately
                                repo.add(listing)
                                snackbarHostState.showSnackbar(
                                    "Listing posted successfully!",
                                    duration = androidx.compose.material3.SnackbarDuration.Short
                                )
                            }
                            
                            // Add to MockCatalog for immediate display
                            MockCatalog.add(
                                title = listing.title,
                                price = listing.price,
                                category = listing.category,
                                condition = listing.condition,
                                imageUri = listing.imageUris.firstOrNull()
                            )
                            
                            nav.popBackStack()
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar(
                                "Error creating listing: ${e.message}",
                                duration = androidx.compose.material3.SnackbarDuration.Long
                            )
                        }
                    }
                },
                enabled = isValid, // only enabled when form passes simple validation
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text(if (isValid) stringResource(R.string.post_listing) else stringResource(R.string.enter_title_price))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

/* -------------------- Helpers -------------------- */

// Small square thumbnail that either shows an image or a “+” icon
@Composable
private fun ThumbBox(
    uri: Uri?,
    shape: RoundedCornerShape,
    borderColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(74.dp)
            .clip(shape)
            .border(1.dp, borderColor, shape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (uri != null) {
            AsyncImageCompat(uri)
        } else {
            Icon(Icons.Outlined.Add, contentDescription = "Add photo")
        }
    }
}

// Simple placeholder image loader
@Composable
private fun AsyncImageCompat(uri: Uri) {
    Image(
        painter = painterResource(id = android.R.drawable.picture_frame),
        contentDescription = uri.toString(),
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxSize()
    )
}

/** Material3 exposed dropdown wrapper (label + read-only text + menu) */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExposedDropdown(
    label: String,
    value: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    options: List<String>,
    onSelect: (String) -> Unit,
    shape: RoundedCornerShape
) {
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = onExpandedChange
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {},      // read-only → use menu to change
            readOnly = true,
            label = { Text(label) },
            modifier = Modifier
                .menuAnchor()        // correct anchor extension
                .fillMaxWidth(),
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
            shape = shape
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = { onSelect(option) }
                )
            }
        }
    }
}

/* -------------------- Previews -------------------- */

@Preview(
    name = "Create Listing – Light",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    device = "id:pixel_7"
)
@Composable
fun CreateListingPreviewLight() {
    ThriftlyTheme {
        val nav = rememberNavController()
        CreateListingScreen(nav = nav)
    }
}

@Preview(
    name = "Create Listing – Dark",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES,
    device = "id:pixel_7"
)
@Composable
private fun CreateListingPreviewDark() {
    ThriftlyTheme {
        val nav = rememberNavController()
        CreateListingScreen(nav = nav)
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
