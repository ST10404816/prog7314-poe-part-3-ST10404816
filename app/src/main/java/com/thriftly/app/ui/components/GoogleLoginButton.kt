package com.thriftly.app.ui.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.*
import androidx.compose.ui.unit.dp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

/**
 * Small reusable Google sign-in button used by the welcome/login screens.
 * Kept minimal: it launches the Google sign-in flow and returns basic profile
 * details via `onSignedIn` so callers can persist or navigate as needed.
 */
@Composable
fun GoogleLoginButton(
    modifier: Modifier = Modifier,
    onSignedIn: (name: String, email: String) -> Unit // callback with basic profile info
) {
    val context = LocalContext.current

    // Activity launcher to handle the result from the Google sign-in Intent
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Parses the sign-in result
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        runCatching { task.result }.onSuccess { acct ->
            // Safely extracts name/email (fallbacks provided)
            val name  = acct.displayName ?: acct.givenName ?: "User"
            val email = acct.email ?: ""
            if (email.isNotBlank()) onSignedIn(name, email)
        }.onFailure {
            // Intentionally quiet; callers can show a snackbar if they want.
        }
    }

    Button(
        onClick = {
            // Configures sign-in to request an email
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()

            // Creates a GoogleSignInClient and starts the sign-in Intent
            val client = GoogleSignIn.getClient(context, gso)
            launcher.launch(client.signInIntent)
        },
        shape = RoundedCornerShape(28.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
    ) {
        Text("Continue with Google")
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
