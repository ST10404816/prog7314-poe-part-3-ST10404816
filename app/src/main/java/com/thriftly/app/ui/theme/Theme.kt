// Theme.kt - Modern Material3 theme with pastel green palette
package com.thriftly.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color

// ===== Light Color Scheme =====
val LightColors = lightColorScheme(
    // Primary colors - pastel green palette
    primary = PastelGreen,
    onPrimary = PureWhite,
    primaryContainer = PastelGreenLight,
    onPrimaryContainer = DarkGray,
    
    // Secondary colors - complementary mint
    secondary = PastelMint,
    onSecondary = DarkGray,
    secondaryContainer = PastelGreenLight,
    onSecondaryContainer = DarkGray,
    
    // Tertiary colors - soft accents
    tertiary = SuccessGreen,
    onTertiary = PureWhite,
    tertiaryContainer = PastelGreenLight,
    onTertiaryContainer = DarkGray,
    
    // Background and surface colors
    background = SoftWhite,
    onBackground = DarkGray,
    surface = PureWhite,
    onSurface = DarkGray,
    surfaceVariant = LightGray,
    onSurfaceVariant = WarmGray,
    
    // Semantic colors
    error = ErrorRed,
    onError = PureWhite,
    errorContainer = Color(0xFFFFEBEE),
    onErrorContainer = Color(0xFFB71C1C),
    
    // Outline colors
    outline = WarmGray,
    outlineVariant = LightGray,
    
    // Other system colors
    inverseSurface = DarkGray,
    inverseOnSurface = PureWhite,
    inversePrimary = DarkPastelGreen,
    surfaceTint = PastelGreen,
    scrim = Color(0x80000000)
)

// ===== Dark Color Scheme =====
val DarkColors = darkColorScheme(
    // Primary colors - adjusted for dark theme
    primary = DarkPastelGreen,
    onPrimary = DarkBackground,
    primaryContainer = DarkGreenAccent,
    onPrimaryContainer = DarkOnSurface,
    
    // Secondary colors
    secondary = DarkGreenLight,
    onSecondary = DarkBackground,
    secondaryContainer = DarkMint,
    onSecondaryContainer = DarkOnSurface,
    
    // Tertiary colors
    tertiary = DarkSuccessGreen,
    onTertiary = DarkBackground,
    tertiaryContainer = DarkGreenAccent,
    onTertiaryContainer = DarkOnSurface,
    
    // Background and surface colors
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = Color(0xFFBDBDBD),
    
    // Semantic colors
    error = DarkErrorRed,
    onError = DarkBackground,
    errorContainer = Color(0xFF2E1114),
    onErrorContainer = Color(0xFFFFB4AB),
    
    // Outline colors
    outline = Color(0xFF757575),
    outlineVariant = Color(0xFF424242),
    
    // Other system colors
    inverseSurface = Color(0xFFE4E4E4),
    inverseOnSurface = DarkBackground,
    inversePrimary = PastelGreen,
    surfaceTint = DarkPastelGreen,
    scrim = Color(0x80000000)
)

// ===== Modern Shape System =====
val ModernShapes = Shapes(
    extraSmall = RoundedCornerShape(4.dp),   // Small chips, badges
    small = RoundedCornerShape(8.dp),        // Small cards, buttons
    medium = RoundedCornerShape(12.dp),      // Standard cards, dialogs
    large = RoundedCornerShape(16.dp),       // Large cards, sheets
    extraLarge = RoundedCornerShape(24.dp)   // Full-screen dialogs
)

// ===== App Theme Wrapper =====
@Composable
fun ThriftlyTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = ModernShapes,
        typography = ThriftlyTypography,
        content = content
    )
}

// ===== Legacy Theme (for backward compatibility) =====
@Deprecated("Use ThriftlyTheme instead", ReplaceWith("ThriftlyTheme(content = content)"))
val RoundedShapes = ModernShapes

/* 
References 

Material Design. 2025. Material Design 3 Guidelines. [Online]. Available at: https://m3.material.io/ [Accessed 15 Nov 2025].
*/
