package com.thriftly.app.ui.screens

/*
 * WelcomeScreen
 *
 * Small, focused composable that shows the app logo and primary auth actions:
 * "Log in", "Sign up", and Google SSO. Kept intentionally simple so it's
 * straightforward to maintain and matches the app's initial onboarding flow.
 */
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.thriftly.app.R
import kotlinx.coroutines.launch

@Composable
fun WelcomeScreen(
    onLogin: () -> Unit,         // navigate to Login screen
    onSignup: () -> Unit,        // navigate to Sign Up screen
    onGoogle: () -> Unit,        // called after Google sign-in succeeds (e.g., navigate home)
    logoRes: Int = R.drawable.logo,
    googleIconRes: Int? = null   // optional colored Google "G" icon
) {
    // --- Local palette just for this screen (could move to theme) ---
    val bgOffWhite = Color(0xFFFFFEFA)
    val brandGreen = Color(0xFF113C2F)
    val coral = Color(0xFFFFA39A)
    val googleOutline = Color(0xFFCBD5E1)
    val textOnCoral = Color(0xFF0E1A0E)

    // Context + coroutine scope for AuthStore on success
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    // Launcher to handle result from Google sign-in intent
    val googleLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        runCatching { task.result }.onSuccess { acct ->
            val name  = acct.displayName ?: acct.givenName ?: "User"
            val email = acct.email.orEmpty()
            if (email.isNotBlank()) {
                scope.launch {
                    // Store minimal user + mark logged-in (mock)
                    com.thriftly.app.data.auth.AuthStore.googleLogin(ctx, name, email)
                    onGoogle()
                }
            }
        }.onFailure {
            // TODO: toast/snackbar if needed
        }
    }

    // Screen background + content wrapper
    Surface(color = bgOffWhite, contentColor = MaterialTheme.colorScheme.onBackground) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo tile
                Image(
                    painter = painterResource(logoRes),
                    contentDescription = "Logo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(24.dp))
                )

                Spacer(Modifier.height(20.dp))

                // App name
                Text(
                    text = stringResource(R.string.app_title),
                    color = brandGreen,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = (-0.5).sp
                )

                Spacer(Modifier.height(28.dp))

                // Primary actions
                Button(
                    onClick = onLogin,
                    colors = ButtonDefaults.buttonColors(containerColor = coral, contentColor = textOnCoral),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text(stringResource(R.string.login), fontSize = 18.sp, fontWeight = FontWeight.Medium) }

                Spacer(Modifier.height(14.dp))

                Button(
                    onClick = onSignup,
                    colors = ButtonDefaults.buttonColors(containerColor = coral, contentColor = textOnCoral),
                    shape = RoundedCornerShape(28.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) { Text(stringResource(R.string.signup), fontSize = 18.sp, fontWeight = FontWeight.Medium) }

                Spacer(Modifier.height(16.dp))

                // Divider with "or"
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Divider(modifier = Modifier.weight(1f))
                    Text("  ${stringResource(R.string.or)}  ", style = MaterialTheme.typography.bodySmall)
                    Divider(modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                // Google SSO (outlined white button)
                OutlinedButton(
                    onClick = {
                        // Build sign-in request (email only; add .requestIdToken(...) for Firebase Auth)
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestEmail()
                            .build()
                        val client = GoogleSignIn.getClient(ctx, gso)
                        googleLauncher.launch(client.signInIntent)
                    },
                    shape = RoundedCornerShape(28.dp),
                    border = BorderStroke(1.dp, googleOutline),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFF111827)
                    ),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    // Optional colored G icon (or simple fallback)
                    if (googleIconRes != null) {
                        Icon(
                            painter = painterResource(googleIconRes),
                            contentDescription = "Google",
                            tint = Color.Unspecified,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                    } else {
                        Box(
                            modifier = Modifier.size(22.dp).clip(CircleShape).background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("G", color = Color(0xFF4285F4), fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(10.dp))
                    }
                    Text(stringResource(R.string.continue_with_google), fontSize = 16.sp)
                }
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFCFCF7)
@Composable
private fun WelcomePreview() {
    MaterialTheme {
        WelcomeScreen(onLogin = {}, onSignup = {}, onGoogle = {})
    }
}

/* 
References 

Android Developers. 2025. Jetpack Compose Documentation. [Online]. Available at: https://developer.android.com/jetpack/compose/documentation [Accessed 17 Nov 2025].
*/
