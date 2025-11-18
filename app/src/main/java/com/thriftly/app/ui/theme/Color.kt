// Color.kt - Modern pastel green palette with dark mode support
package com.thriftly.app.ui.theme

import androidx.compose.ui.graphics.Color

// ===== Light Theme Colors =====
// Pastel green primary palette
val PastelGreen        = Color(0xFF81C784)    // Main pastel green
val PastelGreenLight   = Color(0xFFB2DFDB)    // Lighter variant
val PastelGreenDark    = Color(0xFF4CAF50)    // Darker for contrast
val PastelMint         = Color(0xFFC8E6C9)    // Soft mint accent

// Supporting colors
val SoftWhite          = Color(0xFFFAFAFA)    // Off-white background
val PureWhite          = Color(0xFFFFFFFF)    // Card backgrounds
val WarmGray           = Color(0xFF616161)    // Secondary text
val DarkGray           = Color(0xFF212121)    // Primary text
val LightGray          = Color(0xFFF5F5F5)    // Surface variants

// Semantic colors
val SuccessGreen       = Color(0xFF66BB6A)    // Success states
val WarningAmber       = Color(0xFFFFB74D)    // Warning states
val ErrorRed           = Color(0xFFEF5350)    // Error states

// ===== Dark Theme Colors =====
// Dark mode pastel green palette
val DarkPastelGreen    = Color(0xFF4CAF50)    // Primary in dark
val DarkGreenLight     = Color(0xFF81C784)    // Lighter variant
val DarkGreenAccent    = Color(0xFF388E3C)    // Darker accent
val DarkMint           = Color(0xFF2E7D32)    // Deep mint

// Dark mode backgrounds and surfaces
val DarkBackground     = Color(0xFF121212)    // Main dark background
val DarkSurface        = Color(0xFF1E1E1E)    // Card/surface color
val DarkSurfaceVariant = Color(0xFF2D2D2D)    // Elevated surfaces
val DarkOnSurface      = Color(0xFFE0E0E0)    // Text on dark surfaces
val DarkOnBackground   = Color(0xFFFFFFFF)    // Text on dark background

// Dark mode semantic colors
val DarkSuccessGreen   = Color(0xFF4CAF50)
val DarkWarningAmber   = Color(0xFFFF9800)
val DarkErrorRed       = Color(0xFFF44336)

// ===== Legacy Colors (for backward compatibility) =====
val Peach     = Color(0xFFF8B6A6)  // Keep for existing components
val LeafGreen = PastelGreenDark    // Map to new system
val Mint      = PastelMint         // Map to new system
val Ink       = DarkGray           // Map to new system
val MutedInk  = WarmGray          // Map to new system
val Paper     = SoftWhite          // Map to new system
val BrandGreen = PastelGreenDark   // Map to new system

/* 
References 

Material Design. 2025. Material Design 3 Guidelines. [Online]. Available at: https://m3.material.io/ [Accessed 15 Nov 2025].
*/
