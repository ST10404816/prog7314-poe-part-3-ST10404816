plugins {
    id("com.android.application") version "8.13.1" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.21" apply false

    // KSP plugin – needed for Room
    id("com.google.devtools.ksp") version "2.0.21-1.0.26" apply false

    // Firebase / Google services plugin
    id("com.google.gms.google-services") version "4.4.2" apply false
}
