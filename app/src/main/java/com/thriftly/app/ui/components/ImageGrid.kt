package com.thriftly.app.ui.components

import coil.compose.AsyncImage
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
// import coil.compose.rememberAsyncImagePainter // not needed when using AsyncImage

@Composable
fun ImageGrid(
    imageUris: List<String>,           // list of image URIs (strings)
    onClick: (String) -> Unit          // callback when an image is tapped
) {
    // 2-column grid with padding around content
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(8.dp)
    ) {
        items(imageUris) { uri ->
            // Card with spacing and full width per grid cell
            Card(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
                    .clickable { onClick(uri) } // notify which image was tapped
            ) {
                // Load and crop image to fill the card width
                AsyncImage(
                    model = uri,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(140.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
