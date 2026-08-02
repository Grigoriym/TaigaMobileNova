# JWT Token Testing Guide

Complete guide for testing JWT token validation and refresh token logic in Taiga.

---

## Table of Contents

1. [Testing Invalid Tokens](#testing-invalid-tokens)
2. [Testing Expired Tokens](#testing-expired-tokens)
3. [Testing Refresh Token Flow](#testing-refresh-token-flow)
4. [Client Implementation Examples](#client-implementation-examples)
5. [Common Error Scenarios](#common-error-scenarios)
6. [Troubleshooting](#troubleshooting)

---

## Testing Invalid Tokens

### 1. Malformed Token

```bash
# Test with a completely invalid token
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer invalid_token_here" \
  -v
```

**Expected Response (401):**
```json
{
  "detail": "Token is invalid or expired",
  "code": "token_not_valid",
  "messages": [
    {
      "token_class": "AccessToken",
      "token_type": "access",
      "message": "Token is invalid or expired"
    }
  ]
}
```

### 2. Modified Token (Invalid Signature)

```bash
# Login first to get a valid token
TOKEN=$(curl -X POST http://localhost:9000/api/v1/auth \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123123", "type": "normal"}' \
  | jq -r '.auth_token')

# Modify the token (change last character)
MODIFIED_TOKEN="${TOKEN%?}X"

# Try to use modified token
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer $MODIFIED_TOKEN" \
  -v
```

**Expected Response (401):**
```json
{
  "detail": "Token is invalid or expired",
  "code": "token_not_valid"
}
```

### 3. Missing Authorization Header

```bash
# Request without Authorization header
curl -X GET http://localhost:9000/api/v1/users/me -v
```

**Expected Response (401):**
```json
{
  "detail": "Authentication credentials were not provided."
}
```

### 4. Malformed Authorization Header

```bash
# Missing "Bearer" prefix
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: token_without_bearer" \
  -v

# Extra spaces
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer  token_with_extra_spaces" \
  -v
```

**Expected Response (401):**
```json
{
  "detail": "Authorization header must contain two space-delimited values",
  "code": "bad_authorization_header"
}
```

---

## Testing Expired Tokens

### Method 1: Use Test Script (Fastest)

The repository includes a test script to generate expired tokens:

```bash
# Run the test script
python test_expired_token.py
```

**Output:**
```
================================================================================
EXPIRED ACCESS TOKEN
================================================================================
User: admin
Expired at: 2026-01-05 09:30:00+00:00

Token:
eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ0b2tlbl90eXBlIjoiYWNjZXNzIiwiZXhwIjoxNzM2MDY3MDAwLCJqdGkiOiJhYmMxMjMiLCJ1c2VyX2lkIjoxfQ.signature_here
================================================================================

Test with:
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9..."
```

**Expected Response (401):**
```json
{
  "detail": "Token is invalid or expired",
  "code": "token_not_valid",
  "messages": [
    {
      "token_class": "AccessToken",
      "token_type": "access",
      "message": "Token 'exp' claim has expired"
    }
  ]
}
```

### Method 2: Modify Token Lifetime (For Development)

Temporarily change token lifetime to expire quickly:

1. Edit `settings/common.py`:

```python
SIMPLE_JWT = {
    'ACCESS_TOKEN_LIFETIME': timedelta(seconds=10),  # Token expires in 10 seconds
    'REFRESH_TOKEN_LIFETIME': timedelta(days=8),
    'CANCEL_TOKEN_LIFETIME': timedelta(days=100),
}
```

2. Restart the server:

```bash
python manage.py runserver
```

3. Login and get token:

```bash
# Get token
TOKEN=$(curl -X POST http://localhost:9000/api/v1/auth \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123123", "type": "normal"}' \
  | jq -r '.auth_token')

echo "Token: $TOKEN"

# Use immediately (should work)
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"

# Wait 15 seconds
echo "Waiting 15 seconds..."
sleep 15

# Try again (should fail with expired error)
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN" \
  -v
```

**Remember to revert the settings after testing!**

### Method 3: Wait for Natural Expiration

Default token lifetime is **24 hours**. Login, save the token, and test after 24+ hours.

```bash
# Save tokens to file
curl -X POST http://localhost:9000/api/v1/auth \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123123", "type": "normal"}' \
  > tokens.json

# Extract token
TOKEN=$(cat tokens.json | jq -r '.auth_token')

# Test after 24+ hours
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer $TOKEN"
```

---

## Testing Refresh Token Flow

### Complete Refresh Token Workflow

```bash
# Step 1: Login and save both tokens
echo "Step 1: Login..."
curl -X POST http://localhost:9000/api/v1/auth \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "123123",
    "type": "normal"
  }' > login_response.json

# Extract tokens
ACCESS_TOKEN=$(cat login_response.json | jq -r '.auth_token')
REFRESH_TOKEN=$(cat login_response.json | jq -r '.refresh')

echo "Access Token: $ACCESS_TOKEN"
echo "Refresh Token: $REFRESH_TOKEN"

# Step 2: Use access token (should work)
echo -e "\nStep 2: Using access token..."
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer $ACCESS_TOKEN" \
  | jq '.username'

# Step 3: Refresh to get new access token
echo -e "\nStep 3: Refreshing tokens..."
curl -X POST http://localhost:9000/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh\": \"$REFRESH_TOKEN\"}" \
  > refresh_response.json

# Extract new tokens
NEW_ACCESS_TOKEN=$(cat refresh_response.json | jq -r '.auth_token')
NEW_REFRESH_TOKEN=$(cat refresh_response.json | jq -r '.refresh')

echo "New Access Token: $NEW_ACCESS_TOKEN"
echo "New Refresh Token: $NEW_REFRESH_TOKEN"

# Step 4: Use new access token (should work)
echo -e "\nStep 4: Using new access token..."
curl -X GET http://localhost:9000/api/v1/users/me \
  -H "Authorization: Bearer $NEW_ACCESS_TOKEN" \
  | jq '.username'

# Step 5: Try to reuse old refresh token (should fail if rotation enabled)
echo -e "\nStep 5: Trying to reuse old refresh token..."
curl -X POST http://localhost:9000/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh\": \"$REFRESH_TOKEN\"}" \
  -v
```

**Expected Behavior:**

- Steps 1-4: All succeed ✅
- Step 5: Fails with 401 ❌ (if `ROTATE_REFRESH_TOKENS=True` and `DENYLIST_AFTER_ROTATION=True`)

**Step 5 Expected Response (401):**
```json
{
  "detail": "Token is denylisted",
  "code": "token_not_valid"
}
```

### Testing Refresh Token Rotation

Taiga uses **refresh token rotation** by default:
- `ROTATE_REFRESH_TOKENS = True` - New refresh token on each refresh
- `DENYLIST_AFTER_ROTATION = True` - Old refresh token is invalidated

**Test Script:**

```bash
#!/bin/bash

# Login
RESPONSE=$(curl -s -X POST http://localhost:9000/api/v1/auth \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123123", "type": "normal"}')

REFRESH1=$(echo $RESPONSE | jq -r '.refresh')
echo "Initial Refresh Token: $REFRESH1"

# First refresh
RESPONSE=$(curl -s -X POST http://localhost:9000/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh\": \"$REFRESH1\"}")

REFRESH2=$(echo $RESPONSE | jq -r '.refresh')
echo "After 1st refresh: $REFRESH2"

# Second refresh with new token (should work)
RESPONSE=$(curl -s -X POST http://localhost:9000/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh\": \"$REFRESH2\"}")

REFRESH3=$(echo $RESPONSE | jq -r '.refresh')
echo "After 2nd refresh: $REFRESH3"

# Try to use first refresh token again (should fail)
echo -e "\nTrying to reuse initial refresh token..."
curl -X POST http://localhost:9000/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh\": \"$REFRESH1\"}" \
  -v
```

### Testing Expired Refresh Token

```bash
# Method 1: Modify REFRESH_TOKEN_LIFETIME temporarily
# Edit settings/common.py:
# 'REFRESH_TOKEN_LIFETIME': timedelta(seconds=30),

# Login
RESPONSE=$(curl -s -X POST http://localhost:9000/api/v1/auth \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "123123", "type": "normal"}')

REFRESH_TOKEN=$(echo $RESPONSE | jq -r '.refresh')

# Wait for expiration
echo "Waiting 35 seconds for refresh token to expire..."
sleep 35

# Try to refresh (should fail)
curl -X POST http://localhost:9000/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d "{\"refresh\": \"$REFRESH_TOKEN\"}" \
  -v
```

**Expected Response (401):**
```json
{
  "detail": "Token is invalid or expired",
  "code": "token_not_valid",
  "messages": [
    {
      "token_class": "RefreshToken",
      "token_type": "refresh",
      "message": "Token 'exp' claim has expired"
    }
  ]
}
```

---

## Client Implementation Examples

### Kotlin Implementation

```kotlin

@Serializable
data class AuthTokens(
    @SerialName("auth_token")
    val accessToken: String,
    val refresh: String
)

class TokenManager(
    private val api: TaigaApiService,
    private val storage: SecureStorage
) {
    private val lock = ReentrantReadWriteLock()
    private var accessToken: String? = null
    private var refreshToken: String? = null
    private var isRefreshing = false
    private val refreshCallbacks = mutableListOf<CompletableDeferred<String>>()

    init {
        // Load tokens from secure storage
        accessToken = storage.getAccessToken()
        refreshToken = storage.getRefreshToken()
    }

    /**
     * Get valid access token, refreshing if necessary
     */
    suspend fun getAccessToken(): String? {
        lock.read {
            accessToken?.let { return it }
        }

        // No access token, try to refresh
        return refreshToken?.let { refreshAccessToken() }
    }

    /**
     * Save tokens after login
     */
    fun saveTokens(tokens: AuthTokens) {
        lock.write {
            accessToken = tokens.accessToken
            refreshToken = tokens.refresh
            storage.saveAccessToken(tokens.accessToken)
            storage.saveRefreshToken(tokens.refresh)
        }
    }

    /**
     * Refresh access token using refresh token
     */
    suspend fun refreshAccessToken(): String? {
        // Check if already refreshing
        lock.read {
            if (isRefreshing) {
                // Wait for ongoing refresh
                val deferred = CompletableDeferred<String>()
                refreshCallbacks.add(deferred)
                return deferred.await()
            }
        }

        lock.write { isRefreshing = true }

        return try {
            val currentRefreshToken = lock.read { refreshToken }
                ?: throw IllegalStateException("No refresh token available")

            val response = api.refreshToken(
                RefreshRequest(refresh = currentRefreshToken)
            )

            lock.write {
                accessToken = response.authToken
                storage.saveAccessToken(response.authToken)

                // Update refresh token if rotated
                response.refresh?.let {
                    refreshToken = it
                    storage.saveRefreshToken(it)
                }
            }

            // Notify waiting requests
            lock.write {
                refreshCallbacks.forEach { it.complete(response.authToken) }
                refreshCallbacks.clear()
                isRefreshing = false
            }

            response.authToken
        } catch (e: Exception) {
            // Refresh failed, clear tokens
            lock.write {
                accessToken = null
                refreshToken = null
                storage.clearTokens()
                refreshCallbacks.forEach { it.completeExceptionally(e) }
                refreshCallbacks.clear()
                isRefreshing = false
            }
            null
        }
    }

    /**
     * Clear all tokens (logout)
     */
    fun clearTokens() {
        lock.write {
            accessToken = null
            refreshToken = null
            storage.clearTokens()
        }
    }

    /**
     * Check if user is logged in
     */
    fun isLoggedIn(): Boolean {
        return lock.read { refreshToken != null }
    }
}

/**
 * OkHttp Interceptor for automatic token refresh
 */
class AuthInterceptor(
    private val tokenManager: TokenManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        // Skip auth for login/refresh endpoints
        if (originalRequest.url.encodedPath.endsWith("/auth") ||
            originalRequest.url.encodedPath.endsWith("/auth/refresh")) {
            return chain.proceed(originalRequest)
        }

        // Get access token
        val accessToken = runBlocking { tokenManager.getAccessToken() }
            ?: return chain.proceed(originalRequest) // No token, proceed without auth

        // Add Authorization header
        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()

        // Execute request
        val response = chain.proceed(authenticatedRequest)

        // Handle 401 Unauthorized
        if (response.code == 401) {
            response.close()

            // Try to refresh token
            val newAccessToken = runBlocking { tokenManager.refreshAccessToken() }

            if (newAccessToken != null) {
                // Retry with new token
                val retryRequest = originalRequest.newBuilder()
                    .header("Authorization", "Bearer $newAccessToken")
                    .build()
                return chain.proceed(retryRequest)
            } else {
                // Refresh failed, user needs to re-login
                // Emit event or navigate to login screen
                return response
            }
        }

        return response
    }
}

/**
 * Usage in API client
 */
class TaigaApiClient {
    private val tokenManager = TokenManager(api, secureStorage)

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(AuthInterceptor(tokenManager))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://localhost:9000/api/v1/")
        .client(okHttpClient)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()

    private val api = retrofit.create(TaigaApiService::class.java)

    /**
     * Login
     */
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return try {
            val response = api.auth(
                AuthRequest(
                    username = username,
                    password = password,
                    type = "normal"
                )
            )
            tokenManager.saveTokens(
                AuthTokens(
                    accessToken = response.authToken,
                    refresh = response.refresh
                )
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout
     */
    fun logout() {
        tokenManager.clearTokens()
    }

    /**
     * Check if logged in
     */
    fun isLoggedIn(): Boolean {
        return tokenManager.isLoggedIn()
    }
}
```

### Secure Storage Interface

```kotlin
interface SecureStorage {
    fun saveAccessToken(token: String)
    fun getAccessToken(): String?
    fun saveRefreshToken(token: String)
    fun getRefreshToken(): String?
    fun clearTokens()
}

/**
 * Android implementation using EncryptedSharedPreferences
 */
class AndroidSecureStorage(context: Context) : SecureStorage {
    private val sharedPreferences = EncryptedSharedPreferences.create(
        "taiga_secure_prefs",
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build(),
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override fun saveAccessToken(token: String) {
        sharedPreferences.edit().putString("access_token", token).apply()
    }

    override fun getAccessToken(): String? {
        return sharedPreferences.getString("access_token", null)
    }

    override fun saveRefreshToken(token: String) {
        sharedPreferences.edit().putString("refresh_token", token).apply()
    }

    override fun getRefreshToken(): String? {
        return sharedPreferences.getString("refresh_token", null)
    }

    override fun clearTokens() {
        sharedPreferences.edit().clear().apply()
    }
}
```

---

## Common Error Scenarios

### Scenario 1: Token Expired During Request

**What happens:**
1. Client has a valid token
2. Client sends request
3. Token expires while request is in flight
4. Server responds with 401

**Client handling:**
```kotlin
try {
    val response = api.getProjects()
} catch (e: HttpException) {
    if (e.code() == 401) {
        // Token expired, refresh and retry
        val newToken = tokenManager.refreshAccessToken()
        if (newToken != null) {
            // Retry request
            return api.getProjects()
        } else {
            // Refresh failed, logout
            logout()
        }
    }
}
```

### Scenario 2: Refresh Token Expired

**What happens:**
1. Access token expired
2. Client tries to refresh
3. Refresh token also expired
4. Server responds with 401

**Client handling:**
```kotlin
suspend fun refreshAccessToken(): String? {
    return try {
        val response = api.refreshToken(RefreshRequest(refreshToken!!))
        saveTokens(response)
        response.authToken
    } catch (e: HttpException) {
        if (e.code() == 401) {
            // Refresh token expired, user must re-login
            clearTokens()
            navigateToLogin()
            null
        } else {
            throw e
        }
    }
}
```

### Scenario 3: Concurrent Requests During Refresh

**What happens:**
1. Multiple API calls happen simultaneously
2. First call detects expired token and starts refresh
3. Other calls should wait for refresh to complete

**Client handling:**
- Use a mutex/lock to ensure only one refresh happens at a time
- Queue other requests until refresh completes
- See `TokenManager` implementation above

### Scenario 4: User Logged Out on Another Device

**What happens:**
1. User logs out on device A
2. Device B still has valid tokens
3. Refresh token might be denylisted (depending on implementation)

**Client handling:**
```kotlin
// Periodically check token validity
suspend fun validateToken() {
    try {
        api.verifyToken(VerifyRequest(token = accessToken))
    } catch (e: HttpException) {
        if (e.code() == 401) {
            // Token invalid, logout
            logout()
        }
    }
}
```

---

## Troubleshooting

### Problem: Getting 401 with valid-looking token

**Check:**
1. Is the token properly formatted? (JWT has 3 parts separated by dots)
2. Is the Authorization header correct? (`Authorization: Bearer <token>`)
3. Has the token expired? (Check `exp` claim)
4. Is the user still active in the database?
5. Is the token denylisted?

**Debug:**
```bash
# Decode token (without verification) to check claims
TOKEN="your_token_here"
echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | jq .
```

### Problem: Refresh token not rotating

**Check settings:**
```python
SIMPLE_JWT = {
    'ROTATE_REFRESH_TOKENS': True,  # Should be True
    ...
}
```

**Verify response includes new refresh token:**
```bash
curl -X POST http://localhost:9000/api/v1/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refresh": "..."}' \
  | jq '.refresh'
```

### Problem: Old refresh tokens still work

**Check denylist setting:**
```python
SIMPLE_JWT = {
    'DENYLIST_AFTER_ROTATION': True,  # Should be True
    ...
}
```

**Check if denylist app is installed:**
```python
INSTALLED_APPS = [
    ...
    'taiga.auth.token_denylist',  # Should be present
    ...
]
```

**Check denylist in database:**
```sql
SELECT * FROM auth_outstandingtoken WHERE jti = 'token_jti_here';
SELECT * FROM auth_denylistedtoken;
```

### Problem: Tokens expire too quickly/slowly

**Check token lifetime settings:**
```python
# settings/common.py
SIMPLE_JWT = {
    'ACCESS_TOKEN_LIFETIME': timedelta(hours=24),  # Default: 24 hours
    'REFRESH_TOKEN_LIFETIME': timedelta(days=8),   # Default: 8 days
}
```

**Verify token expiration:**
```bash
# Decode token and check 'exp' claim
TOKEN="your_token_here"
echo $TOKEN | cut -d'.' -f2 | base64 -d 2>/dev/null | jq '.exp'

# Convert epoch to human-readable date
date -d @1736067000
```

---

## Additional Resources

### Useful Commands

**Flush expired tokens from denylist:**
```bash
python manage.py flushexpiredtokens
```

**Create superuser for testing:**
```bash
python manage.py createsuperuser
```

**Django shell testing:**
```python
python manage.py shell

from taiga.auth.tokens import AccessToken, RefreshToken
from taiga.users.models import User

user = User.objects.get(username='admin')
access = AccessToken.for_user(user)
refresh = RefreshToken.for_user(user)

print(f"Access: {access}")
print(f"Refresh: {refresh}")
print(f"Payload: {access.payload}")
```

### Testing Checklist

- [ ] Login with valid credentials returns tokens
- [ ] Login with invalid credentials returns 401
- [ ] API request with valid token succeeds
- [ ] API request with invalid token returns 401
- [ ] API request with expired token returns 401
- [ ] Refresh token with valid refresh token succeeds
- [ ] Refresh token with invalid refresh token returns 401
- [ ] Refresh token with expired refresh token returns 401
- [ ] Refresh token rotation works (new refresh token returned)
- [ ] Old refresh token is denylisted after rotation
- [ ] Reusing old refresh token returns 401
- [ ] Concurrent refresh requests handled correctly
- [ ] Token expiration times are correct

---

## Summary

**Key Points:**

1. **Access tokens** expire after 24 hours (default)
2. **Refresh tokens** expire after 8 days (default)
3. **Refresh token rotation** is enabled by default
4. **Old refresh tokens** are denylisted after rotation
5. **401 responses** indicate invalid/expired tokens
6. **Client should automatically refresh** when receiving 401
7. **Store tokens securely** (encrypted storage on mobile)
8. **Handle concurrent refreshes** to avoid race conditions

**Best Practices:**

- ✅ Always include `Authorization: Bearer <token>` header
- ✅ Implement automatic token refresh on 401
- ✅ Store refresh token securely
- ✅ Handle expired refresh tokens by logging out user
- ✅ Use mutex/lock for concurrent refresh attempts
- ✅ Clear tokens on logout
- ✅ Validate token format before sending requests
