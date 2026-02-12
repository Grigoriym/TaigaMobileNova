# ConnectivityManagerNetworkMonitor Implementation Guide

## Overview

`ConnectivityManagerNetworkMonitor` is a reactive network connectivity monitor that exposes network status as a Kotlin `Flow<Boolean>`. It's part of the Now in Android architecture, demonstrating clean separation of concerns, dependency injection with Hilt, and reactive programming with Coroutines.

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│  UI Layer (Compose)                                             │
│  NiaApp.kt - Collects isOffline Flow, shows Snackbar           │
└────────────────────────────┬────────────────────────────────────┘
                             │ collectAsStateWithLifecycle()
┌────────────────────────────▼────────────────────────────────────┐
│  State Holder                                                   │
│  NiaAppState - Transforms isOnline → isOffline StateFlow       │
└────────────────────────────┬────────────────────────────────────┘
                             │ inject NetworkMonitor
┌────────────────────────────▼────────────────────────────────────┐
│  Data Layer                                                     │
│  NetworkMonitor interface ← ConnectivityManagerNetworkMonitor  │
└─────────────────────────────────────────────────────────────────┘
```

## File Structure

```
core/
├── common/src/main/kotlin/.../network/
│   ├── NiaDispatchers.kt          # Dispatcher qualifier annotation
│   └── di/DispatchersModule.kt    # Provides IO/Default dispatchers
│
├── data/src/main/kotlin/.../util/
│   ├── NetworkMonitor.kt          # Interface definition
│   └── ConnectivityManagerNetworkMonitor.kt  # Implementation
│
├── data/src/main/kotlin/.../di/
│   └── DataModule.kt              # Binds implementation to interface
│
├── testing/src/main/kotlin/.../util/
│   └── TestNetworkMonitor.kt      # Controllable test implementation
│
└── data-test/src/main/kotlin/.../test/
    └── AlwaysOnlineNetworkMonitor.kt  # Simple test stub
```

---

## Implementation Details

### 1. Interface Definition

**File:** `NetworkMonitor.kt`

```kotlin
package com.example.core.data.util

import kotlinx.coroutines.flow.Flow

/**
 * Utility for reporting app connectivity status
 */
interface NetworkMonitor {
    val isOnline: Flow<Boolean>
}
```

### 2. Dispatcher Qualifier

**File:** `NiaDispatchers.kt`

```kotlin
package com.example.core.common.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val niaDispatcher: NiaDispatchers)

enum class NiaDispatchers {
    Default,
    IO,
}
```

### 3. Dispatcher Module

**File:** `DispatchersModule.kt`

```kotlin
package com.example.core.common.network.di

import com.example.core.common.network.Dispatcher
import com.example.core.common.network.NiaDispatchers.IO
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

@Module
@InstallIn(SingletonComponent::class)
object DispatchersModule {
    @Provides
    @Dispatcher(IO)
    fun providesIODispatcher(): CoroutineDispatcher = Dispatchers.IO
}
```

### 4. Core Implementation

**File:** `ConnectivityManagerNetworkMonitor.kt`

```kotlin
package com.example.core.data.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.ConnectivityManager.NetworkCallback
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.core.content.getSystemService
import com.example.core.common.network.Dispatcher
import com.example.core.common.network.NiaDispatchers.IO
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

