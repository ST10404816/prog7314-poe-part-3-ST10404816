# 👗 Thriftly — Second-hand Fashion Marketplace    
**PROG 7314 Part 3** | Lisha Naidoo ST10404816 | **18 November 2025**  

---

## 📖 Table of Contents
- [🧭 Overview](#-overview)
- [🚀 Quick Start](#-quick-start) 
- [✨ Core Features](#-core-features-implemented)
- [🧰 Technologies](#-technologies--libraries-used)
- [📱 User Experience](#-key-user-flows)
- [🌍 Internationalization](#-internationalization-support)
- [🧪 Quality Assurance](#-quality-assurance)
- [💬 AI Disclosure](#-academic-integrity--ai-disclosure)
- [📹 Demonstration Video](#-demonstration-video)
- [📚 References](#-references)
- [📄 License & Usage](#-license-&-usage)

---

## 🧭 Overview  
**Thriftly** is an Android application engineered with **Jetpack Compose** and **Material Design 3**. This secondhand fashion marketplace demonstrates advanced mobile development principles through modern UI/UX patterns, offline-first architecture, and inclusive accessibility features.

## 🚀 Quick Start

### **Prerequisites**
```bash
# Required Software
Android Studio Hedgehog (2023.1.1) or later
JDK 17 or higher
Android SDK 35
Gradle 8.0+
```

### **Installation & Setup**
```bash
# 1. Clone the repository
git clone https://github.com/VCDN-2025/prog7314-poe-part-3-ST10404816.git
cd thriftly

# 2. Open in Android Studio
# File -> Open -> Select project directory

# 3. Sync dependencies  
./gradlew build

# 4. Run on device or emulator
./gradlew installDebug
```

### **Configuration**
1. **Firebase Setup**: Add `google-services.json` to `app/src`
2. **API Keys**: Configure any external service keys in `local.properties`
3. **Build Variants**: Choose `debug` for development, `release` for production

---

## ✨ Core Features Implemented  

### 🛍️ **Marketplace Features**
| Feature | Description | Status |
|---------|-------------|---------|
| 💬 **Real-time Messaging** | Chat between buyers and sellers | ✅ Implemented |
| ❤️ **Advanced Wishlist** | Price tracking, notifications, alerts | ✅ Implemented |

### 🔧 **Technical Features**
| Feature | Description | Implementation |
|---------|-------------|---------------|
| 📱 **Offline-First Architecture** | Full functionality without internet | Background sync, local storage |
| 🔐 **Biometric Authentication** | Fingerprint/face unlock | BiometricPrompt integration |
| 🔄 **Background Sync** | WorkManager integration | Periodic sync with constraints |
| 🌐 **Connectivity Monitoring** | Real-time network status | Flow-based reactive updates |
| 📊 **State Management** | MVVM with reactive UI | StateFlow, ViewModels |

---

## 🧰 Technologies & Libraries Used  

### 🖥️ **Frontend / UI**
```kotlin
// Modern declarative UI
implementation("androidx.compose:compose-bom:2024.09.02")
implementation("androidx.compose.material3:material3")
implementation("androidx.compose.animation:animation")

// Enhanced image loading
implementation("io.coil-kt:coil-compose:2.7.0")
implementation("com.valentinilk.shimmer:compose-shimmer:1.3.0")
```

### 🗄️ **Backend / Data**
```kotlin
// Local database and storage
implementation("androidx.room:room-runtime:2.8.1")
implementation("androidx.datastore:datastore-preferences:1.1.1")

// Background processing
implementation("androidx.work:work-runtime-ktx:2.8.1")
```

### 🔐 **Security & Authentication**
```kotlin
// Biometric and Firebase authentication
implementation("androidx.biometric:biometric:1.1.0")
implementation("com.google.firebase:firebase-auth-ktx")
implementation("com.google.android.gms:play-services-auth:21.2.0")
```

### 🌐 **Networking**
```kotlin
// Modern HTTP client
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-moshi:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
```

---

## 🌍 **Internationalization Support**

### **Supported Languages**
1. **English** (Default) - Complete implementation
2. **Afrikaans** - Full translation with cultural adaptation  
3. **Zulu (IsiZulu)** - Native translation for local market

### **Technical Implementation**
- String externalization in `values-*/strings.xml`
- RTL layout support preparation
- Cultural number/date formatting
- Locale-aware context handling

---

## 📱 **Key User Flows**

### **Enhanced Shopping Experience**
1. **Browse** → Grid/List view with filters and search
2. **Discover** → Seller profiles with ratings and history  
3. **Communicate** → Real-time messaging with offer system
4. **Wishlist** → Save items with price monitoring
5. **Purchase** → Secure transaction flow with reviews

### **Seller Experience**  
1. **List Items** → Rich media upload with categorization
2. **Manage Orders** → Status tracking and communication
3. **Profile** → Build reputation through reviews
4. **Analytics** → View performance metrics

---

## 🧪 **Quality Assurance**

### **Testing Strategy**
- **Unit Tests**: ViewModel and Repository logic
- **Integration Tests**: Database operations  
- **UI Tests**: Compose testing framework
- **Accessibility Tests**: TalkBack validation

### **Code Quality**
- **Documentation**: Comprehensive KDoc comments
- **Architecture**: Clean separation of concerns  
- **Error Handling**: Graceful failure recovery
- **Performance**: Optimized recomposition and memory usage

---

## 🚀 **Performance Optimizations**

### **UI Performance**
- Lazy loading for large lists
- Image optimization with Coil
- Efficient recomposition through `remember` and `derivedStateOf`
- Animation performance targeting 60fps

### **Data Performance**  
- Local caching with Room database
- Background sync optimization
- Network request batching
- Memory leak prevention

---

## 💬 **AI Disclosure**

### **Transparent AI Tool Usage**
This project utilized AI assistance within appropriate academic boundaries:

#### **AI-Assisted Tasks** ✅
- **Documentation Generation**: KDoc comments and README structure
- **Code Review & Debugging**: Syntax error detection and best practices
- **Translation Verification**: Accuracy checking for Afrikaans/Zulu strings  
- **Architecture Guidance**: Android development patterns and optimization
- **Learning Support**: Explaining complex concepts and implementation approaches
  
---

## 📹 **Demonstration Video**

Youtube Video: <https://youtu.be/UGV8hPWygKw> 

---

## 📚 **References**

1. Android Developers. 2025. *Jetpack Compose Documentation*. [Online]. Available at:   
   <https://developer.android.com/jetpack/compose> [Accessed 1 Nov 2025].
2. Material Design. 2025. *Material Design 3 Guidelines*. [Online]. Available at:   
   <https://m3.material.io/> [Accessed 5 Nov 2025].
3. Android Developers. 2025. *Accessibility in Compose*. [Online]. Available at:  
   <https://developer.android.com/jetpack/compose/accessibility> [Accessed 1 Nov 2025].
4. Kotlin Documentation. 2025. *Coroutines and Flow*. [Online]. Available at:  
   <https://kotlinlang.org/docs/coroutines-overview.html> [Accessed 4 Nov 2025].
5. Google. 2025. *Android Architecture Guidelines*. [Online]. Available at:  
   <https://developer.android.com/topic/architecture> [Accessed 3 Nov 2025].
6. Android Developers. 2025. *Room Database Guide*. [Online]. Available at:  
   <https://developer.android.com/training/data-storage/room> [Accessed 1 Nov 2025].
7. Firebase. 2025. *Authentication Documentation*. [Online]. Available at:  
   <https://firebase.google.com/docs/auth/android/start> [Accessed 3 Nov 2025].

---

## 📄 **License & Usage**

### **Academic License**
This project is submitted as part of academic coursework for PROG 7314 at Emeris Durban North Campus. 


