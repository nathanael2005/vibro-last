package com.example
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.util.Base64
import android.util.Log
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.UUID

// --- Supabase Network Response Models ---

@JsonClass(generateAdapter = true)
data class SupabaseProduct(
    @Json(name = "id") val id: String?,
    @Json(name = "title") val title: String?,
    @Json(name = "price") val price: String?,
    @Json(name = "location") val location: String?,
    @Json(name = "condition") val condition: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "imageUrl") val imageUrlCamel: String? = null,
    @Json(name = "image_url") val imageUrlSnake: String? = null,
    @Json(name = "image") val imageSimple: String? = null,
    @Json(name = "img") val imageUrlImg: String? = null,
    @Json(name = "photo") val imageUrlPhoto: String? = null,
    @Json(name = "categoryId") val categoryIdCamel: String? = null,
    @Json(name = "category_id") val categoryIdSnake: String? = null,
    @Json(name = "sellerName") val sellerNameCamel: String? = null,
    @Json(name = "seller_name") val sellerNameSnake: String? = null,
    @Json(name = "isPromoted") val isPromotedCamel: Boolean? = null,
    @Json(name = "is_promoted") val isPromotedSnake: Boolean? = null,
    @Json(name = "timeAgo") val timeAgoCamel: String? = null,
    @Json(name = "time_ago") val time_ago: String? = null,
    @Json(name = "created_at") val createdAt: String? = null
) {
    constructor(
        id: String,
        title: String,
        price: String,
        imageUrl: String,
        location: String,
        condition: String,
        description: String,
        categoryId: String,
        sellerName: String,
        isPromoted: Boolean,
        timeAgo: String
    ) : this(
        id = id,
        title = title,
        price = price,
        location = location,
        condition = condition,
        description = description,
        imageUrlCamel = imageUrl,
        imageUrlSnake = imageUrl,
        imageSimple = imageUrl,
        imageUrlImg = imageUrl,
        imageUrlPhoto = imageUrl,
        categoryIdCamel = categoryId,
        categoryIdSnake = categoryId,
        sellerNameCamel = sellerName,
        sellerNameSnake = sellerName,
        isPromotedCamel = isPromoted,
        isPromotedSnake = isPromoted,
        timeAgoCamel = timeAgo,
        time_ago = timeAgo,
        createdAt = timeAgo
    )

    val imageUrl: String get() {
        val raw = imageUrlCamel ?: imageUrlSnake ?: imageSimple ?: imageUrlImg ?: imageUrlPhoto ?: ""
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return ""
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed
        }
        if (trimmed.startsWith("data:image")) {
            return trimmed
        }
        val cleanSupabaseUrl = NetworkManager.supabaseUrl.trim().removeSuffix("/")
        if (cleanSupabaseUrl.isNotEmpty()) {
            if (trimmed.contains("/")) {
                if (trimmed.startsWith("public/")) {
                    return "$cleanSupabaseUrl/storage/v1/object/$trimmed"
                }
                return "$cleanSupabaseUrl/storage/v1/object/public/$trimmed"
            } else {
                return "$cleanSupabaseUrl/storage/v1/object/public/ads/$trimmed"
            }
        }
        return trimmed
    }
    val categoryId: String get() = categoryIdCamel ?: categoryIdSnake ?: "1"
    val sellerName: String get() = sellerNameCamel ?: sellerNameSnake ?: "Verified Seller"
    val isPromoted: Boolean get() = isPromotedCamel ?: isPromotedSnake ?: false
    val timeAgo: String get() = timeAgoCamel ?: time_ago ?: createdAt ?: "Just now"
}

@JsonClass(generateAdapter = true)
data class SupabaseChatThread(
    @Json(name = "id") val id: String,
    @Json(name = "senderName") val senderName: String,
    @Json(name = "lastMessage") val lastMessage: String,
    @Json(name = "time") val time: String
)

@JsonClass(generateAdapter = true)
data class SupabaseChatMessage(
    @Json(name = "id") val id: String? = null,
    @Json(name = "threadId") val threadId: String,
    @Json(name = "sender") val sender: String,
    @Json(name = "content") val content: String,
    @Json(name = "time") val time: String
)

@JsonClass(generateAdapter = true)
data class ImageKitUploadResponse(
    @Json(name = "fileId") val fileId: String?,
    @Json(name = "name") val name: String?,
    @Json(name = "url") val url: String?,
    @Json(name = "thumbnailUrl") val thumbnailUrl: String?
)

@JsonClass(generateAdapter = true)
data class BaseUserMetadata(
    @Json(name = "name") val name: String,
    @Json(name = "city") val city: String
)