internal class ConnectivityManagerNetworkMonitor @Inject constructor(
    @ApplicationContext private val context: Context,
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
) : NetworkMonitor {

    override val isOnline: Flow<Boolean> = callbackFlow {
        val connectivityManager = context.getSystemService<ConnectivityManager>()

        // Handle case where ConnectivityManager is unavailable
        if (connectivityManager == null) {
            channel.trySend(false)
            channel.close()
            return@callbackFlow
        }

        /**
         * Tracks ALL networks matching the request, not just the active one.
         * This allows detecting connectivity through any available network.
         */
        val callback = object : NetworkCallback() {
            private val networks = mutableSetOf<Network>()

            override fun onAvailable(network: Network) {
                networks += network
                channel.trySend(true)
            }

            override fun onLost(network: Network) {
                networks -= network
                channel.trySend(networks.isNotEmpty())
            }
        }

        // Register for networks with internet capability
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        // Send initial connectivity status
        channel.trySend(connectivityManager.isCurrentlyConnected())

        // Unregister callback when flow collection stops
        awaitClose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }
        .flowOn(ioDispatcher)  // Execute on IO dispatcher
        .conflate()            // Drop intermediate values if consumer is slow

    private fun ConnectivityManager.isCurrentlyConnected(): Boolean {
        val networkCapabilities = getNetworkCapabilities(activeNetwork) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
```

### 5. DI Binding

**File:** `DataModule.kt`

```kotlin
package com.example.core.data.di

import com.example.core.data.util.ConnectivityManagerNetworkMonitor
import com.example.core.data.util.NetworkMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    internal abstract fun bindsNetworkMonitor(
        networkMonitor: ConnectivityManagerNetworkMonitor,
    ): NetworkMonitor
}
```

---

## Usage in UI

### Injecting into Activity

**File:** `MainActivity.kt`

```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val appState = rememberAppState(networkMonitor = networkMonitor)
            MyApp(appState)
        }
    }
}
```

### State Holder

**File:** `AppState.kt`

```kotlin
@Stable
class AppState(
    coroutineScope: CoroutineScope,
    networkMonitor: NetworkMonitor,
) {
    // Invert the flow: isOnline → isOffline
    val isOffline = networkMonitor.isOnline
        .map(Boolean::not)
        .stateIn(
            scope = coroutineScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = false,
        )
}

@Composable
fun rememberAppState(
    networkMonitor: NetworkMonitor,
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
): AppState {
    return remember(coroutineScope, networkMonitor) {
        AppState(coroutineScope, networkMonitor)
    }
}
```

### Displaying Offline Status

**File:** `MyApp.kt`

```kotlin
@Composable
fun MyApp(appState: AppState) {
    val snackbarHostState = remember { SnackbarHostState() }
    val isOffline by appState.isOffline.collectAsStateWithLifecycle()

    val notConnectedMessage = stringResource(R.string.not_connected)

    LaunchedEffect(isOffline) {
        if (isOffline) {
            snackbarHostState.showSnackbar(
                message = notConnectedMessage,
                duration = SnackbarDuration.Indefinite,
            )
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) {
        // App content
    }
}
```

---

## Testing

### Test Implementation

**File:** `TestNetworkMonitor.kt`

```kotlin
class TestNetworkMonitor : NetworkMonitor {
    private val connectivityFlow = MutableStateFlow(true)

    override val isOnline: Flow<Boolean> = connectivityFlow

    fun setConnected(isConnected: Boolean) {
        connectivityFlow.value = isConnected
    }
}
```

### Simple Stub for UI Tests

**File:** `AlwaysOnlineNetworkMonitor.kt`

```kotlin
class AlwaysOnlineNetworkMonitor @Inject constructor() : NetworkMonitor {
    override val isOnline: Flow<Boolean> = flowOf(true)
}
```

### Test Module

```kotlin
@Module
@InstallIn(SingletonComponent::class)
abstract class TestDataModule {
    @Binds
    abstract fun bindsNetworkMonitor(
        networkMonitor: AlwaysOnlineNetworkMonitor,
    ): NetworkMonitor
}
```

---

## Key Design Decisions

| Decision | Rationale |
|----------|-----------|
| **Interface abstraction** | Enables easy testing and swappable implementations |
| **`callbackFlow`** | Bridges callback-based Android API to Kotlin Flow |
| **Track multiple networks** | Device may have WiFi + cellular; online if ANY has internet |
| **`.conflate()`** | Prevents backpressure; consumer always gets latest state |
| **`.flowOn(ioDispatcher)`** | Network operations run off main thread |
| **`awaitClose { }`** | Ensures callback is unregistered when flow stops |
| **`NET_CAPABILITY_INTERNET`** | Only tracks networks with actual internet access |
| **`internal` visibility** | Implementation hidden; consumers use interface only |

---

## Required Dependencies

```kotlin
// build.gradle.kts
dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-compiler:2.48")

    // Optional: for tracing
    implementation("androidx.tracing:tracing-ktx:1.2.0")
}
```

## Required Permission

```xml
<!-- AndroidManifest.xml -->
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

---

This pattern provides a clean, testable, and reactive way to monitor network connectivity that integrates well with modern Android architecture using Compose, Hilt, and Kotlin Flows.
