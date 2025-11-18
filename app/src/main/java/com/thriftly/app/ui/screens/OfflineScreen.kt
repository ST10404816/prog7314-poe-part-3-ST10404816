package com.thriftly.app.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.thriftly.app.OfflineRepository
import kotlinx.coroutines.launch
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.res.stringResource
import com.thriftly.app.R

@Composable
fun OfflineScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val actions by OfflineRepository.allActionsFlow(context).collectAsState(initial = emptyList())
    var text by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text(stringResource(R.string.offline_actions_title))

        androidx.compose.material3.OutlinedTextField(value = text, onValueChange = { text = it }, label = { Text(stringResource(R.string.action_label)) })
        Button(onClick = {
            if (text.isNotBlank()) {
                scope.launch { OfflineRepository.addAction(context, text.trim()) }
                text = ""
            }
        }) { Text(stringResource(R.string.add_offline_action)) }

        Button(onClick = {
            if (context is com.thriftly.app.MainActivity) context.syncPendingNow()
        }) { Text(stringResource(R.string.sync_now)) }

        androidx.compose.foundation.lazy.LazyColumn {
            items(actions) { item ->
                val status = if (item.synced) stringResource(R.string.synced) else stringResource(R.string.pending)
                Text("${item.content} $status")
            }
        }

        Button(onClick = onBack) { Text(stringResource(R.string.back)) }
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