@JsonClass(generateAdapter = true)
data class SupabaseSignUpRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String,
    @Json(name = "data") val data: BaseUserMetadata? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseSignInRequest(
    @Json(name = "email") val email: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthUserMetadata(
    @Json(name = "name") val name: String? = null,
    @Json(name = "city") val city: String? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthUser(
    @Json(name = "id") val id: String,
    @Json(name = "email") val email: String?,
    @Json(name = "user_metadata") val userMetadata: SupabaseAuthUserMetadata? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "token_type") val tokenType: String?,
    @Json(name = "expires_in") val expiresIn: Long?,
    @Json(name = "refresh_token") val refreshToken: String?,
    @Json(name = "user") val user: SupabaseAuthUser? = null
)

// --- Retrofit API Interfaces ---

interface SupabaseAuthApi {
    @POST("signup")
    suspend fun signUp(
        @Body request: SupabaseSignUpRequest
    ): Response<SupabaseAuthResponse>

    @POST("token?grant_type=password")
    suspend fun signIn(
        @Body request: SupabaseSignInRequest
    ): Response<SupabaseAuthResponse>
}

interface SupabaseApi {
    @GET("products")
    suspend fun getProducts(
        @Query("select") select: String = "*"
    ): List<Map<String, @JvmSuppressWildcards Any>>

    @POST("products")
    suspend fun insertProduct(
        @Body product: Map<String, @JvmSuppressWildcards Any>,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<Map<String, @JvmSuppressWildcards Any>>

    @GET("ads")
    suspend fun getAds(
        @Query("select") select: String = "*"
    ): List<Map<String, @JvmSuppressWildcards Any>>

    @POST("ads")
    suspend fun insertAd(
        @Body product: Map<String, @JvmSuppressWildcards Any>,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<Map<String, @JvmSuppressWildcards Any>>

    @PATCH("products")
    suspend fun updateProduct(
        @Query("id") idQuery: String,
        @Body product: Map<String, @JvmSuppressWildcards Any>
    ): List<Map<String, @JvmSuppressWildcards Any>>

    @PATCH("ads")
    suspend fun updateAd(
        @Query("id") idQuery: String,
        @Body product: Map<String, @JvmSuppressWildcards Any>
    ): List<Map<String, @JvmSuppressWildcards Any>>

    @DELETE("products")
    suspend fun deleteProduct(
        @Query("id") idQuery: String
    ): List<Map<String, @JvmSuppressWildcards Any>>

    @DELETE("ads")
    suspend fun deleteAd(
        @Query("id") idQuery: String
    ): List<Map<String, @JvmSuppressWildcards Any>>

    @GET("chat_threads")
    suspend fun getChatThreads(
        @Query("select") select: String = "*"
    ): List<SupabaseChatThread>

    @POST("chat_threads")
    suspend fun insertChatThread(
        @Body thread: SupabaseChatThread,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<SupabaseChatThread>

    @GET("chat_messages")
    suspend fun getChatMessages(
        @Query("select") select: String = "*"
    ): List<SupabaseChatMessage>

    @POST("chat_messages")
    suspend fun insertChatMessage(
        @Body message: SupabaseChatMessage,
        @Header("Prefer") prefer: String = "return=representation"
    ): List<SupabaseChatMessage>
}

interface ImageKitApi {
    @Multipart
    @POST("files/upload")
    suspend fun uploadFile(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part,
        @Part("fileName") fileName: okhttp3.RequestBody,
        @Part("publicKey") publicKey: okhttp3.RequestBody
    ): Response<ImageKitUploadResponse>
}

interface SupabaseStorageApi {
    @POST("object/{bucket}/{filename}")
    suspend fun uploadFile(
        @Header("Authorization") authHeader: String,
        @Header("apikey") apiKey: String,
        @Path("bucket") bucket: String,
        @Path("filename") filename: String,
        @Body file: okhttp3.RequestBody
    ): Response<Map<String, Any>>
}

// --- Network Service Singleton Manager ---

object NetworkManager {
    private const val TAG = "NetworkManager"
    var syncErrorMessage by androidx.compose.runtime.mutableStateOf<String?>(null)
    var syncItemCount by androidx.compose.runtime.mutableStateOf<Int>(0)
    fun getErrorMessage(e: Exception): String {
        if (e is retrofit2.HttpException) {
            val body = e.response()?.errorBody()?.string()
            if (!body.isNullOrBlank()) return "HTTP ${e.code()}: $body"
        }
        return e.message ?: "Unknown error"
    }

    private var sharedPrefs: android.content.SharedPreferences? = null
    var customSupabaseUrl: String = ""
    var customSupabaseAnonKey: String = ""

    fun initialize(context: android.content.Context) {
        if (sharedPrefs == null) {
            sharedPrefs = context.getSharedPreferences("supabase_config", android.content.Context.MODE_PRIVATE)
            customSupabaseUrl = sharedPrefs?.getString("supabase_url", "") ?: ""
            customSupabaseAnonKey = sharedPrefs?.getString("supabase_anon_key", "") ?: ""
        }
    }

    fun saveConfig(context: android.content.Context, url: String, key: String) {
        initialize(context)
        customSupabaseUrl = url.trim()
        customSupabaseAnonKey = key.trim()
        sharedPrefs?.edit()?.apply {
            putString("supabase_url", customSupabaseUrl)
            putString("supabase_anon_key", customSupabaseAnonKey)
            apply()
        }
    }

    fun clearConfig(context: android.content.Context) {
        initialize(context)
        customSupabaseUrl = ""
        customSupabaseAnonKey = ""
        sharedPrefs?.edit()?.apply {
            remove("supabase_url")
            remove("supabase_anon_key")
            apply()
        }
    }

    // Retrieve environment variables compiled via Secrets Gradle Plugin or dynamic overrides
    val supabaseUrl: String
        get() = customSupabaseUrl.ifEmpty {
            try {
                BuildConfig.SUPABASE_URL.trim()
            } catch (e: Throwable) {
                ""
            }
        }

    val supabaseAnonKey: String
        get() = customSupabaseAnonKey.ifEmpty {
            try {
                BuildConfig.SUPABASE_ANON_KEY.trim()
            } catch (e: Throwable) {
                ""
            }
        }

    val imageKitPublicKey: String = try {
        BuildConfig.IMAGEKIT_PUBLIC_KEY.trim()
    } catch (e: Throwable) {
        ""
    }

    val imageKitPrivateKey: String = try {
        BuildConfig.IMAGEKIT_PRIVATE_KEY.trim()
    } catch (e: Throwable) {
        ""
    }

    val isSupabaseConfigured: Boolean
        get() {
            val url = supabaseUrl
            val key = supabaseAnonKey
            return url.isNotEmpty() && key.isNotEmpty() && !url.contains("YOUR_SUPABASE_URL_HERE")
        }

    val isImageKitConfigured: Boolean = imageKitPublicKey.isNotEmpty() && imageKitPrivateKey.isNotEmpty() && !imageKitPublicKey.contains("YOUR_IMAGEKIT_PUBLIC_KEY_HERE")

    val adTableKeys = java.util.concurrent.CopyOnWriteArraySet<String>()
    val productTableKeys = java.util.concurrent.CopyOnWriteArraySet<String>()

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private fun createOkHttpClient(apiKey: String): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("apikey", apiKey)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .addHeader("Content-Type", "application/json")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    private var cachedSupabaseApi: SupabaseApi? = null
    private var cachedUrlForApi: String = ""
    private var cachedKeyForApi: String = ""

    val supabaseApi: SupabaseApi?
        get() {
            if (!isSupabaseConfigured) return null
            val url = supabaseUrl
            val key = supabaseAnonKey
            if (cachedSupabaseApi != null && cachedUrlForApi == url && cachedKeyForApi == key) {
                return cachedSupabaseApi
            }
            try {
                val baseUrl = if (url.endsWith("/")) url else "$url/"
                val fullUrl = "${baseUrl}rest/v1/"
                val client = createOkHttpClient(key)
                cachedSupabaseApi = Retrofit.Builder()
                    .baseUrl(fullUrl)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .client(client)
                    .build()
                    .create(SupabaseApi::class.java)
                cachedUrlForApi = url
                cachedKeyForApi = key
                return cachedSupabaseApi
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SupabaseApi client: ", e)
                return null
            }
        }

    private var cachedSupabaseAuthApi: SupabaseAuthApi? = null
    private var cachedUrlForAuth: String = ""
    private var cachedKeyForAuth: String = ""

    val supabaseAuthApi: SupabaseAuthApi?
        get() {
            if (!isSupabaseConfigured) return null
            val url = supabaseUrl
            val key = supabaseAnonKey
            if (cachedSupabaseAuthApi != null && cachedUrlForAuth == url && cachedKeyForAuth == key) {
                return cachedSupabaseAuthApi
            }
            try {
                val baseUrl = if (url.endsWith("/")) url else "$url/"
                val fullUrl = "${baseUrl}auth/v1/"
                val client = createOkHttpClient(key)
                cachedSupabaseAuthApi = Retrofit.Builder()
                    .baseUrl(fullUrl)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .client(client)
                    .build()
                    .create(SupabaseAuthApi::class.java)
                cachedUrlForAuth = url
                cachedKeyForAuth = key
                return cachedSupabaseAuthApi
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SupabaseAuthApi client: ", e)
                return null
            }
        }

    // ImageKit Direct Upload Retrofit Client
    val imageKitApi: ImageKitApi? by lazy {
        try {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .build()

            Retrofit.Builder()
                .baseUrl("https://upload.imagekit.io/api/v1/")
                .addConverterFactory(MoshiConverterFactory.create(moshi))
                .client(client)
                .build()
                .create(ImageKitApi::class.java)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize ImageKit API client: ", e)
            null
        }
    }

    // Clean base64 auth token generator for raw private key
    fun getImageKitBasicAuthToken(): String {
        val credentials = "$imageKitPrivateKey:"
        val base64Token = Base64.encodeToString(credentials.toByteArray(), Base64.NO_WRAP)
        return "Basic $base64Token"
    }

    /**
     * Upload an image to ImageKit using ImageKit API.
     * Support binary multipart upload.
     */
    suspend fun uploadImageToImageKit(
        imageBytes: ByteArray,
        customFileName: String = "ad_image_${UUID.randomUUID()}.jpg"
    ): String? {
        if (!isImageKitConfigured) {
            Log.w(TAG, "ImageKit properties missing in .env")
            return null
        }

        val api = imageKitApi ?: return null

        return try {
            val authHeader = getImageKitBasicAuthToken()
            val fileRequestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
            val filePart = MultipartBody.Part.createFormData("file", customFileName, fileRequestBody)

            val fileNameBody = customFileName.toRequestBody("text/plain".toMediaTypeOrNull())
            val publicKeyBody = imageKitPublicKey.toRequestBody("text/plain".toMediaTypeOrNull())

            val response = api.uploadFile(authHeader, filePart, fileNameBody, publicKeyBody)
            if (response.isSuccessful && response.body() != null) {
                val url = response.body()?.url
                Log.i(TAG, "Successfully uploaded file to ImageKit: $url")
                url
            } else {
                Log.e(TAG, "ImageKit upload failed with code: ${response.code()} ${response.errorBody()?.string()}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception uploading image file to ImageKit", e)
            null
        }
    }

    private var cachedSupabaseStorageApi: SupabaseStorageApi? = null
    private var cachedUrlForStorage: String = ""
    private var cachedKeyForStorage: String = ""

    val supabaseStorageApi: SupabaseStorageApi?
        get() {
            if (!isSupabaseConfigured) return null
            val url = supabaseUrl
            val key = supabaseAnonKey
            if (cachedSupabaseStorageApi != null && cachedUrlForStorage == url && cachedKeyForStorage == key) {
                return cachedSupabaseStorageApi
            }
            try {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                val storageClient = OkHttpClient.Builder()
                    .addInterceptor(loggingInterceptor)
                    .build()

                val baseUrl = if (url.endsWith("/")) url else "$url/"
                val fullUrl = "${baseUrl}storage/v1/"
                cachedSupabaseStorageApi = Retrofit.Builder()
                    .baseUrl(fullUrl)
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                    .client(storageClient)
                    .build()
                    .create(SupabaseStorageApi::class.java)
                cachedUrlForStorage = url
                cachedKeyForStorage = key
                return cachedSupabaseStorageApi
            } catch (e: Exception) {
                Log.e(TAG, "Failed to initialize SupabaseStorageApi client: ", e)
                return null
            }
        }

    /**
     * Upload an image to Supabase Storage dynamically using existing SUPABASE_ANON_KEY.
     * Tries "ads" bucket first, then "products", then "images" bucket.
     */
    suspend fun uploadImageToSupabase(
        imageBytes: ByteArray,
        customFileName: String = "ad_image_${UUID.randomUUID()}.jpg"
    ): String? {
        if (!isSupabaseConfigured) return null
        val api = supabaseStorageApi ?: return null

        val requestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull(), 0, imageBytes.size)
        val authHeader = "Bearer $supabaseAnonKey"

        val buckets = listOf("ads", "products", "images")
        for (bucket in buckets) {
            try {
                Log.d(TAG, "Attempting to upload file to Supabase Storage bucket '$bucket'...")
                val response = api.uploadFile(authHeader, supabaseAnonKey, bucket, customFileName, requestBody)
                if (response.isSuccessful) {
                    val cleanBaseUrl = supabaseUrl.removeSuffix("/")
                    val finalUrl = "$cleanBaseUrl/storage/v1/object/public/$bucket/$customFileName"
                    Log.i(TAG, "Successfully uploaded file to Supabase Storage bucket '$bucket': $finalUrl")
                    return finalUrl
                } else {
                    Log.w(TAG, "Failed to upload to Supabase bucket '$bucket', status: ${response.code()} ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception uploading image to Supabase bucket '$bucket'", e)
            }
        }
        return null
    }

    suspend fun signUpWithEmail(email: String, password: String, name: String, city: String): SupabaseAuthResponse {
        val api = supabaseAuthApi ?: throw Exception("Supabase authentication is currently unconfigured or unavailable.")
        val response = api.signUp(SupabaseSignUpRequest(email, password, BaseUserMetadata(name, city)))
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response from authentication server.")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown sign up error"
            Log.e(TAG, "Sign up failed: $errorBody")
            throw Exception(parseAuthError(errorBody))
        }
    }

    suspend fun signInWithEmail(email: String, password: String): SupabaseAuthResponse {
        val api = supabaseAuthApi ?: throw Exception("Supabase authentication is currently unconfigured or unavailable.")
        val response = api.signIn(SupabaseSignInRequest(email, password))
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response from authentication server.")
        } else {
            val errorBody = response.errorBody()?.string() ?: "Unknown sign in error"
            Log.e(TAG, "Sign in failed: $errorBody")
            throw Exception(parseAuthError(errorBody))
        }
    }

    private fun parseAuthError(errorJson: String): String {
        return try {
            val jsonObject = org.json.JSONObject(errorJson)
            val desc = jsonObject.optString("error_description")
            if (desc.isNotEmpty()) return desc
            val msg = jsonObject.optString("msg")
            if (msg.isNotEmpty()) return msg
            val error = jsonObject.optString("error")
            if (error.isNotEmpty()) return error
            "Authentication failed"
        } catch (e: Exception) {
            "Authentication failed. Please check your credentials."
        }
    }

    fun mapMapToProduct(map: Map<String, Any?>): Product {
        var id = ""
        val idKeys = listOf("id", "product_id", "ad_id", "item_id", "uid", "productid", "adid", "itemid")
        for (k in map.keys) {
            if (idKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    id = v
                    break
                }
            }
        }
        if (id.isBlank()) {
            id = java.util.UUID.randomUUID().toString()
        }

        var title = ""
        val titleKeys = listOf("title", "name", "product_name", "productname", "item_name", "itemname", "ad_title", "adtitle")
        for (k in map.keys) {
            if (titleKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    title = v
                    break
                }
            }
        }
        if (title.isBlank()) title = "Untitled Product"

        var price = ""
        val priceKeys = listOf("price", "cost", "rate", "amount", "price_etb", "price_birr", "priceetb", "pricebirr")
        for (k in map.keys) {
            if (priceKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    price = v
                    break
                }
            }
        }
        if (price.isBlank()) price = "Negotiable"

        var imageUrl = ""
        val imgKeys = listOf("image_url", "imageUrl", "image", "img", "photo", "imageurl", "img_url", "imgurl", "picture", "image_link", "imagelink", "photourl", "photo_url")
        for (k in map.keys) {
            if (imgKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true) && !v.equals("undefined", ignoreCase = true)) {
                    imageUrl = v
                    break
                }
            }
        }

        // Resilient fuzzy search for image-related columns if no exact match found
        if (imageUrl.isBlank()) {
            for (k in map.keys) {
                val kLower = k.lowercase()
                if (kLower.contains("image") || kLower.contains("img") || kLower.contains("photo") || 
                    kLower.contains("pic") || kLower.contains("thumb") || kLower.contains("url") || 
                    kLower.contains("link") || kLower.contains("file")
                ) {
                    val v = map[k]?.toString()
                    if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true) && !v.equals("undefined", ignoreCase = true)) {
                        imageUrl = v
                        android.util.Log.i("SupabaseService", "Dynamically matched image column '$k' with value: $imageUrl")
                        break
                    }
                }
            }
        }

        // Resilient dynamic value scanning for any URL or image extension as a final fallback
        if (imageUrl.isBlank()) {
            val skipKeys = listOf("id", "title", "name", "price", "description", "details", "desc", "location", "city", "condition", "status", "seller_name", "sellerName", "seller", "created_at", "createdAt")
            for ((k, v) in map) {
                if (v != null && !skipKeys.any { it.equals(k, ignoreCase = true) }) {
                    val strVal = v.toString().trim()
                    if (strVal.startsWith("http://") || strVal.startsWith("https://") ||
                        strVal.endsWith(".jpg", ignoreCase = true) || strVal.endsWith(".jpeg", ignoreCase = true) ||
                        strVal.endsWith(".png", ignoreCase = true) || strVal.endsWith(".webp", ignoreCase = true) ||
                        strVal.contains("/storage/") || strVal.contains("/images/")
                    ) {
                        imageUrl = strVal
                        android.util.Log.i("SupabaseService", "Parsed image URL from fuzzy raw scanning on column '$k': $imageUrl")
                        break
                    }
                }
            }
        }

        var location = ""
        val locationKeys = listOf("location", "city", "area", "neighborhood", "address", "town", "region")
        for (k in map.keys) {
            if (locationKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    location = v
                    break
                }
            }
        }
        if (location.isBlank()) location = "Addis Ababa"

        var condition = ""
        val conditionKeys = listOf("condition", "status", "state", "quality", "cond", "type")
        for (k in map.keys) {
            if (conditionKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    condition = v
                    break
                }
            }
        }
        if (condition.isBlank()) condition = "New"

        var description = ""
        val descriptionKeys = listOf("description", "details", "desc", "about", "info", "body", "content")
        for (k in map.keys) {
            if (descriptionKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    description = v
                    break
                }
            }
        }
        if (description.isBlank()) description = ""

        var rawCategoryId = ""
        val catKeys = listOf("category_id", "categoryId", "category", "categoryid", "cat_id", "catid", "class", "group", "type_id", "type")
        for (k in map.keys) {
            if (catKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    rawCategoryId = v
                    break
                }
            }
        }
        if (rawCategoryId.contains(".")) {
            rawCategoryId = rawCategoryId.substringBefore(".")
        }

        val catLower = rawCategoryId.lowercase().trim()
        var categoryId = when {
            catLower == "1" || catLower.contains("vehicle") || catLower.contains("car") || catLower.contains("motorcycle") || catLower.contains("truck") || catLower.contains("mote") -> "1"
            catLower == "2" || catLower.contains("phone") || catLower.contains("mob") || catLower.contains("tablet") || catLower.contains("smartwatch") -> "2"
            catLower == "3" || catLower.contains("property") || catLower.contains("home") || catLower.contains("rent") || catLower.contains("real") || catLower.contains("house") || catLower.contains("flat") || catLower.contains("land") -> "3"
            catLower == "4" || catLower.contains("fashion") || catLower.contains("wear") || catLower.contains("cloth") || catLower.contains("shoe") || catLower.contains("apparel") || catLower.contains("bag") || catLower.contains("watch") || catLower.contains("jewelry") -> "4"
            catLower == "5" || catLower.contains("furniture") || catLower.contains("chair") || catLower.contains("table") || catLower.contains("desk") || catLower.contains("sofa") || catLower.contains("appliance") || catLower.contains("bed") -> "5"
            catLower == "6" || catLower.contains("electronic") || catLower.contains("tv") || catLower.contains("television") || catLower.contains("laptop") || catLower.contains("computer") || catLower.contains("camera") || catLower.contains("sound") -> "6"
            catLower == "7" || catLower.contains("health") || catLower.contains("beauty") || catLower.contains("cosmetic") || catLower.contains("perfume") || catLower.contains("care") || catLower.contains("makeup") -> "7"
            catLower == "8" || catLower.contains("service") || catLower.contains("repair") || catLower.contains("cleaning") || catLower.contains("transport") -> "8"
            catLower == "9" || catLower.contains("job") || catLower.contains("hiring") || catLower.contains("work") || catLower.contains("vacancy") -> "9"
            catLower == "10" || catLower.contains("pet") || catLower.contains("animal") || catLower.contains("dog") || catLower.contains("cat") -> "10"
            catLower == "11" || catLower.contains("food") || catLower.contains("agriculture") || catLower.contains("farm") || catLower.contains("crop") || catLower.contains("seed") -> "11"
            else -> ""
        }

        if (categoryId.isEmpty() || categoryId.toIntOrNull() == null || categoryId.toInt() < 1 || categoryId.toInt() > 11) {
            categoryId = CategoryMapper.mapTitleToCategoryId(title, description)
            android.util.Log.i("SupabaseService", "Auto-classified product '$title' with raw category '$rawCategoryId' into valid category ID: $categoryId")
        }

        var sellerName = ""
        val sellerKeys = listOf("seller_name", "sellerName", "seller", "sellername", "user_name", "username")
        for (k in map.keys) {
            if (sellerKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    sellerName = v
                    break
                }
            }
        }
        if (sellerName.isBlank()) sellerName = "Verified Seller"

        var isPromoted = false
        val promoKeys = listOf("is_promoted", "isPromoted", "promoted", "ispromoted")
        for (k in map.keys) {
            if (promoKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]
                if (v is Boolean) {
                    isPromoted = v
                    break
                } else if (v != null) {
                    isPromoted = v.toString().toBoolean()
                    break
                }
            }
        }

        var timeAgo = ""
        val timeKeys = listOf("time_ago", "timeAgo", "created_at", "createdAt", "timeago", "time")
        for (k in map.keys) {
            if (timeKeys.any { it.equals(k, ignoreCase = true) }) {
                val v = map[k]?.toString()
                if (!v.isNullOrBlank() && !v.equals("null", ignoreCase = true)) {
                    if (v.contains("-") && v.contains("T")) {
                        timeAgo = "Just now"
                    } else {
                        timeAgo = v
                    }
                    break
                }
            }
        }
        if (timeAgo.isBlank()) timeAgo = "Just now"

        var finalImageUrl = imageUrl.trim()
        if (finalImageUrl.isNotEmpty() && 
            !finalImageUrl.startsWith("http://") && 
            !finalImageUrl.startsWith("https://") && 
            !finalImageUrl.startsWith("data:image") &&
            !finalImageUrl.startsWith("file:/") &&
            !finalImageUrl.startsWith("content:/")
        ) {
            val cleanSupabaseUrl = supabaseUrl.trim().removeSuffix("/")
            if (cleanSupabaseUrl.isNotEmpty()) {
                finalImageUrl = if (finalImageUrl.contains("/")) {
                    if (finalImageUrl.startsWith("public/")) {
                        "$cleanSupabaseUrl/storage/v1/object/$finalImageUrl"
                    } else {
                        "$cleanSupabaseUrl/storage/v1/object/public/$finalImageUrl"
                    }
                } else {
                    "$cleanSupabaseUrl/storage/v1/object/public/ads/$finalImageUrl"
                }
            }
        }

        return Product(
            id = id,
            title = title,
            price = price,
            imageUrl = finalImageUrl,
            location = location,
            condition = condition,
            description = description,
            categoryId = categoryId,
            sellerName = sellerName,
            isPromoted = isPromoted,
            timeAgo = timeAgo
        )
    }

    fun buildInsertPayload(prod: Product, keysSet: Set<String>): Map<String, Any> {
        val payload = mutableMapOf<String, Any>()

        // 1. Title/Name mapping
        val titleKeys = listOf("title", "name", "product_name", "productname", "item_name", "itemname", "ad_title", "adtitle")
        val finalTitleKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> titleKeys.any { it.equals(k, ignoreCase = true) } } ?: "title"
        } else "title"
        payload[finalTitleKey] = prod.title

        // 2. Price mapping
        val priceKeys = listOf("price", "cost", "rate", "amount", "price_etb", "price_birr", "priceetb", "pricebirr")
        val finalPriceKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> priceKeys.any { it.equals(k, ignoreCase = true) } } ?: "price"
        } else "price"
        payload[finalPriceKey] = prod.price

        // 3. Location mapping
        val locationKeys = listOf("location", "city", "area", "neighborhood", "address", "town", "region")
        val finalLocationKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> locationKeys.any { it.equals(k, ignoreCase = true) } } ?: "location"
        } else "location"
        payload[finalLocationKey] = prod.location

        // 4. Condition mapping
        val conditionKeys = listOf("condition", "status", "state", "quality", "cond", "type")
        val finalConditionKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> conditionKeys.any { it.equals(k, ignoreCase = true) } } ?: "condition"
        } else "condition"
        payload[finalConditionKey] = prod.condition

        // 5. Description mapping
        val descriptionKeys = listOf("description", "details", "desc", "about", "info", "body", "content")
        val finalDescriptionKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> descriptionKeys.any { it.equals(k, ignoreCase = true) } } ?: "description"
        } else "description"
        payload[finalDescriptionKey] = prod.description

        // 6. ID mapping
        val idKeys = listOf("id", "product_id", "ad_id", "item_id", "uid", "productid", "adid", "itemid")
        val finalIdKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> idKeys.any { it.equals(k, ignoreCase = true) } } ?: "id"
        } else "id"
        payload[finalIdKey] = prod.id

        // 7. Image mapping
        val imgKeys = listOf("image_url", "imageUrl", "image", "img", "photo", "imageurl", "img_url", "imgurl", "picture", "image_link", "imagelink", "photourl", "photo_url")
        val finalImgKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> imgKeys.any { it.equals(k, ignoreCase = true) } } ?: "image_url"
        } else "image_url"
        payload[finalImgKey] = prod.imageUrl

        // 8. Category mapping
        val catKeys = listOf("category_id", "categoryId", "category", "categoryid", "cat_id", "catid", "class", "group", "type_id", "type")
        val finalCatKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> catKeys.any { it.equals(k, ignoreCase = true) } } ?: "category_id"
        } else "category_id"
        payload[finalCatKey] = prod.categoryId

        // 9. Seller Name mapping
        val sellerKeys = listOf("seller_name", "sellerName", "seller", "sellername", "owner", "user", "username", "user_name")
        val finalSellerKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> sellerKeys.any { it.equals(k, ignoreCase = true) } } ?: "seller_name"
        } else "seller_name"
        payload[finalSellerKey] = prod.sellerName

        // 10. Promoted mapping
        val promoKeys = listOf("is_promoted", "isPromoted", "promoted", "promo", "featured", "is_featured", "isfeatured")
        val finalPromoKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> promoKeys.any { it.equals(k, ignoreCase = true) } } ?: "is_promoted"
        } else "is_promoted"
        payload[finalPromoKey] = prod.isPromoted

        // 11. Time Ago mapping
        val timeKeys = listOf("time_ago", "timeAgo", "time", "date", "created_at", "createdat", "timestamp", "posted")
        val finalTimeKey = if (keysSet.isNotEmpty()) {
            keysSet.firstOrNull { k -> timeKeys.any { it.equals(k, ignoreCase = true) } } ?: "time_ago"
        } else "time_ago"
        payload[finalTimeKey] = prod.timeAgo

        if (keysSet.isNotEmpty()) {
            return payload.filter { keysSet.contains(it.key) }
        }
        return payload
    }

    fun getFallbackImageUrl(categoryId: String, title: String): String {
        val titleLower = title.lowercase()
        val catLower = categoryId.lowercase().trim()
        return when {
            catLower == "1" || catLower.contains("vehicle") || catLower.contains("car") || catLower.contains("motorcycle") || catLower.contains("truck") || catLower.contains("mote") ||
                    titleLower.contains("car") || titleLower.contains("toyota") || titleLower.contains("camry") || titleLower.contains("suzuki") || titleLower.contains("hyundai") || titleLower.contains("honda") || titleLower.contains("ford") || titleLower.contains("motor") || titleLower.contains("truck") || titleLower.contains("vehicle") || titleLower.contains("vitz") || titleLower.contains("corolla") || titleLower.contains("yaris") -> {
                "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fd?auto=format&fit=crop&w=600&q=80" // Vehicles
            }
            catLower == "2" || catLower.contains("phone") || catLower.contains("tech") || catLower.contains("electronic") || catLower.contains("smartwatch") || catLower.contains("tablet") || catLower.contains("laptop") || catLower.contains("computer") ||
                    titleLower.contains("phone") || titleLower.contains("iphone") || titleLower.contains("samsung") || titleLower.contains("pixel") || titleLower.contains("galaxy") || titleLower.contains("laptop") || titleLower.contains("computer") || titleLower.contains("playstation") || titleLower.contains("ps5") || titleLower.contains("tv") || titleLower.contains("ipad") || titleLower.contains("macbook") || titleLower.contains("hp") || titleLower.contains("dell") || titleLower.contains("pro max") || titleLower.contains("redmi") || titleLower.contains("infinix") || titleLower.contains("tecno") -> {
                "https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&w=600&q=80" // Phones / Electronics
            }
            catLower == "3" || catLower.contains("property") || catLower.contains("home") || catLower.contains("rent") || catLower.contains("real") || catLower.contains("house") || catLower.contains("flat") || catLower.contains("land") ||
                    titleLower.contains("house") || titleLower.contains("flat") || titleLower.contains("rent") || titleLower.contains("apartment") || titleLower.contains("condo") || titleLower.contains("property") || titleLower.contains("bole") || titleLower.contains("villa") || titleLower.contains("studio") -> {
                "https://images.unsplash.com/photo-1554995207-c18c203602cb?auto=format&fit=crop&w=600&q=80" // Property
            }
            catLower == "4" || catLower.contains("fashion") || catLower.contains("wear") || catLower.contains("cloth") || catLower.contains("shoe") || catLower.contains("apparel") || catLower.contains("bag") || catLower.contains("watch") || catLower.contains("jewelry") ||
                    titleLower.contains("shirt") || titleLower.contains("dress") || titleLower.contains("sneaker") || titleLower.contains("shoe") || titleLower.contains("cloth") || titleLower.contains("pant") || titleLower.contains("jacket") || titleLower.contains("t-shirt") || titleLower.contains("boots") || titleLower.contains("shoes") || titleLower.contains("bag") || titleLower.contains("watch") || titleLower.contains("jewelry") || titleLower.contains("kicks") -> {
                "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=600&q=80" // Fashion / Sneakers
            }
            catLower == "5" || catLower.contains("furniture") || catLower.contains("chair") || catLower.contains("table") || catLower.contains("desk") || catLower.contains("sofa") || catLower.contains("appliance") || catLower.contains("bed") ||
                    titleLower.contains("sofa") || titleLower.contains("chair") || titleLower.contains("table") || titleLower.contains("desk") || titleLower.contains("furniture") || titleLower.contains("bed") || titleLower.contains("cabinet") || titleLower.contains("wardrobe") || titleLower.contains("couch") || titleLower.contains("dining") -> {
                "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?auto=format&fit=crop&w=600&q=80" // Furniture
            }
            catLower == "6" || catLower.contains("electronic") || catLower.contains("tv") || catLower.contains("television") || catLower.contains("laptop") || catLower.contains("computer") || catLower.contains("sound") ||
                    titleLower.contains("tv") || titleLower.contains("laptop") || titleLower.contains("macbook") || titleLower.contains("computer") || titleLower.contains("speaker") || titleLower.contains("camera") || titleLower.contains("monitor") -> {
                "https://images.unsplash.com/photo-1588508065123-287b28e013da?auto=format&fit=crop&w=600&q=80" // Electronics
            }
            catLower == "7" || catLower.contains("health") || catLower.contains("beauty") || catLower.contains("cosmetic") || catLower.contains("perfume") || catLower.contains("makeup") -> {
                "https://images.unsplash.com/photo-1522335789203-aabd1fc54bc9?auto=format&fit=crop&w=600&q=80" // Health & Beauty
            }
            catLower == "8" || catLower.contains("service") || catLower.contains("repair") || catLower.contains("cleaning") || catLower.contains("transport") -> {
                "https://images.unsplash.com/photo-1621905251189-08b45d6a269e?auto=format&fit=crop&w=600&q=80" // Services
            }
            catLower == "9" || catLower.contains("job") || catLower.contains("work") || catLower.contains("vacancy") -> {
                "https://images.unsplash.com/photo-1486406146926-c627a92ad1ab?auto=format&fit=crop&w=600&q=80" // Jobs
            }
            catLower == "10" || catLower.contains("pet") || catLower.contains("animal") || catLower.contains("dog") || catLower.contains("cat") -> {
                "https://images.unsplash.com/photo-1450778869180-41d0601e046e?auto=format&fit=crop&w=600&q=80" // Animals & Pets
            }
            catLower == "11" || catLower.contains("food") || catLower.contains("agriculture") || catLower.contains("farm") || catLower.contains("crop") -> {
                "https://images.unsplash.com/photo-1593113598332-cd288d649433?auto=format&fit=crop&w=600&q=80" // Agriculture & Food
            }
            else -> {
                val options = listOf(
                    "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fd?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1554995207-c18c203602cb?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1538688525198-9b88f6f53126?auto=format&fit=crop&w=600&q=80"
                )
                val index = Math.abs(title.hashCode()) % options.size
                options[index]
            }
        }
    }
}
