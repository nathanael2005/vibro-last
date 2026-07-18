package com.example
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue

import android.content.Context
import android.widget.Toast
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.launch

// --- Navigation Routes ---
const val SPLASH_ROUTE = "splash"
const val WELCOME_ROUTE = "welcome"
const val LOGIN_ROUTE = "login"
const val MAIN_SHELL_ROUTE = "main"
const val PRODUCT_DETAIL_ROUTE = "product/{productId}"
fun createProductDetailRoute(id: String) = "product/$id"

// --- Location Models ---
data class CityLocation(
    val name: String,
    val subLocations: List<String>
)

val ethiopianCities = listOf(
    CityLocation("Addis Ababa", listOf(
        "Bole", "Yeka", "Kirkos", "Lideta", "Arada", "Gullele", 
        "Kolfe Keranio", "Nifas Silk-Lafto", "Akaki Kality", "Lemi Kura"
    )),
    CityLocation("Hawassa", listOf(
        "Tabor", "Misrak", "Hayipha", "Millennium", "Lewi", "Amora Gedel", "Bahel Adarash", "Hawassa Lake"
    )),
    CityLocation("Adama", listOf(
        "Bole", "Kebele 01", "Geda", "Melka Adama", "Franco"
    )),
    CityLocation("Bahir Dar", listOf(
        "Kebele 3", "Kebele 4", "Bezawit", "Ginbot 20", "Shimbit", "Tana", "Abay"
    )),
    CityLocation("Dessie", listOf(
        "Kebele 1", "Kebele 2", "Segno Gebeya", "Hotie", "Menen", "Robit", "Arab Ganda"
    )),
    CityLocation("Jimma", listOf(
        "Ginjo", "Hermata", "Kochi", "Mendera", "Jiren", "Seto Semero"
    )),
    CityLocation("Mekelle", listOf(
        "Adi Haki", "Kedamay Weyane", "Ayder", "Hadnet", "Hawelti", "Semien"
    )),
    CityLocation("Gondar", listOf(
        "Arada", "Azezo", "Piazza", "Kebele 18", "Maraki", "Wolleka"
    )),
    CityLocation("Dire Dawa", listOf(
        "Kebele 01", "Kebele 02", "Sabian", "Depo", "Shinile"
    )),
    CityLocation("Harar", listOf(
        "Jegol", "Kebele 01", "Kebele 02", "Dakar", "Aboker"
    )),
    CityLocation("Jijiga", listOf(
        "Kebele 01", "Kebele 02", "Kebele 03"
    )),
    CityLocation("Arba Minch", listOf(
        "Sikela", "Secha", "Limat"
    )),
    CityLocation("Hosaena", listOf(
        "Kebele 01", "Kebele 02", "Lichamba"
    )),
    CityLocation("Dilla", listOf(
        "Kebele 01", "Kebele 02", "Haroresa"
    )),
    CityLocation("Nekemte", listOf(
        "Kebele 01", "Kebele 02"
    )),
    CityLocation("Debre Birhan", listOf(
        "Kebele 01", "Kebele 02", "Tebase"
    )),
    CityLocation("Debre Markos", listOf(
        "Kebele 01", "Kebele 02"
    )),
    CityLocation("Asella", listOf(
        "Kebele 01", "Kebele 02"
    )),
    CityLocation("Bishoftu", listOf(
        "Babogaya", "Bishoftu Lake", "Hora", "Kuriftu"
    )),
    CityLocation("Shashemene", listOf(
        "Kebele 01", "Kebele 02", "Rastafarian Quarter"
    )),
    CityLocation("Gambela", listOf(
        "Kebele 01", "Kebele 02"
    )),
    CityLocation("Semera", listOf(
        "Kebele 01", "Logia"
    ))
)

// --- Models ---
data class Product(
    val id: String,
    val title: String,
    val price: String,
    val imageUrl: String,
    val location: String,
    val condition: String,
    val description: String,
    val categoryId: String,
    val sellerName: String = "Verified Seller",
    val isPromoted: Boolean = false,
    val timeAgo: String = "2 hours ago",
    val createdAt: Long = System.currentTimeMillis()
)

fun getRelativeTime(product: Product): String {
    val diffMs = System.currentTimeMillis() - product.createdAt
    if (diffMs < 0) return product.timeAgo
    val diffSec = diffMs / 1000
    if (diffSec < 60) {
        return "Just now"
    }
    val diffMin = diffSec / 60
    if (diffMin < 60) {
        return "$diffMin ${if (diffMin == 1L) "min" else "mins"} ago"
    }
    val diffHours = diffMin / 60
    if (diffHours < 24) {
        return "$diffHours ${if (diffHours == 1L) "hour" else "hours"} ago"
    }
    val diffDays = diffHours / 24
    if (diffDays < 7) {
        return "$diffDays ${if (diffDays == 1L) "day" else "days"} ago"
    }
    return product.timeAgo
}

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

data class ChatMessage(
    val sender: String,
    val content: String,
    val time: String
)

data class Review(
    val sellerName: String,
    val reviewerName: String,
    val rating: Int,
    val comment: String,
    val timeAgo: String
)

data class ChatThread(
    val id: String,
    val senderName: String,
    val lastMessage: String,
    val time: String,
    val messages: List<ChatMessage>
)

// --- Preset Data ---
val mockCategories = listOf(
    Category("1", "Vehicles", Icons.Default.DirectionsCar, Color(0xFFFEF3C7), Color(0xFFD97706)),
    Category("2", "Phones & Tablets", Icons.Default.Smartphone, Color(0xFFDBEAFE), Color(0xFF2563EB)),
    Category("3", "Property", Icons.Default.Home, Color(0xFFF3E8FF), Color(0xFF9333EA)),
    Category("4", "Fashion", Icons.Default.Checkroom, Color(0xFFFFE4E6), Color(0xFFE11D48)),
    Category("5", "Furniture & Appliances", Icons.Default.Weekend, Color(0xFFD1FAE5), Color(0xFF059669)),
    Category("6", "Electronics", Icons.Default.Tv, Color(0xFFE0F2FE), Color(0xFF0369A1)),
    Category("7", "Health & Beauty", Icons.Default.Brush, Color(0xFFFCE7F3), Color(0xFFBE185D)),
    Category("8", "Services", Icons.Default.Build, Color(0xFFF1F5F9), Color(0xFF475569)),
    Category("9", "Jobs", Icons.Default.Work, Color(0xFFFEF2F2), Color(0xFFDC2626)),
    Category("10", "Animals & Pets", Icons.Default.Pets, Color(0xFFECFDF5), Color(0xFF047857)),
    Category("11", "Agriculture & Food", Icons.Default.Restaurant, Color(0xFFFFF7ED), Color(0xFFC2410C))
)

val mockProducts = mutableListOf(
    Product(
        id = "101",
        title = "Toyota Camry 2018 (Direct Owner)",
        price = "Br 2,500,000",
        imageUrl = "https://images.unsplash.com/photo-1621007947382-bb3c3994e3fd?auto=format&fit=crop&w=600&q=80",
        location = "Addis Ababa, Bole",
        condition = "Used - Good",
        description = "Chilling AC, perfect engine and gearbox. Direct custom cleared, single owner use. Just buy and drive.",
        categoryId = "1",
        sellerName = "Abebe Kebede",
        isPromoted = true,
        timeAgo = "2 hours ago",
        createdAt = System.currentTimeMillis() - 2 * 3600 * 1000
    ),
    Product(
        id = "102",
        title = "iPhone 13 Pro Max - 256GB Gold",
        price = "Br 85,000",
        imageUrl = "https://images.unsplash.com/photo-1632661674596-df8be070a5c5?auto=format&fit=crop&w=600&q=80",
        location = "Addis Ababa, Kazanchis",
        condition = "Used - Like New",
        description = "Battery health is 92%. Clean original imported, factory unlocked. Comes with box & original cable.",
        categoryId = "2",
        sellerName = "Selam Electronics",
        isPromoted = false,
        timeAgo = "10 mins ago",
        createdAt = System.currentTimeMillis() - 10 * 60 * 1000
    ),
    Product(
        id = "103",
        title = "Modern 3 Bedroom Flat in Bole",
        price = "Br 150,000 / mo",
        imageUrl = "https://images.unsplash.com/photo-1554995207-c18c203602cb?auto=format&fit=crop&w=600&q=80",
        location = "Addis Ababa, Bole Atlas",
        condition = "Newly Built",
        description = "Spacious Rooms all en-suite with modern tiles, fully painted, fenced compound, secure security.",
        categoryId = "3",
        sellerName = "Sheger Homes & Realty",
        isPromoted = true,
        timeAgo = "1 day ago",
        createdAt = System.currentTimeMillis() - 24 * 3600 * 1000
    ),
    Product(
        id = "104",
        title = "PlayStation 5 Standard Edition",
        price = "Br 55,000",
        imageUrl = "https://images.unsplash.com/photo-1606813907291-d86efa9b94db?auto=format&fit=crop&w=600&q=80",
        location = "Addis Ababa, Sarbet",
        condition = "New",
        description = "Sealed box standard disc edition. Comes with 1 DualSense wireless controller and 1 free game.",
        categoryId = "2",
        sellerName = "Yonas Tech Store",
        isPromoted = false,
        timeAgo = "3 hours ago",
        createdAt = System.currentTimeMillis() - 3 * 3600 * 1000
    ),
    Product(
        id = "105",
        title = "White Sneakers - Air Force 1",
        price = "Br 4,500",
        imageUrl = "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=600&q=80",
        location = "Hawassa, Piassa",
        condition = "New",
        description = "Super quality sneakers. Available in sizes 40 to 45. Durable sole and highly fashionable.",
        categoryId = "4",
        sellerName = "Habesha Kicks",
        isPromoted = false,
        timeAgo = "Yesterday",
        createdAt = System.currentTimeMillis() - 24 * 3600 * 1000
    ),
    Product(
        id = "106",
        title = "HP Envy 13 Laptop Intel i7",
        price = "Br 62,000",
        imageUrl = "https://images.unsplash.com/photo-1531297172868-ed80b5c1c8ea?auto=format&fit=crop&w=600&q=80",
        location = "Addis Ababa, 4 Kilo",
        condition = "Used - Good",
        description = "16GB RAM, 512GB SSD. Backlit keyboard, fingerprint scanner. Chilled battery backup of 6 hours.",
        categoryId = "2",
        sellerName = "Elias Laptop Shop",
        isPromoted = false,
        timeAgo = "2 days ago",
        createdAt = System.currentTimeMillis() - 2 * 24 * 3600 * 1000
    )
)

@Composable
fun MarketplaceApp() {
    val navController = rememberNavController()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    remember {
        NetworkManager.initialize(context.applicationContext)
        val loaded = ProductPersistence.loadProducts(context)
        if (loaded != null && loaded.isNotEmpty()) {
            mockProducts.clear()
            mockProducts.addAll(loaded)
        }
        true
    }
    val sharedPrefs = remember { context.getSharedPreferences("vibro_prefs", Context.MODE_PRIVATE) }

    val currentVersion = remember(context) {
        try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0"
        } catch (e: Exception) {
            "1.0"
        }
    }

    var updateInfo by remember { mutableStateOf<GitHubUpdateManager.UpdateInfo?>(null) }
    var showUpdateDialog by remember { mutableStateOf(false) }
    var downloadProgress by remember { mutableStateOf(0f) }
    var isDownloading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Automatically check for updates on launch
        val info = GitHubUpdateManager.checkForUpdates(currentVersion)
        if (info != null && info.hasUpdate) {
            updateInfo = info
            showUpdateDialog = true
        }
    }

    val onManualCheckForUpdates: () -> Unit = {
        Toast.makeText(context, "Checking for updates...", Toast.LENGTH_SHORT).show()
        coroutineScope.launch {
            val info = GitHubUpdateManager.checkForUpdates(currentVersion)
            if (info != null) {
                if (info.hasUpdate) {
                    updateInfo = info
                    showUpdateDialog = true
                } else {
                    Toast.makeText(context, "Vibro is up-to-date! (Version: $currentVersion)", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(context, "Failed to connect to update server. Please check your network.", Toast.LENGTH_LONG).show()
            }
        }
    }

    var isLoggedIn by remember {
        mutableStateOf(sharedPrefs.getBoolean("is_logged_in", false))
    }
    var userName by remember {
        mutableStateOf(sharedPrefs.getString("user_name", "Guest User") ?: "Guest User")
    }
    var userEmail by remember {
        mutableStateOf(sharedPrefs.getString("user_email", "guest@example.com") ?: "guest@example.com")
    }
    var userCity by remember {
        mutableStateOf(sharedPrefs.getString("user_city", "Addis Ababa, Ethiopia") ?: "Addis Ababa, Ethiopia")
    }

    val onSignIn: (String, String, String, String) -> Unit = { email, password, name, city ->
        sharedPrefs.edit()
            .putBoolean("is_logged_in", true)
            .putString("user_email", email)
            .putString("user_name", name.ifEmpty { email.substringBefore("@") })
            .putString("user_city", city.ifEmpty { "Addis Ababa, Ethiopia" })
            .apply()
        isLoggedIn = true
        userEmail = email
        userName = name.ifEmpty { email.substringBefore("@") }
        userCity = city.ifEmpty { "Addis Ababa, Ethiopia" }
    }

    val onSignOut: () -> Unit = {
        sharedPrefs.edit()
            .putBoolean("is_logged_in", false)
            .putString("user_email", "guest@example.com")
            .putString("user_name", "Guest User")
            .putString("user_city", "Addis Ababa, Ethiopia")
            .apply()
        isLoggedIn = false
        userEmail = "guest@example.com"
        userName = "Guest User"
        userCity = "Addis Ababa, Ethiopia"
    }

    // --- Global Shared States ---
    val sellerReviews = remember {
        mutableStateListOf<Review>(
            Review("Abebe Kebede", "Abebe K.", 5, "Fast communication and original product. Secure transaction around Bole, highly recommended!", "2 days ago"),
            Review("Abebe Kebede", "Marta Y.", 4, "Reliable seller, but was slightly late to our meeting point. The Camry is very clean though.", "5 days ago"),
            Review("Selam Electronics", "Selamawit T.", 5, "Trustworthy seller. The iPhone specs were exactly as stated in the ad. Clean transaction!", "1 week ago"),
            Review("Selam Electronics", "Elias J.", 5, "Genuine Apple products. The gold color is gorgeous.", "2 weeks ago"),
            Review("Sheger Homes & Realty", "Mariam W.", 5, "Bought a wood table and frame sofa. Durable joinery, exactly as promised.", "5 days ago"),
            Review("Sheger Homes & Realty", "Tariku B.", 4, "Spacious flat, caretakers were very cooperative. Good experience.", "2 weeks ago"),
            Review("Yonas Tech Store", "Yonas M.", 4, "Fair prices and very polite. Slight delay in meeting up, but overall very active and reliable.", "3 days ago"),
            Review("Yonas Tech Store", "Kidus H.", 5, "Got my PS5 from them. Sealed, brand new, works perfectly!", "1 week ago"),
            Review("Habesha Kicks", "Dawit L.", 5, "Amazing sneakers, authentic quality. Best shop in Hawassa!", "1 day ago"),
            Review("Habesha Kicks", "Betty K.", 5, "Very stylish and comfortable shoes. Recommended!", "3 days ago"),
            Review("Elias Laptop Shop", "Sami G.", 4, "Laptop runs well, battery life is decent. Polite seller.", "4 days ago"),
            Review("Elias Laptop Shop", "Hana D.", 4, "Good experience, laptop came clean with all original accessories.", "1 week ago"),
            Review("You", "Abebe K.", 5, "Prompt responses. Transaction went smoothly and they paid instantly.", "2 days ago"),
            Review("You", "Selamawit T.", 5, "Excellent buyer! Very clear communication.", "1 week ago")
        )
    }
    val products = remember {
        mutableStateListOf<Product>().apply {
            val loaded = ProductPersistence.loadProducts(context)
            if (loaded != null && loaded.isNotEmpty()) {
                mockProducts.clear()
                mockProducts.addAll(loaded)
            }
            addAll(mockProducts)
        }
    }
    val savedProductIds = remember { mutableStateListOf<String>().apply { add("101"); add("103") } }
    val chats = remember {
        mutableStateListOf(
            ChatThread("1", "Abebe Kebede", "Is the price negotiable?", "2:30 PM", listOf(
                ChatMessage("Abebe Kebede", "Hello!", "12:00 PM"),
                ChatMessage("You", "Hi, is the Camry still available?", "12:05 PM"),
                ChatMessage("Abebe Kebede", "Yes, it is still available. Very clean buy.", "12:10 PM"),
                ChatMessage("You", "Awesome. Is the price negotiable?", "12:15 PM")
            )),
            ChatThread("2", "Selam Electronics", "Yes, 256GB is available in gold.", "10:15 AM", listOf(
                ChatMessage("Selam Electronics", "We have iPhone 13 Pro Max in gold.", "10:00 AM"),
                ChatMessage("You", "Great, do you have 256GB?", "10:10 AM"),
                ChatMessage("Selam Electronics", "Yes, 256GB is available in gold.", "10:15 AM")
            )),
            ChatThread("3", "Sheger Homes & Realty", "The caretaker can show you around.", "Yesterday", listOf(
                ChatMessage("You", "Hello, when can I inspect the flat?", "Yesterday"),
                ChatMessage("Sheger Homes & Realty", "The caretaker can show you around tomorrow morning.", "Yesterday")
            ))
        )
    }

    var refreshTrigger by remember { mutableStateOf(0) }

    // --- Dynamic Sync from Supabase ---
    LaunchedEffect(refreshTrigger) {
        while (true) {
            if (NetworkManager.isSupabaseConfigured) {
                try {
                    // Fetch Products and Ads from Supabase with high resilience
                    var localSyncError: String? = null
                    val remoteAds = try {
                        android.util.Log.d("MarketplaceApp", "Attempting to fetch from 'ads' table...")
                        val ads = NetworkManager.supabaseApi?.getAds()
                        if (ads != null && ads.isNotEmpty()) {
                            NetworkManager.adTableKeys.clear()
                            NetworkManager.adTableKeys.addAll(ads[0].keys)
                            android.util.Log.i("MarketplaceApp", "Captured 'ads' table schema keys: ${NetworkManager.adTableKeys}")
                        }
                        ads
                    } catch (e: Exception) {
                        localSyncError = "Ads fetch failed: ${NetworkManager.getErrorMessage(e)}"
                        android.util.Log.w("MarketplaceApp", "Failed to fetch from 'ads' table: ${NetworkManager.getErrorMessage(e)}")
                        null
                    }

                    val remoteProducts = try {
                        android.util.Log.d("MarketplaceApp", "Attempting to fetch from 'products' table...")
                        val prods = NetworkManager.supabaseApi?.getProducts()
                        if (prods != null && prods.isNotEmpty()) {
                            NetworkManager.productTableKeys.clear()
                            NetworkManager.productTableKeys.addAll(prods[0].keys)
                            android.util.Log.i("MarketplaceApp", "Captured 'products' table schema keys: ${NetworkManager.productTableKeys}")
                        }
                        prods
                    } catch (e: Exception) {
                        if (localSyncError == null) localSyncError = "Products fetch failed: ${NetworkManager.getErrorMessage(e)}"
                        android.util.Log.w("MarketplaceApp", "Failed to fetch from 'products' table: ${NetworkManager.getErrorMessage(e)}")
                        null
                    }
                    
                    if (localSyncError == null && remoteAds != null && remoteProducts != null) {
                        NetworkManager.syncErrorMessage = null
                    } else if (localSyncError != null) {
                        NetworkManager.syncErrorMessage = localSyncError
                    }

                    val allRemoteMaps = mutableListOf<Map<String, Any>>()
                    if (remoteAds != null) {
                        allRemoteMaps.addAll(remoteAds)
                    }
                    if (remoteProducts != null) {
                        allRemoteMaps.addAll(remoteProducts)
                    }

                    NetworkManager.syncItemCount = allRemoteMaps.size
                    
                    val remoteMapped = allRemoteMaps.map { map ->
                        val p = NetworkManager.mapMapToProduct(map)
                        val finalImage = if (p.imageUrl.isBlank() || p.imageUrl.equals("null", ignoreCase = true)) {
                            NetworkManager.getFallbackImageUrl(p.categoryId, p.title)
                        } else {
                            p.imageUrl
                        }
                        p.copy(imageUrl = finalImage)
                    }.distinctBy { it.id }

                    // Keep unique mock products to preserve "remaining ads"
                    val remoteIds = remoteMapped.map { it.id }.toSet()
                    val remainingMock = mockProducts.filter { it.id !in remoteIds }

                    products.clear()
                    products.addAll(remoteMapped)
                    products.addAll(remainingMock)
                    android.util.Log.i("MarketplaceApp", "Successfully synced products from Supabase 'ads' and 'products' tables (${remoteMapped.size} total items synced, merged with remaining mock ads)")

                } catch (e: Exception) {
                    android.util.Log.w("MarketplaceApp", "Could not fetch products from Supabase Rest endpoint (table might not exist yet). Fallback to mock: ${NetworkManager.getErrorMessage(e)}")
                }

                try {
                    // Fetch Threads and Messages
                    val remoteThreads = NetworkManager.supabaseApi?.getChatThreads()
                    val remoteMessages = NetworkManager.supabaseApi?.getChatMessages()
                    if (remoteThreads != null && remoteThreads.isNotEmpty()) {
                        chats.clear()
                        chats.addAll(remoteThreads.map { t ->
                            val messagesList = remoteMessages?.filter { it.threadId == t.id }?.map { m ->
                                ChatMessage(m.sender, m.content, m.time)
                            } ?: emptyList()
                            ChatThread(
                                id = t.id,
                                senderName = t.senderName,
                                lastMessage = t.lastMessage,
                                time = t.time,
                                messages = messagesList.ifEmpty {
                                    listOf(ChatMessage("System", "Thread started", "Just now"))
                                }
                            )
                        })
                        android.util.Log.i("MarketplaceApp", "Successfully synced chat threads from Supabase")
                    }
                } catch (e: Exception) {
                    android.util.Log.w("MarketplaceApp", "Could not fetch chats from Supabase: ${NetworkManager.getErrorMessage(e)}")
                }
            }
            kotlinx.coroutines.delay(10000) // Poll and sync every 10 seconds for live updates
        }
    }

    NavHost(
        navController = navController,
        startDestination = SPLASH_ROUTE,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() },
        popEnterTransition = { fadeIn() },
        popExitTransition = { fadeOut() }
    ) {
        composable(route = SPLASH_ROUTE) {
            SplashScreen(
                onNavigationNext = {
                    val isFirstTime = sharedPrefs.getBoolean("is_first_time", true)
                    if (isFirstTime) {
                        navController.navigate(WELCOME_ROUTE) {
                            popUpTo(SPLASH_ROUTE) { inclusive = true }
                        }
                    } else {
                        navController.navigate(MAIN_SHELL_ROUTE) {
                            popUpTo(SPLASH_ROUTE) { inclusive = true }
                        }
                    }
                }
            )
        }

        composable(route = WELCOME_ROUTE) {
            WelcomeOnboardingScreen(
                onGetStarted = {
                    sharedPrefs.edit().putBoolean("is_first_time", false).apply()
                    navController.navigate(LOGIN_ROUTE) {
                        popUpTo(WELCOME_ROUTE) { inclusive = true }
                    }
                },
                onSkip = {
                    sharedPrefs.edit().putBoolean("is_first_time", false).apply()
                    navController.navigate(MAIN_SHELL_ROUTE) {
                        popUpTo(WELCOME_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(route = LOGIN_ROUTE) {
            LoginScreen(
                onLoginSuccess = { email, password, name, city ->
                    onSignIn(email, password, name, city)
                    navController.navigate(MAIN_SHELL_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
                    }
                },
                onSkip = {
                    navController.navigate(MAIN_SHELL_ROUTE) {
                        popUpTo(LOGIN_ROUTE) { inclusive = true }
                    }
                }
            )
        }

        composable(route = MAIN_SHELL_ROUTE) {
            MainContainerScreen(
                products = products,
                savedProductIds = savedProductIds,
                chats = chats,
                isLoggedIn = isLoggedIn,
                userName = userName,
                userEmail = userEmail,
                userCity = userCity,
                onSignInClick = {
                    navController.navigate(LOGIN_ROUTE)
                },
                onSignOutClick = {
                    onSignOut()
                    Toast.makeText(context, "Logged out successfully!", Toast.LENGTH_SHORT).show()
                },
                onUpdateProfile = { name, city ->
                    sharedPrefs.edit().putString("user_name", name).putString("user_city", city).apply()
                    userName = name
                    userCity = city
                },
                onProductClick = { id ->
                    navController.navigate(createProductDetailRoute(id))
                },
                refreshTrigger = refreshTrigger,
                onRefreshFeed = { refreshTrigger++ },
                sellerReviews = sellerReviews,
                onAddReview = { reviewerName, rating, comment ->
                    sellerReviews.add(0, Review("You", reviewerName, rating, comment, "Just now"))
                },
                onManualCheckForUpdates = onManualCheckForUpdates
            )
        }

        composable(
            route = PRODUCT_DETAIL_ROUTE,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            val product = products.find { it.id == productId }

            if (product != null) {
                val isLiked = savedProductIds.contains(product.id)
                ProductDetailScreen(
                    product = product,
                    isLiked = isLiked,
                    isLoggedIn = isLoggedIn,
                    onBackClick = { navController.popBackStack() },
                    onFavoriteToggle = {
                        if (isLiked) {
                            savedProductIds.remove(product.id)
                        } else {
                            savedProductIds.add(product.id)
                        }
                    },
                    onStartChat = { messageText ->
                        if (!isLoggedIn) {
                            Toast.makeText(context, "Sign in to chat with sellers", Toast.LENGTH_LONG).show()
                            navController.navigate(LOGIN_ROUTE)
                        } else {
                            val existing = chats.find { it.senderName == product.sellerName }
                            if (existing == null) {
                                val newThreadId = (chats.size + 1).toString()
                                val newThread = ChatThread(
                                    id = newThreadId,
                                    senderName = product.sellerName,
                                    lastMessage = messageText,
                                    time = "Just now",
                                    messages = listOf(
                                        ChatMessage("You", messageText, "Just now")
                                    )
                                )
                                chats.add(0, newThread)

                                // Post to Supabase in background
                                coroutineScope.launch {
                                    if (NetworkManager.isSupabaseConfigured) {
                                        try {
                                            NetworkManager.supabaseApi?.insertChatThread(
                                                SupabaseChatThread(
                                                    id = newThreadId,
                                                    senderName = product.sellerName,
                                                    lastMessage = messageText,
                                                    time = "Just now"
                                                )
                                            )
                                            NetworkManager.supabaseApi?.insertChatMessage(
                                                SupabaseChatMessage(
                                                    threadId = newThreadId,
                                                    sender = "You",
                                                    content = messageText,
                                                    time = "Just now"
                                                )
                                            )
                                            android.util.Log.i("MarketplaceApp", "Sent initial chat and thread to Supabase")
                                        } catch (e: Exception) {
                                            android.util.Log.e("MarketplaceApp", "Failed to sync initial chat thread: ${NetworkManager.getErrorMessage(e)}")
                                        }
                                    }
                                }
                            } else {
                                val index = chats.indexOfFirst { it.id == existing.id }
                                if (index != -1) {
                                    val thread = chats[index]
                                    chats[index] = thread.copy(
                                        lastMessage = messageText,
                                        time = "Just now",
                                        messages = thread.messages + ChatMessage("You", messageText, "Just now")
                                    )

                                    // Sync message to Supabase
                                    coroutineScope.launch {
                                        if (NetworkManager.isSupabaseConfigured) {
                                            try {
                                                NetworkManager.supabaseApi?.insertChatMessage(
                                                    SupabaseChatMessage(
                                                        threadId = thread.id,
                                                        sender = "You",
                                                        content = messageText,
                                                        time = "Just now"
                                                    )
                                                )
                                                android.util.Log.i("MarketplaceApp", "Sent message to existing thread in Supabase")
                                            } catch (e: Exception) {
                                                android.util.Log.e("MarketplaceApp", "Failed to sync message: ${NetworkManager.getErrorMessage(e)}")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    },
                    products = products,
                    onProductClick = { id ->
                        navController.navigate(createProductDetailRoute(id))
                    },
                    sellerReviews = sellerReviews,
                    onAddReview = { reviewerName, rating, comment ->
                        sellerReviews.add(0, Review(product.sellerName, reviewerName, rating, comment, "Just now"))
                    },
                    onUpdateProduct = { updated ->
                        val globalIndex = products.indexOfFirst { it.id == updated.id }
                        if (globalIndex != -1) {
                            products[globalIndex] = updated
                        }
                        val mockIndex = mockProducts.indexOfFirst { it.id == updated.id }
                        if (mockIndex != -1) {
                            mockProducts[mockIndex] = updated
                        }
                        ProductPersistence.saveProducts(context, mockProducts)
                    },
                    onDeleteProduct = { toDelete ->
                        products.remove(toDelete)
                        mockProducts.remove(toDelete)
                        ProductPersistence.saveProducts(context, mockProducts)
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Product not found")
                }
            }
        }
    }

    if (showUpdateDialog && updateInfo != null) {
        UpdateDialog(
            updateInfo = updateInfo!!,
            isDownloading = isDownloading,
            downloadProgress = downloadProgress,
            onDismiss = {
                if (!isDownloading) {
                    showUpdateDialog = false
                }
            },
            onUpdateClick = {
                coroutineScope.launch {
                    isDownloading = true
                    val apkFile = GitHubUpdateManager.downloadApk(context, updateInfo!!.downloadUrl) { progress ->
                        downloadProgress = progress
                    }
                    isDownloading = false
                    if (apkFile != null) {
                        showUpdateDialog = false
                        GitHubUpdateManager.installApk(context, apkFile)
                    } else {
                        Toast.makeText(context, "Failed to download update APK.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }
}

// --- Main Container with bottom tab selection ---
@Composable
fun MainContainerScreen(
    products: MutableList<Product>,
    savedProductIds: MutableList<String>,
    chats: MutableList<ChatThread>,
    isLoggedIn: Boolean,
    userName: String,
    userEmail: String,
    userCity: String,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    onProductClick: (String) -> Unit,
    refreshTrigger: Int,
    onRefreshFeed: () -> Unit,
    sellerReviews: List<Review>,
    onAddReview: (reviewerName: String, rating: Int, comment: String) -> Unit,
    onManualCheckForUpdates: () -> Unit = {}
) {
    var activeTab by remember { mutableStateOf("home") }
    var showPostAdDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        bottomBar = {
            MarketplaceBottomNav(
                activeTab = activeTab,
                onTabSelected = { activeTab = it },
                onPostAdClick = {
                    if (isLoggedIn) {
                        showPostAdDialog = true
                    } else {
                        showLoginRequiredDialog = true
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (activeTab) {
                "home" -> HomeScreenContent(
                    products = products,
                    savedProductIds = savedProductIds,
                    onProductClick = onProductClick,
                    onRefreshFeed = onRefreshFeed
                )
                "saved" -> SavedScreenContent(
                    products = products,
                    savedProductIds = savedProductIds,
                    onProductClick = onProductClick,
                    onRemoveFavorite = { savedProductIds.remove(it) }
                )
                "chats" -> {
                    if (!isLoggedIn) {
                        Box(
                            modifier = Modifier.fillMaxSize().padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Forum,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Chats are private", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Please sign in to read messages and negotiate with sellers on Vibro.", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(onClick = onSignInClick) {
                                    Text("Sign In Now")
                                }
                            }
                        }
                    } else {
                        ChatsScreenContent(
                            threads = chats,
                            onSendMessage = { threadId, text ->
                                val index = chats.indexOfFirst { it.id == threadId }
                                if (index != -1) {
                                    val thread = chats[index]
                                    chats[index] = thread.copy(
                                        lastMessage = text,
                                        time = "Just now",
                                        messages = thread.messages + ChatMessage("You", text, "Just now")
                                    )

                                    // Publish of chat message to Supabase
                                    coroutineScope.launch {
                                        if (NetworkManager.isSupabaseConfigured) {
                                            try {
                                                NetworkManager.supabaseApi?.insertChatMessage(
                                                    SupabaseChatMessage(
                                                        threadId = threadId,
                                                        sender = "You",
                                                        content = text,
                                                        time = "Just now"
                                                    )
                                                )
                                                android.util.Log.i("MarketplaceApp", "Message synced with Supabase successfully.")
                                            } catch (e: Exception) {
                                                android.util.Log.e("MarketplaceApp", "Failed to sync chat message: ${NetworkManager.getErrorMessage(e)}")
                                            }
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
                "profile" -> ProfileScreenContent(
                    myAdsCount = products.filter { it.sellerName == "You" }.size,
                    savedCount = savedProductIds.size,
                    isLoggedIn = isLoggedIn,
                    userName = userName,
                    userEmail = userEmail,
                    userCity = userCity,
                    onSignInClick = onSignInClick,
                    onSignOutClick = onSignOutClick,
                    onUpdateProfile = onUpdateProfile,
                    products = products,
                    savedProductIds = savedProductIds,
                    onProductClick = onProductClick,
                    sellerReviews = sellerReviews,
                    onAddReview = onAddReview,
                    onPostAdClick = { showPostAdDialog = true },
                    onManualCheckForUpdates = onManualCheckForUpdates
                )
            }

            if (showLoginRequiredDialog) {
                AlertDialog(
                    onDismissRequest = { showLoginRequiredDialog = false },
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Text("Sign In Required", fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Text("To list your products and post ads on Vibro Marketplace, you need to sign in first. It only takes a minute!")
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showLoginRequiredDialog = false
                                onSignInClick()
                            }
                        ) {
                            Text("Sign In Now")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showLoginRequiredDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }

            if (showPostAdDialog) {
                PostAdDialog(
                    onDismiss = { showPostAdDialog = false },
                    onPublish = { title, price, categoryId, location, condition, description, imageUrl, isPromoted ->
                        val cleanPriceNum = price.replace("[^\\d]".toRegex(), "")
                        val formattedPrice = "Br " + if (cleanPriceNum.isNotEmpty()) {
                            String.format("%,d", cleanPriceNum.toLong())
                        } else {
                            "0"
                        }
                        
                        val newAdId = (products.size + 101).toString()
                        val finalImgUrl = if (imageUrl.trim().isNotEmpty()) imageUrl else {
                            NetworkManager.getFallbackImageUrl(categoryId, title)
                        }
                        
                        val newProd = Product(
                            id = newAdId,
                            title = title,
                            price = formattedPrice,
                            imageUrl = finalImgUrl,
                            location = location,
                            condition = condition,
                            description = if (description.trim().isNotEmpty()) description else title,
                            categoryId = categoryId,
                            sellerName = "You",
                            isPromoted = isPromoted,
                            timeAgo = "Just now"
                        )
                        products.add(0, newProd)
                        mockProducts.add(0, newProd)
                        ProductPersistence.saveProducts(context, mockProducts)

                        // Sync newly posted ad to Supabase Database
                        coroutineScope.launch {
                            if (NetworkManager.isSupabaseConfigured) {
                                // Try inserting to 'ads' first, then 'products'
                                try {
                                    val payload = NetworkManager.buildInsertPayload(newProd, NetworkManager.adTableKeys)
                                    android.util.Log.d("MarketplaceApp", "Prepared payload for 'ads' table: $payload")
                                    NetworkManager.supabaseApi?.insertAd(payload)
                                    android.util.Log.i("MarketplaceApp", "Synced newly published ad to Supabase (ads table).")
                                    NetworkManager.syncErrorMessage = null
                                } catch (e: Exception) {
                                    android.util.Log.w("MarketplaceApp", "Could not sync new ad to Supabase (ads table): ${NetworkManager.getErrorMessage(e)}. Trying 'products' table...")
                                    try {
                                        val payload = NetworkManager.buildInsertPayload(newProd, NetworkManager.productTableKeys)
                                        android.util.Log.d("MarketplaceApp", "Prepared payload for 'products' table: $payload")
                                        NetworkManager.supabaseApi?.insertProduct(payload)
                                        android.util.Log.i("MarketplaceApp", "Synced newly published ad to Supabase (products table).")
                                        NetworkManager.syncErrorMessage = null
                                    } catch (ex: Exception) {
                                        android.util.Log.e("MarketplaceApp", "Could not sync new ad to Supabase (products table): ${NetworkManager.getErrorMessage(ex)}")
                                        NetworkManager.syncErrorMessage = "Failed to insert ad: ${NetworkManager.getErrorMessage(ex)}"
                                    }
                                }
                                onRefreshFeed()
                            }
                        }

                        Toast.makeText(context, "Ad published successfully!", Toast.LENGTH_SHORT).show()
                        showPostAdDialog = false
                        activeTab = "home"
                    }
                )
            }
        }
    }
}

// --- Home Tab Content with filters ---
@Composable
fun HomeScreenContent(
    products: List<Product>,
    savedProductIds: List<String>,
    onProductClick: (String) -> Unit,
    onRefreshFeed: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showConnectionDialog by remember { mutableStateOf(false) }
    var showCitySelectorHome by remember { mutableStateOf(false) }
    var selectedCityHomeFilter by remember { mutableStateOf("Ethiopia (All)") }

    val filteredProducts = products.filter { product ->
        val matchesSearch = product.title.contains(searchQuery, ignoreCase = true) ||
                            product.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
        val matchesCity = if (selectedCityHomeFilter == "Ethiopia (All)") true else product.location.contains(selectedCityHomeFilter, ignoreCase = true)
        matchesSearch && matchesCategory && matchesCity
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // High fidelity header matching Jiji style
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.clickable { showCitySelectorHome = true }.padding(4.dp)) {
                        Box(
                            modifier = Modifier.size(32.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("LOCATION", style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(selectedCityHomeFilter, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                    Box(
                        modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = "Alerts", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(Color.Red, CircleShape)
                                .align(Alignment.TopEnd)
                                .offset(x = (-2).dp, y = 2.dp)
                        )
                    }
                }

                // Modern Search Text Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("search_input"),
                    placeholder = { Text("Search 1.5M+ items on Vibro...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )


            }
        }

        // Horizontal Category Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "All" item
            item {
                CategoryChip(
                    name = "All Items",
                    icon = Icons.Default.GridView,
                    isSelected = selectedCategoryId == null,
                    bgColor = MaterialTheme.colorScheme.surfaceVariant,
                    iconColor = MaterialTheme.colorScheme.primary,
                    onClick = { selectedCategoryId = null }
                )
            }

            items(mockCategories) { cat ->
                CategoryChip(
                    name = cat.name,
                    icon = cat.icon,
                    isSelected = selectedCategoryId == cat.id,
                    bgColor = cat.bgColor,
                    iconColor = cat.iconColor,
                    onClick = { selectedCategoryId = if (selectedCategoryId == cat.id) null else cat.id }
                )
            }
        }

        // Listings header bar
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (selectedCategoryId != null || searchQuery.isNotEmpty()) "Search Results (${filteredProducts.size})" else "Trending Near You",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "REFRESH FEED",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { onRefreshFeed() }
            )
        }

        if (filteredProducts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.SearchOff, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(48.dp))
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("No matching ads found", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Try looking for alternative keywords or categories.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(filteredProducts) { item ->
                    ProductGridCard(
                        product = item,
                        onClick = { onProductClick(item.id) }
                    )
                }


            }
        }
    }

    if (showCitySelectorHome) {
        androidx.compose.ui.window.Dialog(onDismissRequest = { showCitySelectorHome = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Select City", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(bottom = 16.dp))
                    
                    Surface(
                        modifier = Modifier.fillMaxWidth().clickable {
                            selectedCityHomeFilter = "Ethiopia (All)"
                            showCitySelectorHome = false
                        }.padding(vertical = 12.dp)
                    ) {
                        Text("Ethiopia (All)", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary))
                    }
                    
                    HorizontalDivider()
                    
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        items(ethiopianCities) { city ->
                            Surface(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedCityHomeFilter = city.name
                                    showCitySelectorHome = false
                                }.padding(vertical = 12.dp)
                            ) {
                                Text(city.name, style = MaterialTheme.typography.bodyLarge)
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { showCitySelectorHome = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close")
                    }
                }
            }
        }
    }

    if (showConnectionDialog) {
        var inputUrl by remember { mutableStateOf(NetworkManager.customSupabaseUrl.ifEmpty { NetworkManager.supabaseUrl }) }
        var inputKey by remember { mutableStateOf(NetworkManager.customSupabaseAnonKey.ifEmpty { NetworkManager.supabaseAnonKey }) }
        val context = LocalContext.current

        AlertDialog(
            onDismissRequest = { showConnectionDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Cloud,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Supabase Sync Settings")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "If your environment credentials are not synced, you can view or enter your Supabase URL and Anon Key below to establish a direct sandbox synchronization connection.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = inputUrl,
                        onValueChange = { inputUrl = it },
                        label = { Text("Supabase URL") },
                        placeholder = { Text("https://your-project.supabase.co") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = inputKey,
                        onValueChange = { inputKey = it },
                        label = { Text("Supabase Anon Key") },
                        placeholder = { Text("eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    if (NetworkManager.isSupabaseConfigured && NetworkManager.syncErrorMessage == null) Color(0xFF137333) else Color(0xFFB06000),
                                    CircleShape
                                )
                        )
                        Text(
                            text = if (NetworkManager.isSupabaseConfigured) {
                                if (NetworkManager.syncErrorMessage == null) "Status: Configuration Active & Synced (${NetworkManager.syncItemCount} remote items)" else "Status: Configured but Sync Failed"
                            } else "Status: Inactive/Offline Sandbox",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (NetworkManager.isSupabaseConfigured && NetworkManager.syncErrorMessage == null) Color(0xFF137333) else Color(0xFFB06000)
                        )
                    }

                    if (NetworkManager.syncErrorMessage != null) {
                        Text(
                            text = "Error: ${NetworkManager.syncErrorMessage}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            },
            confirmButton = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(
                        onClick = {
                            NetworkManager.clearConfig(context)
                            inputUrl = ""
                            inputKey = ""
                            onRefreshFeed()
                            showConnectionDialog = false
                            Toast.makeText(context, "Reset to environment variables defaults", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Text("Reset to Env")
                    }

                    Row {
                        TextButton(onClick = { showConnectionDialog = false }) {
                            Text("Cancel")
                        }
                        Button(
                            onClick = {
                                if (inputUrl.isNotBlank() && inputKey.isNotBlank()) {
                                    NetworkManager.saveConfig(context, inputUrl, inputKey)
                                    onRefreshFeed()
                                    showConnectionDialog = false
                                    Toast.makeText(context, "Saved & Syncing...", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please fill in both fields", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Text("Save & Sync")
                        }
                    }
                }
            }
        )
    }
}

// --- Custom Category Chip to avoid Material 3 Experimental warnings ---
@Composable
fun CategoryChip(
    name: String,
    icon: ImageVector,
    isSelected: Boolean,
    bgColor: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    // Unify colors: use primary when selected, and a subtle transparent surface variant when unselected
    val activeBg = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val activeTint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(activeBg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = name,
            tint = activeTint,
            modifier = Modifier.size(24.dp)
        )
    }
}

// --- Saved View Screen ---
@Composable
fun SavedScreenContent(
    products: List<Product>,
    savedProductIds: List<String>,
    onProductClick: (String) -> Unit,
    onRemoveFavorite: (String) -> Unit
) {
    val savedItems = products.filter { savedProductIds.contains(it.id) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Simple custom Top Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Bookmarks & Saves", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${savedItems.size} items monitored", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        if (savedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Box(
                        modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(32.dp))
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No bookmarks saved", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Tap the heart icon on any ad to bookmark it for monitoring.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedItems) { prod ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onProductClick(prod.id) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AsyncImage(
                                model = prod.imageUrl,
                                contentDescription = prod.title,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop,
                                error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title)),
                                placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(prod.price, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row {
                                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Text(prod.location, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            IconButton(onClick = { onRemoveFavorite(prod.id) }) {
                                Icon(Icons.Default.Favorite, contentDescription = "Unsave", tint = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Conversations Box View Screen ---
@Composable
fun ChatsScreenContent(
    threads: List<ChatThread>,
    onSendMessage: (String, String) -> Unit
) {
    var activeThread by remember { mutableStateOf<ChatThread?>(null) }
    var writeMsgText by remember { mutableStateOf("") }

    if (activeThread != null) {
        val curThread = threads.find { it.id == activeThread?.id } ?: activeThread!!

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Header bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeThread = null }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Box(
                        modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(curThread.senderName.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(curThread.senderName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        Text("Online seller", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }

            // Message Scroll area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                curThread.messages.forEach { itemMsg ->
                    val isYou = itemMsg.sender == "You"
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        contentAlignment = if (isYou) Alignment.CenterEnd else Alignment.CenterStart
                    ) {
                        Column(horizontalAlignment = if (isYou) Alignment.End else Alignment.Start) {
                            Surface(
                                shape = RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isYou) 12.dp else 2.dp,
                                    bottomEnd = if (isYou) 2.dp else 12.dp
                                ),
                                color = if (isYou) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isYou) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            ) {
                                Text(itemMsg.content, modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp), fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(itemMsg.time, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Chat typing control bar
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 4.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = writeMsgText,
                        onValueChange = { writeMsgText = it },
                        modifier = Modifier.weight(1f).height(48.dp),
                        placeholder = { Text("Write message here...") },
                        shape = RoundedCornerShape(24.dp),
                        maxLines = 1,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (writeMsgText.trim().isNotEmpty()) {
                                onSendMessage(curThread.id, writeMsgText.trim())
                                writeMsgText = ""
                            }
                        },
                        colors = IconButtonDefaults.iconButtonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    } else {
        // Conversation lists screen
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("My Inbox", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Interact with buyers & sellers directly", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            if (threads.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No chats active yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(threads) { thread ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { activeThread = thread },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier.size(40.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        thread.senderName.first().toString(),
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(thread.senderName, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text(thread.time, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(thread.lastMessage, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- Profile View Screen ---
@Composable
fun ProfileScreenContent(
    myAdsCount: Int,
    savedCount: Int,
    isLoggedIn: Boolean,
    userName: String,
    userEmail: String,
    userCity: String,
    onSignInClick: () -> Unit,
    onSignOutClick: () -> Unit,
    onUpdateProfile: (String, String) -> Unit,
    products: MutableList<Product>,
    savedProductIds: MutableList<String>,
    onProductClick: (String) -> Unit,
    sellerReviews: List<Review>,
    onAddReview: (reviewerName: String, rating: Int, comment: String) -> Unit,
    onPostAdClick: () -> Unit = {},
    onManualCheckForUpdates: () -> Unit = {}
) {
    var activeSubScreen by rememberSaveable { mutableStateOf<String?>(null) }

    BackHandler(enabled = activeSubScreen != null) {
        activeSubScreen = null
    }

    var currentPlan by remember { mutableStateOf("Free Tier") }
    var isPhoneVerified by remember { mutableStateOf(false) }
    var currentNotificationOption by remember { mutableStateOf(true) }
    var currentEmailNewsletterOption by remember { mutableStateOf(false) }
    var currentCurrencyOption by remember { mutableStateOf("ETB (Br)") }
    var currentMeetupMode by remember { mutableStateOf(true) }

    if (!isLoggedIn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Guest",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Guest Mode",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Sign in to list vehicles, smartphones or flats, browse private chats with sellers, unlock store plans, and save bookmarks permanently.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Login, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign In or Register now", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Vibro Marketplace™ - Ethiopia",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.4f)
            )
        }
    } else {
        if (activeSubScreen != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                // Custom App Bar Header for Sub-Page
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(top = 16.dp, bottom = 16.dp, start = 8.dp, end = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { activeSubScreen = null }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (activeSubScreen) {
                            "listings" -> "My Active Listings"
                            "bookmarks" -> "Saved Bookmarks"
                            "reviews" -> "My Store Reviews"
                            "store_plans" -> "Vibro Store Booster Plans"
                            "settings" -> "Manage Preferences"
                            "security" -> "Trust & Security Hub"
                            "support" -> "Vibro Live Help Desk"
                            "edit_profile" -> "Update Profile Details"
                            else -> ""
                        },
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                }

                // Sub-Page Body Content with full-width padding
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    when (activeSubScreen) {
                        "listings" -> {
                            ProfileListingsPageContent(
                                products = products,
                                onProductClick = { prodId ->
                                    activeSubScreen = null
                                    onProductClick(prodId)
                                },
                                onPostAdClick = {
                                    activeSubScreen = null
                                    onPostAdClick()
                                }
                            )
                        }
                        "bookmarks" -> {
                            ProfileBookmarksPageContent(
                                products = products,
                                savedProductIds = savedProductIds,
                                onProductClick = { prodId ->
                                    activeSubScreen = null
                                    onProductClick(prodId)
                                }
                            )
                        }
                        "reviews" -> {
                            SellerReviewsPageContent(
                                sellerName = "You",
                                sellerReviews = sellerReviews,
                                onAddReview = onAddReview
                            )
                        }
                        "store_plans" -> {
                            ProfileStorePlansPageContent(
                                currentPlan = currentPlan,
                                onPlanUpgraded = { currentPlan = it },
                                onDismiss = { activeSubScreen = null }
                            )
                        }
                        "settings" -> {
                            ProfileSettingsPageContent(
                                userCity = userCity,
                                onUpdateLocation = { onUpdateProfile(userName, it) },
                                currentNotification = currentNotificationOption,
                                onNotificationChange = { currentNotificationOption = it },
                                currentCurrency = currentCurrencyOption,
                                onCurrencyChange = { currentCurrencyOption = it },
                                currentMeetupMode = currentMeetupMode,
                                onMeetupModeChange = { currentMeetupMode = it },
                                onDismiss = { activeSubScreen = null }
                            )
                        }
                        "security" -> {
                            ProfileSecurityPageContent(
                                isPhoneVerified = isPhoneVerified,
                                onPhoneVerifiedChange = { isPhoneVerified = it },
                                onDismiss = { activeSubScreen = null }
                            )
                        }
                        "support" -> {
                            ProfileSupportPageContent(
                                onDismiss = { activeSubScreen = null }
                            )
                        }
                        "edit_profile" -> {
                            ProfileEditPageContent(
                                userName = userName,
                                userCity = userCity,
                                onUpdateProfile = onUpdateProfile,
                                onDismiss = { activeSubScreen = null }
                            )
                        }
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 28.dp, horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(76.dp)
                                .background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f), CircleShape)
                                .border(3.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                if (userName.isNotEmpty()) userName.first().toString() else "U",
                                fontSize = 32.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(userEmail, fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        Text(userCity, fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = if (currentPlan == "Free Tier") Color.White.copy(alpha = 0.15f) else Color(0xFFFBBF24),
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (currentPlan == "Free Tier") Icons.Default.Person else Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (currentPlan == "Free Tier") Color.White else Color.Black,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = currentPlan,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (currentPlan == "Free Tier") Color.White else Color.Black
                                    )
                                }
                            }
                            if (isPhoneVerified) {
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = Color(0xFF10B981),
                                    modifier = Modifier.padding(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Text(
                                            text = "Verified Phone",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { activeSubScreen = "edit_profile" },
                            colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Profile Details", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Stats boxes row
                val reviewsForYou = sellerReviews.filter { it.sellerName == "You" }
                val avgRatingYou = if (reviewsForYou.isEmpty()) 4.9f else reviewsForYou.map { it.rating }.average().toFloat()
                val formattedRatingYou = String.format("%.1f", avgRatingYou)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatsBox(Modifier.weight(1f), "My Listed Ads", myAdsCount.toString(), Icons.Default.Campaign, Color(0xFFD1FAE5), onClick = { activeSubScreen = "listings" })
                    StatsBox(Modifier.weight(1f), "My Bookmarks", savedCount.toString(), Icons.Default.Favorite, Color(0xFFFFE4E6), onClick = { activeSubScreen = "bookmarks" })
                    StatsBox(Modifier.weight(1f), "Store Reviews", formattedRatingYou, Icons.Default.Star, Color(0xFFFEF3C7), onClick = { activeSubScreen = "reviews" })
                }

                // Standard profile sections
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        ProfileOptionRow(Icons.Default.AccountBalanceWallet, "Premium Store Plans", "Get 10x buyers boost now", onClick = { activeSubScreen = "store_plans" })
                        HorizontalDivider()
                        ProfileOptionRow(Icons.Default.Settings, "Account & Location Settings", "Manage search address and defaults", onClick = { activeSubScreen = "settings" })
                        HorizontalDivider()
                        ProfileOptionRow(Icons.Default.Shield, "Trust & Security Rules", "Active logins and phone verification status", onClick = { activeSubScreen = "security" })
                        HorizontalDivider()
                        ProfileOptionRow(Icons.Default.SupportAgent, "Live Help Support Center", "Reach Vibro agents in 24 hours", onClick = { activeSubScreen = "support" })
                        HorizontalDivider()
                        ProfileOptionRow(Icons.Default.Info, "Check for Updates", "Scan GitHub for a newer version of Vibro", onClick = onManualCheckForUpdates)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Logout card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clickable { onSignOutClick() },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.08f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Log Out",
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Sign Out of Vibro Account",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

// --- Profile Sub-Dialogs implementations ---

@Composable
fun EditAdDialog(
    product: Product,
    onDismiss: () -> Unit,
    onSave: (title: String, price: String, categoryId: String, location: String, condition: String, description: String, imageUrl: String) -> Unit
) {
    var title by remember { mutableStateOf(product.title) }
    var price by remember { mutableStateOf(product.price.replace("[^\\d]".toRegex(), "")) }
    var categoryId by remember { mutableStateOf(product.categoryId) }
    var location by remember { mutableStateOf(product.location) }
    var condition by remember { mutableStateOf(product.condition) }
    var description by remember { mutableStateOf(product.description) }
    var imageUrl by remember { mutableStateOf(product.imageUrl) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "Edit Your Listing",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Title
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                // Price
                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (Birr)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                // Category
                Text("Category", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val categories = listOf(
                        "1" to "Vehicles",
                        "2" to "Phones",
                        "3" to "Property",
                        "4" to "Fashion",
                        "5" to "Furniture"
                    )
                    categories.forEach { (id, name) ->
                        val selected = categoryId == id
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { categoryId = id }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(name, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Condition
                Text("Condition", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val conditions = listOf("New", "Used - Like New", "Used - Good", "Used - Fair")
                    conditions.forEach { cond ->
                        val selected = condition == cond
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { condition = cond }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(cond.substringAfter(" - "), color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (e.g. Addis Ababa, Bole)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                // Image URL
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
                )

                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(onClick = {
                        if (title.isBlank() || price.isBlank() || location.isBlank()) {
                            return@Button
                        }
                        onSave(title, price, categoryId, location, condition, description, imageUrl)
                    }) {
                        Text("Save Changes")
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileListingsPageContent(
    products: MutableList<Product>,
    onProductClick: (String) -> Unit,
    onPostAdClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var editingProduct by remember { mutableStateOf<Product?>(null) }
    val myProducts = products.filter { it.sellerName == "You" }

    if (myProducts.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Inbox, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("You haven't posted any ads yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onPostAdClick) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Post Your First Ad")
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(myProducts.size) { index ->
                    val prod = myProducts[index]

                    KeyfacedCard(
                        prod = prod,
                        onEditAd = {
                            editingProduct = prod
                        },
                        onViewAd = {
                            onProductClick(prod.id)
                        },
                        onDeleteAd = {
                            products.remove(prod)
                            mockProducts.remove(prod)
                            ProductPersistence.saveProducts(context, mockProducts)
                            
                            coroutineScope.launch {
                                if (NetworkManager.isSupabaseConfigured) {
                                    try {
                                        val idQuery = "eq.${prod.id}"
                                        try {
                                            NetworkManager.supabaseApi?.deleteAd(idQuery)
                                        } catch (e: Exception) {
                                            NetworkManager.supabaseApi?.deleteProduct(idQuery)
                                        }
                                        android.util.Log.i("MarketplaceApp", "Deleted ad with ID ${prod.id} from Supabase")
                                    } catch (e: Exception) {
                                        android.util.Log.e("MarketplaceApp", "Failed to delete ad from Supabase: ${NetworkManager.getErrorMessage(e)}")
                                    }
                                }
                            }
                            Toast.makeText(context, "Ad removed successfully!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            if (editingProduct != null) {
                val currentEditingProd = editingProduct!!
                EditAdDialog(
                    product = currentEditingProd,
                    onDismiss = { editingProduct = null },
                    onSave = { title, price, categoryId, location, condition, description, imageUrl ->
                        val cleanPriceNum = price.replace("[^\\d]".toRegex(), "")
                        val formattedPrice = "Br " + if (cleanPriceNum.isNotEmpty()) {
                            String.format("%,d", cleanPriceNum.toLong())
                        } else {
                            "0"
                        }
                        val finalImgUrl = if (imageUrl.trim().isNotEmpty()) imageUrl else {
                            NetworkManager.getFallbackImageUrl(categoryId, title)
                        }

                        val updatedProd = currentEditingProd.copy(
                            title = title,
                            price = formattedPrice,
                            categoryId = categoryId,
                            location = location,
                            condition = condition,
                            description = description,
                            imageUrl = finalImgUrl
                        )

                        val globalIndex = products.indexOfFirst { it.id == currentEditingProd.id }
                        if (globalIndex != -1) {
                            products[globalIndex] = updatedProd
                        }
                        val mockIndex = mockProducts.indexOfFirst { it.id == currentEditingProd.id }
                        if (mockIndex != -1) {
                            mockProducts[mockIndex] = updatedProd
                        }
                        ProductPersistence.saveProducts(context, mockProducts)

                        coroutineScope.launch {
                            if (NetworkManager.isSupabaseConfigured) {
                                try {
                                    val payload = NetworkManager.buildInsertPayload(updatedProd, NetworkManager.adTableKeys)
                                    val idQuery = "eq.${updatedProd.id}"
                                    try {
                                        NetworkManager.supabaseApi?.updateAd(idQuery, payload)
                                        android.util.Log.i("MarketplaceApp", "Updated ad in Supabase ads table")
                                    } catch (e: Exception) {
                                        val prodPayload = NetworkManager.buildInsertPayload(updatedProd, NetworkManager.productTableKeys)
                                        NetworkManager.supabaseApi?.updateProduct(idQuery, prodPayload)
                                        android.util.Log.i("MarketplaceApp", "Updated product in Supabase products table")
                                    }
                                    NetworkManager.syncErrorMessage = null
                                } catch (e: Exception) {
                                    android.util.Log.e("MarketplaceApp", "Failed to sync updated ad to Supabase: ${NetworkManager.getErrorMessage(e)}")
                                    NetworkManager.syncErrorMessage = "Failed to sync updated ad: ${NetworkManager.getErrorMessage(e)}"
                                }
                            }
                        }

                        editingProduct = null
                        Toast.makeText(context, "Listing updated successfully!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
fun KeyfacedCard(
    prod: Product,
    onEditAd: () -> Unit,
    onViewAd: () -> Unit,
    onDeleteAd: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AsyncImage(
                    model = prod.imageUrl,
                    contentDescription = prod.title,
                    modifier = Modifier.size(54.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop,
                    error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title)),
                    placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title))
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(prod.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
                    Text("${prod.price} Birr", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                    val catLabel = when (prod.categoryId) {
                        "1" -> "Vehicles"
                        "2" -> "Phones & Electronics"
                        "3" -> "Property & Rent"
                        "4" -> "Fashion & Wearables"
                        "5" -> "Furniture"
                        else -> "Other"
                    }
                    Text("Category: $catLabel", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(onClick = onViewAd) {
                    Text("View", fontSize = 12.sp)
                }
                TextButton(onClick = onEditAd) {
                    Text("Edit Listing", fontSize = 12.sp)
                }
                TextButton(
                    onClick = onDeleteAd,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ProfileBookmarksPageContent(
    products: List<Product>,
    savedProductIds: MutableList<String>,
    onProductClick: (String) -> Unit
) {
    val bookmarkedItems = products.filter { savedProductIds.contains(it.id) }
    if (bookmarkedItems.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("No bookmarked items yet.", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 16.sp)
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(bookmarkedItems.size) { idx ->
                val prod = bookmarkedItems[idx]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = prod.imageUrl,
                            contentDescription = prod.title,
                            modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title)),
                            placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title))
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(prod.title, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyLarge)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("${prod.price} Birr", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Row(horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = {
                                onProductClick(prod.id)
                            }) {
                                Icon(Icons.Default.ArrowForward, contentDescription = "View Ad", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                            }
                            IconButton(onClick = {
                                savedProductIds.remove(prod.id)
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove Bookmark", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SellerReviewsPageContent(
    sellerName: String,
    sellerReviews: List<Review>,
    onAddReview: (reviewerName: String, rating: Int, comment: String) -> Unit
) {
    val context = LocalContext.current
    val reviewsForSeller = sellerReviews.filter { it.sellerName == sellerName }
    val avgRating = if (reviewsForSeller.isEmpty()) 5.0f else reviewsForSeller.map { it.rating }.average().toFloat()
    val totalRatings = reviewsForSeller.size

    var newReviewerName by remember { mutableStateOf("") }
    var newRating by remember { mutableStateOf(5) }
    var newComment by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Score Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (sellerName == "You") "Your Public Score" else "Seller Public Score",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = String.format("%.1f", avgRating),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("/ 5.0", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f))
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Row {
                        repeat(5) { starIndex ->
                            val isFilled = starIndex < Math.round(avgRating)
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = if (isFilled) Color(0xFFFBBF24) else Color.LightGray,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Based on $totalRatings ratings", fontSize = 12.sp, color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f))
                }
            }
        }

        // Reviews List
        Text("Verification Comments", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        if (reviewsForSeller.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No reviews yet. Be the first to leave a review!", fontSize = 14.sp, color = Color.Gray)
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                reviewsForSeller.forEach { review ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(review.reviewerName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                                    Text(review.timeAgo, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Row {
                                    repeat(5) { starIndex ->
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (starIndex < review.rating) Color(0xFFFBBF24) else Color.LightGray,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(review.comment, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // Write a Review Form
        HorizontalDivider()
        Text("Add Your Rating", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Rating:", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            repeat(5) { starIndex ->
                val ratingValue = starIndex + 1
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Rate $ratingValue Stars",
                    tint = if (ratingValue <= newRating) Color(0xFFFBBF24) else Color.LightGray,
                    modifier = Modifier
                        .size(32.dp)
                        .clickable { newRating = ratingValue }
                )
            }
        }

        OutlinedTextField(
            value = newReviewerName,
            onValueChange = { newReviewerName = it },
            label = { Text("Your Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = newComment,
            onValueChange = { newComment = it },
            label = { Text("Your Review / Comment") },
            maxLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Button(
            onClick = {
                val reviewer = newReviewerName.trim().ifEmpty { "Anonymous Buyer" }
                val comment = newComment.trim().ifEmpty { "Excellent product and service!" }
                onAddReview(reviewer, newRating, comment)
                Toast.makeText(context, "Review submitted successfully!", Toast.LENGTH_SHORT).show()
                newReviewerName = ""
                newComment = ""
                newRating = 5
            },
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Submit Review", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ProfileStorePlansPageContent(
    currentPlan: String,
    onPlanUpgraded: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var selectedPlanToUpgrade by remember { mutableStateOf<String?>(null) }
    var upgradeInputValue by remember { mutableStateOf("") }
    var paymentMethod by remember { mutableStateOf("Telebirr") }
    var isPayingSimulated by remember { mutableStateOf(false) }
    var isPaySuccess by remember { mutableStateOf(false) }

    if (selectedPlanToUpgrade == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Current Active Plan", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(currentPlan, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    SuggestionChip(
                        onClick = {},
                        label = { Text(if (currentPlan == "Free Tier") "Basic" else "Booster Active", fontSize = 11.sp) }
                    )
                }
            }

            Text("Upgrade Packages", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

            val plans = listOf(
                Triple("Bronze Booster", "150 Birr / month", listOf("5 Concurrent Active Ads", "1 Boost Credit (2x Search Priority)")),
                Triple("Gold Merchant", "499 Birr / month", listOf("20 Concurrent Active Ads", "5 Boost Credits (5x Search Priority)")),
                Triple("Platinum Boss", "1200 Birr / month", listOf("Unlimited Active Ads", "15 Boost Credits (10x Search Priority)"))
            )

            plans.forEach { (planName, price, features) ->
                val isCurrent = currentPlan == planName
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                    colors = CardDefaults.cardColors(containerColor = if (isCurrent) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(planName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text(price, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodyMedium)
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        features.forEach { feat ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(feat, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { selectedPlanToUpgrade = planName },
                            enabled = !isCurrent,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isCurrent) "Active Plan" else "Select Plan", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Safe Simulated Checkout", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(selectedPlanToUpgrade!!, fontWeight = FontWeight.Black, style = MaterialTheme.typography.titleMedium)
                Text(
                    when(selectedPlanToUpgrade) {
                        "Bronze Booster" -> "150 ETB"
                        "Gold Merchant" -> "499 ETB"
                        else -> "1200 ETB"
                    },
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            HorizontalDivider()

            Text("Select Local Wallet Provider:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)

            val pMethods = listOf("Telebirr", "CBE Birr", "Chapa Card", "Awash Birr")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pMethods.forEach { method ->
                    val isSelected = paymentMethod == method
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { paymentMethod = method }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                          Text(
                              text = method,
                              fontSize = 12.sp,
                              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                              color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                          )
                    }
                }
            }

            OutlinedTextField(
                value = upgradeInputValue,
                onValueChange = { upgradeInputValue = it.filter { ch -> ch.isDigit() } },
                label = { Text("$paymentMethod Phone / Account") },
                placeholder = { Text("e.g. 0912345678") },
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (isPayingSimulated) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Securing connection and simulating callback...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (isPaySuccess) {
                Text("Payment Simulated successfully! Welcome to $selectedPlanToUpgrade!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        if (!isPayingSimulated) {
                            selectedPlanToUpgrade = null
                            isPaySuccess = false
                            upgradeInputValue = ""
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        if (isPaySuccess) {
                            onPlanUpgraded(selectedPlanToUpgrade!!)
                            selectedPlanToUpgrade = null
                            isPaySuccess = false
                            upgradeInputValue = ""
                            onDismiss()
                        } else {
                            if (upgradeInputValue.isNotEmpty()) {
                                isPayingSimulated = true
                                coroutineScope.launch {
                                    kotlinx.coroutines.delay(1800)
                                    isPayingSimulated = false
                                    isPaySuccess = true
                                }
                            } else {
                                Toast.makeText(context, "Please enter your payment phone/account", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    enabled = !isPayingSimulated,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isPaySuccess) "Proceed" else "Simulate Pay")
                }
            }
        }
    }
}

@Composable
fun ProfileSettingsPageContent(
    userCity: String,
    onUpdateLocation: (String) -> Unit,
    currentNotification: Boolean,
    onNotificationChange: (Boolean) -> Unit,
    currentCurrency: String,
    onCurrencyChange: (String) -> Unit,
    currentMeetupMode: Boolean,
    onMeetupModeChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var tempNotification by remember { mutableStateOf(currentNotification) }
    var tempCurrency by remember { mutableStateOf(currentCurrency) }
    var tempMeetupMode by remember { mutableStateOf(currentMeetupMode) }
    var tempSelectedCity by remember { mutableStateOf(userCity) }
    var showCityDropdown by remember { mutableStateOf(false) }

    val addisNeighborhoods = listOf(
        "Bole, Addis Ababa",
        "Kazanchis, Addis Ababa",
        "Megenagna, Addis Ababa",
        "Piassa, Addis Ababa",
        "Lebu, Addis Ababa",
        "Ayat, Addis Ababa",
        "Hawassa, Sidama",
        "Adama, Oromia",
        "Bahir Dar, Amhara",
        "Dire Dawa"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column {
            Text("Preferred Location / Neighborhood", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { showCityDropdown = true }
                    .padding(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(tempSelectedCity.ifEmpty { "Select Neighborhood" }, style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            if (showCityDropdown) {
                DropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 240.dp)
                ) {
                    addisNeighborhoods.forEach { neighborhood ->
                        DropdownMenuItem(
                            text = { Text(neighborhood, fontSize = 14.sp) },
                            onClick = {
                                tempSelectedCity = neighborhood
                                showCityDropdown = false
                            }
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        Column {
            Text("Local Currency Unit", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("ETB (Br)", "USD ($)").forEach { curr ->
                    val isSelected = tempCurrency == curr
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .clickable { tempCurrency = curr }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = curr,
                            fontSize = 14.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Instant Push Notifications", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Get notified instantly on new incoming chat messages", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = tempNotification,
                onCheckedChange = { tempNotification = it }
            )
        }

        HorizontalDivider()

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Safe Trade Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(2.dp))
                Text("Prioritize matched buyers near monitored camera zones", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = tempMeetupMode,
                onCheckedChange = { tempMeetupMode = it }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onNotificationChange(tempNotification)
                    onCurrencyChange(tempCurrency)
                    onMeetupModeChange(tempMeetupMode)
                    if (tempSelectedCity != userCity) {
                        onUpdateLocation(tempSelectedCity)
                    }
                    onDismiss()
                    Toast.makeText(context, "Preferences saved successfully", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save")
            }
        }
    }
}

@Composable
fun ProfileSecurityPageContent(
    isPhoneVerified: Boolean,
    onPhoneVerifiedChange: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var showOtpSimulator by remember { mutableStateOf(false) }
    var isPhoneVerifiedState by remember { mutableStateOf(isPhoneVerified) }
    var otpPhoneInput by remember { mutableStateOf("") }
    var otpCodeInput by remember { mutableStateOf("") }
    var otpSentSuccessfully by remember { mutableStateOf(false) }
    var isVerifyingCodeSim by remember { mutableStateOf(false) }
    var countOfSessions by remember { mutableStateOf(2) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text("VIBRO ACCOUNT ID", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("VBR-9284-5501 Ethiopian", fontWeight = FontWeight.Black, style = MaterialTheme.typography.bodyMedium)
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Vibro Account ID", "VBR-9284-5501-889")
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Account ID copied!", Toast.LENGTH_SHORT).show()
                        }
                    ) {
                        Text("Copy ID", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        HorizontalDivider()

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Official Phone Verification", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        text = if (isPhoneVerifiedState) "Verified official badge active ✅" else "Unlock blue checkmark ❌",
                        fontSize = 12.sp,
                        color = if (isPhoneVerifiedState) Color(0xFF10B981) else MaterialTheme.colorScheme.error
                    )
                }
                if (!isPhoneVerifiedState) {
                    Button(
                        onClick = { showOtpSimulator = true },
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp)
                    ) {
                        Text("Verify Now", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Verified", tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                }
            }

            if (showOtpSimulator) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        if (!otpSentSuccessfully) {
                            Text("Simulated Ethiopian OTP Verification", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            OutlinedTextField(
                                value = otpPhoneInput,
                                onValueChange = { otpPhoneInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("Phone Number") },
                                placeholder = { Text("09XXXXXXXX") },
                                leadingIcon = { Text("+251 ", fontSize = 14.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp)) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Button(
                                onClick = {
                                    if (otpPhoneInput.length >= 9) {
                                        otpSentSuccessfully = true
                                        Toast.makeText(context, "OTP Code simulated successfully! Code is 1234", Toast.LENGTH_LONG).show()
                                    } else {
                                        Toast.makeText(context, "Enter a valid Ethiopian phone number", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Request Code (1234)", fontSize = 12.sp)
                            }
                        } else {
                            Text("Enter 4-Digit simulated OTP:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            OutlinedTextField(
                                value = otpCodeInput,
                                onValueChange = { otpCodeInput = it.filter { ch -> ch.isDigit() } },
                                label = { Text("4-Digit Code (Enter 1234)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            if (isVerifyingCodeSim) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp).align(Alignment.CenterHorizontally))
                            }
                            Button(
                                onClick = {
                                    if (otpCodeInput == "1234") {
                                        isVerifyingCodeSim = true
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(1200)
                                            isVerifyingCodeSim = false
                                            isPhoneVerifiedState = true
                                            onPhoneVerifiedChange(true)
                                            showOtpSimulator = false
                                            Toast.makeText(context, "Phone verified successfully ✅", Toast.LENGTH_LONG).show()
                                        }
                                    } else {
                                        Toast.makeText(context, "Incorrect OTP Code! Enter 1234", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = !isVerifyingCodeSim,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Confirm Code", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("ACTIVE HARDWARE LOGINS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("This Mobile - Android Streaming Emulator", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Location: Addis Ababa, Ethiopia • Active now", fontSize = 11.sp, color = Color(0xFF10B981))
                    }
                }
                if (countOfSessions > 1) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Xiaomi Redmi Note 12", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("Location: Hawassa • Active 2 days ago", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            if (countOfSessions > 1) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = {
                        countOfSessions = 1
                        Toast.makeText(context, "All other sessions terminated", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Terminate other sessions", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Done")
        }
    }
}

@Composable
fun ProfileSupportPageContent(
    onDismiss: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val chatMessagesSupport = remember {
        mutableStateListOf(
            Pair("Agent", "Hello! Welcome to Vibro Support. I am Betelhem. How can I assist you today in Addis Ababa?")
        )
    }
    var userChatText by remember { mutableStateOf("") }
    var faqExpandedIndex by remember { mutableStateOf<Int?>(null) }

    val faqs = listOf(
        Pair("How do I avoid advanced transfer scams?", "Always meet physically in open public areas (Bole cafes, shopping malls). Inspect the item fully, and check titles at the traffic bureau before bank app transfers. Don't pay deposits before seeing items!"),
        Pair("How long does my item post stay active?", "For free accounts, listings stay published for 30 consecutive days. Boosting your profile to Bronze Booster increases it to 60 days. You can renew expired ads on 'My Active Listings' anytime!"),
        Pair("How do I upload multiple product photos?", "Go to 'Post Ad', click 'Select listing photos'. You can launch your photo gallery launcher to select high-resolution photos under 5MB.")
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Select Trending FAQs:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            faqs.forEachIndexed { i, faq ->
                val isExp = faqExpandedIndex == i
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { faqExpandedIndex = if (isExp) null else i },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(faq.first, fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = if (isExp) Icons.Default.ArrowDropDown else Icons.Default.ChevronRight,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (isExp) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(faq.second, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        HorizontalDivider()

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF10B981))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Live Agent Support (Online)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
        }

        Card(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatMessagesSupport.size) { i ->
                    val msg = chatMessagesSupport[i]
                    val isAgent = msg.first == "Agent"
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (isAgent) Arrangement.Start else Arrangement.End
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (isAgent) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary
                            ),
                            shape = RoundedCornerShape(
                                topStart = 12.dp,
                                topEnd = 12.dp,
                                bottomStart = if (isAgent) 0.dp else 12.dp,
                                bottomEnd = if (isAgent) 12.dp else 0.dp
                            ),
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isAgent) "Betelhem (Support)" else "You",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (isAgent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = msg.second,
                                    fontSize = 13.sp,
                                    color = if (isAgent) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TextField(
                value = userChatText,
                onValueChange = { userChatText = it },
                placeholder = { Text("Ask anything...", fontSize = 13.sp) },
                singleLine = true,
                modifier = Modifier.weight(1f),
                textStyle = MaterialTheme.typography.bodyMedium
            )
            IconButton(
                onClick = {
                    if (userChatText.isNotEmpty()) {
                        val userMsg = userChatText
                        chatMessagesSupport.add(Pair("User", userMsg))
                        userChatText = ""
                        coroutineScope.launch {
                            kotlinx.coroutines.delay(1200)
                            val replyMsg = when {
                                userMsg.contains("scam", ignoreCase = true) || userMsg.contains("fraud", ignoreCase = true) -> {
                                    "Safety is our priority. Meet only at public spots, and never send advance deposits."
                                }
                                userMsg.contains("image", ignoreCase = true) || userMsg.contains("photo", ignoreCase = true) -> {
                                    "Ensure photo is in JPEG or PNG, under 5MB per upload."
                                }
                                else -> {
                                    "Thanks! Ticket assigned. We will solve this extremely fast!"
                                }
                            }
                            chatMessagesSupport.add(Pair("Agent", replyMsg))
                        }
                    }
                }
            ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Message", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            }
        }

        Button(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Close Help Desk")
        }
    }
}

@Composable
fun StatsBox(modifier: Modifier = Modifier, title: String, value: String, icon: ImageVector, backColor: Color, onClick: () -> Unit) {
    Surface(
        modifier = modifier.height(96.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = backColor
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(28.dp).background(Color.White.copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(14.dp))
            }
            Column {
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.Black)
                Text(title, fontSize = 10.sp, color = Color.DarkGray, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
fun SellerReviewsDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    sellerName: String,
    sellerReviews: List<Review>,
    onAddReview: (reviewerName: String, rating: Int, comment: String) -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Reviews for $sellerName", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Box(modifier = Modifier.heightIn(max = 480.dp)) {
                SellerReviewsPageContent(
                    sellerName = sellerName,
                    sellerReviews = sellerReviews,
                    onAddReview = onAddReview
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun ProfileEditPageContent(
    userName: String,
    userCity: String,
    onUpdateProfile: (String, String) -> Unit,
    onDismiss: () -> Unit
) {
    var tempName by remember { mutableStateOf(userName) }
    var tempCity by remember { mutableStateOf(userCity) }
    var showCityDropdown by remember { mutableStateOf(false) }

    val addisNeighborhoods = listOf(
        "Bole, Addis Ababa",
        "Kazanchis, Addis Ababa",
        "Megenagna, Addis Ababa",
        "Piassa, Addis Ababa",
        "Lebu, Addis Ababa",
        "Ayat, Addis Ababa",
        "Hawassa, Sidama",
        "Adama, Oromia",
        "Bahir Dar, Amhara",
        "Dire Dawa"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Edit Profile Details", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        OutlinedTextField(
            value = tempName,
            onValueChange = { tempName = it },
            label = { Text("Display Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Column {
            Text("Preferred Neighborhood / City", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .clickable { showCityDropdown = true }
                    .padding(14.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(tempCity.ifEmpty { "Select Neighborhood" }, style = MaterialTheme.typography.bodyLarge)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            if (showCityDropdown) {
                DropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false },
                    modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 240.dp)
                ) {
                    addisNeighborhoods.forEach { neighborhood ->
                        DropdownMenuItem(
                            text = { Text(neighborhood, fontSize = 14.sp) },
                            onClick = {
                                tempCity = neighborhood
                                showCityDropdown = false
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onUpdateProfile(tempName, tempCity)
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Changes")
            }
        }
    }
}

@Composable
fun ProfileOptionRow(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(subtitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
    }
}

data class VibroArticle(
    val id: String,
    val title: String,
    val excerpt: String,
    val rTime: String,
    val fullContent: String,
    val icon: ImageVector
)

val mockArticlesByCat = mapOf(
    "1" to listOf(
        VibroArticle("v1", "Crucial Guide: Inspecting Used Cars in Addis", "Don't get scammed. Learn how to inspect the engine, verify custom clearance, and structure payments safely.", "4 min read", "When buying a used car in Addis Ababa:\n\n1. Check the body paint for signs of accident reconstruction.\n2. Verify the custom clearance paperwork / Libret from the transport bureau directly before paying a deposit.\n3. Hire an independent mechanic to perform a compression check.\n4. Ensure the seller is either the owner on the Libret or has an officially verified Power of Attorney.", Icons.Default.Build),
        VibroArticle("v2", "How to Verify Car Ownership Docs in Ethiopia", "Comprehensive checklist for transferring ownership, checking active bank collaterals, and transport bureau processes.", "3 min read", "Step 1: Get a copy of the Libret (registration folder).\nStep 2: Take it to the nearest driver & vehicle licensing bureau to run a search for active liens or active bank collateral.\nStep 3: Draft an official sales contract via the public notary office.\nStep 4: Do not pay full cash in hand; use certified bank payments (CPO) to prevent double transfers.", Icons.Default.Assignment)
    ),
    "2" to listOf(
        VibroArticle("p1", "Is Your iPhone Original? Checking IMEI Status", "How to use local telecom (Ethio Telecom) and global databases to verify IMEI, warranty, and network locks.", "2 min read", "1. Dial *#06# to display the IMEI on the device screen.\n2. Cross-reference the IMEI with the physical engraving on the SIM card tray and the box barcode.\n3. Use global portals to check for lock status / iCloud lock.\n4. Avoid buying iPhones in a locked state or with signed-in Apple IDs.", Icons.Default.Smartphone),
        VibroArticle("p2", "How to Avoid Refurbished and Replica Android Phones", "Spotting cloned displays, replica boxes, or counterfeit software builds in modern electronic markets.", "3 min read", "Some local sellers package high-quality replicas (clones) as originals.\n\n- Test the standard camera quality; replicas have poor lenses and slow focus.\n- Use hardware benchmarking apps like CPU-Z to verify the processor model fits the specifications.\n- Verify charging speed matching the actual model specifications.", Icons.Default.Info)
    ),
    "3" to listOf(
        VibroArticle("r1", "Tenant Checklist for Bole & Kazanchis Apartments", "What to look for in your rental agreement, security deposits, water access, and utility bill sharing options.", "5 min read", "Before committing to a rental in Addis Ababa:\n\n- Water & Backup: Ensure the building has active water reservoirs/tanks and automatic generator switchover.\n- Security: Verify the door locking mechanisms and gate security rules.\n- Agreement: Get a written, stamp-notarized agreement. Word-of-mouth is not legally binding for leases.\n- Utilities: Clarify how electricity (prepaid tokens) is loaded and divided in multi-story villas.", Icons.Default.Home),
        VibroArticle("r2", "Evaluating Land Values and House Titles in Ethiopia", "Essential legal procedures for verifying property deeds, avoiding multiple-owner scams, and bank validation.", "4 min read", "Verify the 'Carta' (Property Title Deed) with the district land management bureau (Woreda) to confirm the seller is registered and has no mortgage default. Always proceed through authorized government legal channels.", Icons.Default.Assignment)
    ),
    "4" to listOf(
        VibroArticle("f1", "Finding Your Perfect Fit: Ethiopian Sizing Guide", "International size charts explained for Italian, US, and UK conversions commonly seen in Addis boutiques.", "2 min read", "Ensure correct fits when ordering fashion:\n- Italian luxury cuts usually run slimmer than US cuts.\n- For shoes, Euro sizes (39-45) are the standard in Ethiopian retail; ask for measurements in centimeters if uncertain.", Icons.Default.Checkroom),
        VibroArticle("f2", "How to Spot Luxury Replica Sneakers & Designer Clothing", "Materials, stitching patterns, and branding indicators to tell authentic premium wear from clever clones.", "3 min read", "Inspect the inner stitching: authentic boutique clothing has secure twin stitching. Replica printing wears off after the first wash; feel the weight and quality of the underlying fabric.", Icons.Default.ShoppingBag)
    ),
    "5" to listOf(
        VibroArticle("u1", "Inspecting Wooden Joinery & Upholstery Like a Pro", "A checklist for assessing solid wood frames, local wanza/wood types, and high density fabric durability.", "4 min read", "1. Frame Material: Ask if the frame is made of solid Wanza, Korke, or cheap chipboard.\n2. Joint Strength: Shake the chairs to check for dowelled and glued joints vs staples.\n3. Foam Density: Low-quality foam sinks permanently within 3 months; specify 30+ density foam.", Icons.Default.Build),
        VibroArticle("u2", "Designing a Spacious Living Room: Sofa Dimensions Guide", "How to measure available floor space, door layouts, and corner clearances before delivery in apartments.", "3 min read", "Ensure you measure entry doors, lift widths, and hallway turns in modern apartments before purchasing large L-shaped sofas, so they physically fit during delivery.", Icons.Default.Home)
    )
)

// --- Product detail page full style --
@Composable
fun ProductDetailScreen(
    product: Product,
    isLiked: Boolean,
    isLoggedIn: Boolean = false,
    onBackClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onStartChat: (String) -> Unit,
    products: List<Product> = emptyList(),
    onProductClick: (String) -> Unit = {},
    sellerReviews: List<Review>,
    onAddReview: (reviewerName: String, rating: Int, comment: String) -> Unit,
    onUpdateProduct: (Product) -> Unit = {},
    onDeleteProduct: (Product) -> Unit = {}
) {
    val context = LocalContext.current
    var activeReadingArticle by remember { mutableStateOf<VibroArticle?>(null) }
    var showSellerReviewsDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var showEditDialog by remember { mutableStateOf(false) }
    var showQuickChatDialog by remember { mutableStateOf(false) }
    var showFullScreenPhotoViewer by remember { mutableStateOf(false) }

    if (showFullScreenPhotoViewer) {
        FullScreenImageGallery(
            images = getProductImages(product),
            initialIndex = 0,
            onDismiss = { showFullScreenPhotoViewer = false }
        )
    }

    if (activeReadingArticle != null) {
        val article = activeReadingArticle!!
        Dialog(onDismissRequest = { activeReadingArticle = null }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = article.icon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Safe Buying Guide",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = article.rTime,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = { activeReadingArticle = null }) {
                            Icon(Icons.Default.Close, contentDescription = "Close Article")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(12.dp))

                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = article.fullContent,
                            style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Never send money or compromise safety. Conduct transactions in busy public spots and verify properties thoroughly.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { activeReadingArticle = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("I Understand, Got It", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Core Topbar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBackClick, modifier = Modifier.testTag("back_button")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Details", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }

                Row {
                    IconButton(onClick = onFavoriteToggle) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save Status",
                            tint = if (isLiked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = {
                        Toast.makeText(context, "Url Link copied!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            }
        }

        // Scroll Area Content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { showFullScreenPhotoViewer = true }
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(product.categoryId, product.title)),
                    placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(product.categoryId, product.title))
                )

                if (product.isPromoted) {
                    Box(
                        modifier = Modifier
                            .padding(12.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(20.dp))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text("PROMOTED", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                Text(product.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(product.price, style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)

                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(product.location, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("Specification State", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Condition", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(product.condition, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(16.dp))

                Text("About This Item", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(product.description, fontSize = 14.sp, lineHeight = 20.sp, color = MaterialTheme.colorScheme.onSurface)

                Spacer(modifier = Modifier.height(24.dp))

                // Seller Detail Box
                val reviewsForSeller = sellerReviews.filter { it.sellerName == product.sellerName }
                val avgRating = if (reviewsForSeller.isEmpty()) 5.0f else reviewsForSeller.map { it.rating }.average().toFloat()
                val totalRatings = reviewsForSeller.size

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showSellerReviewsDialog = true }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(product.sellerName.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(product.sellerName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verified Vibro Seller", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(5) { starIndex ->
                                    val isFilled = starIndex < Math.round(avgRating)
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (isFilled) Color(0xFFFBBF24) else Color.LightGray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = String.format("%.1f (%d reviews)", avgRating, totalRatings),
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                SellerReviewsDialog(
                    visible = showSellerReviewsDialog,
                    onDismiss = { showSellerReviewsDialog = false },
                    sellerName = product.sellerName,
                    sellerReviews = sellerReviews,
                    onAddReview = { reviewerName, rating, comment ->
                        onAddReview(reviewerName, rating, comment)
                    }
                )

                // RELATED ARTICLES & USER SAFETY GUIDES
                val catArticles = mockArticlesByCat[product.categoryId] ?: emptyList()
                if (catArticles.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(18.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Buying Guides & Advisor Articles",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Expert tips verified by Vibro safety team specifically for " +
                           (mockCategories.find { it.id == product.categoryId }?.name ?: "this category") + ".",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        catArticles.forEach { article ->
                            Card(
                                modifier = Modifier
                                    .width(260.dp)
                                    .clickable { activeReadingArticle = article }
                                    .testTag("article_${article.id}"),
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = article.icon,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .background(MaterialTheme.colorScheme.secondaryContainer, RoundedCornerShape(8.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = article.rTime,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Text(
                                        text = article.title,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = article.excerpt,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        lineHeight = 16.sp
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.End,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Tap to read full guide →",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // RELATED ADS
                val relatedProducts = remember(product, products) {
                    products.filter { it.categoryId == product.categoryId && it.id != product.id }
                }

                if (relatedProducts.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Related Ads & Recommendations",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        relatedProducts.forEach { relProd ->
                            Card(
                                modifier = Modifier
                                    .width(160.dp)
                                    .clickable { onProductClick(relProd.id) }
                                    .testTag("related_product_${relProd.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                            ) {
                                Column {
                                    Box(modifier = Modifier.fillMaxWidth().height(100.dp)) {
                                        AsyncImage(
                                            model = relProd.imageUrl,
                                            contentDescription = relProd.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier.fillMaxSize(),
                                            error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(relProd.categoryId, relProd.title)),
                                            placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(relProd.categoryId, relProd.title))
                                        )
                                        if (relProd.isPromoted) {
                                            Box(
                                                modifier = Modifier
                                                    .padding(6.dp)
                                                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                            ) {
                                                Text("PREMIUM", color = Color.White, fontSize = 7.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = relProd.title,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = relProd.price,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = relProd.location,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Bottom CTA contact board
        Surface(
            shadowElevation = 8.dp,
            color = MaterialTheme.colorScheme.surface
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (product.sellerName == "You") {
                    Button(
                        onClick = {
                            showEditDialog = true
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Edit Listing", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            onDeleteProduct(product)
                            coroutineScope.launch {
                                if (NetworkManager.isSupabaseConfigured) {
                                    try {
                                        val idQuery = "eq.${product.id}"
                                        try {
                                            NetworkManager.supabaseApi?.deleteAd(idQuery)
                                        } catch (e: Exception) {
                                            NetworkManager.supabaseApi?.deleteProduct(idQuery)
                                        }
                                        android.util.Log.i("MarketplaceApp", "Deleted ad with ID ${product.id} from Supabase")
                                    } catch (e: Exception) {
                                        android.util.Log.e("MarketplaceApp", "Failed to delete ad from Supabase: ${NetworkManager.getErrorMessage(e)}")
                                    }
                                }
                            }
                            Toast.makeText(context, "Listing removed successfully!", Toast.LENGTH_SHORT).show()
                            onBackClick()
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Delete Listing", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = {
                            Toast.makeText(context, "Dialing seller: +251 911 223 344", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier.weight(1f).height(48.dp).testTag("call_seller_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Call Seller", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            if (!isLoggedIn) {
                                onStartChat("")
                            } else {
                                showQuickChatDialog = true
                            }
                        },
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Chat, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Chat Now", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        if (showQuickChatDialog) {
            QuickChatDialog(
                sellerName = product.sellerName,
                productTitle = product.title,
                onDismiss = { showQuickChatDialog = false },
                onSend = { message ->
                    onStartChat(message)
                    showQuickChatDialog = false
                    Toast.makeText(context, "Message sent successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }

        if (showEditDialog) {
            var currentProductState by remember { mutableStateOf(product) }
            EditAdDialog(
                product = currentProductState,
                onDismiss = { showEditDialog = false },
                onSave = { title, price, categoryId, location, condition, description, imageUrl ->
                    val cleanPriceNum = price.replace("[^\\d]".toRegex(), "")
                    val formattedPrice = "Br " + if (cleanPriceNum.isNotEmpty()) {
                        String.format("%,d", cleanPriceNum.toLong())
                    } else {
                        "0"
                    }
                    val finalImgUrl = if (imageUrl.trim().isNotEmpty()) imageUrl else {
                        NetworkManager.getFallbackImageUrl(categoryId, title)
                    }

                    val updatedProd = currentProductState.copy(
                        title = title,
                        price = formattedPrice,
                        categoryId = categoryId,
                        location = location,
                        condition = condition,
                        description = description,
                        imageUrl = finalImgUrl
                    )

                    onUpdateProduct(updatedProd)
                    currentProductState = updatedProd

                    coroutineScope.launch {
                        if (NetworkManager.isSupabaseConfigured) {
                            try {
                                val payload = NetworkManager.buildInsertPayload(updatedProd, NetworkManager.adTableKeys)
                                val idQuery = "eq.${updatedProd.id}"
                                try {
                                    NetworkManager.supabaseApi?.updateAd(idQuery, payload)
                                    android.util.Log.i("MarketplaceApp", "Updated ad in Supabase ads table")
                                } catch (e: Exception) {
                                    val prodPayload = NetworkManager.buildInsertPayload(updatedProd, NetworkManager.productTableKeys)
                                    NetworkManager.supabaseApi?.updateProduct(idQuery, prodPayload)
                                    android.util.Log.i("MarketplaceApp", "Updated product in Supabase products table")
                                }
                                NetworkManager.syncErrorMessage = null
                            } catch (e: Exception) {
                                android.util.Log.e("MarketplaceApp", "Failed to sync updated ad to Supabase: ${NetworkManager.getErrorMessage(e)}")
                                NetworkManager.syncErrorMessage = "Failed to sync updated ad: ${NetworkManager.getErrorMessage(e)}"
                            }
                        }
                    }

                    showEditDialog = false
                    Toast.makeText(context, "Listing updated successfully!", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}

// --- Product Card Grid Cell ---
@Composable
fun ProductGridCard(
    product: Product,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(190.dp)
            .clickable(onClick = onClick)
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
            ) {
                AsyncImage(
                    model = product.imageUrl,
                    contentDescription = product.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(product.categoryId, product.title)),
                    placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(product.categoryId, product.title))
                )

                if (product.isPromoted) {
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text("AD", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(product.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(product.price, fontSize = 13.sp, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(10.dp))
                Spacer(modifier = Modifier.width(2.dp))
                Text(getRelativeTime(product), fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// --- Common Jiji Bottom Navigation Bar ---
@Composable
fun MarketplaceBottomNav(
    activeTab: String,
    onTabSelected: (String) -> Unit,
    onPostAdClick: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomCenter) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
            shadowElevation = 8.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomNavItem(Icons.Default.Home, "Home", activeTab == "home") { onTabSelected("home") }
                BottomNavItem(Icons.Default.FavoriteBorder, "Saved", activeTab == "saved") { onTabSelected("saved") }
                Spacer(modifier = Modifier.width(52.dp))
                BottomNavItem(Icons.Default.Forum, "Chats", activeTab == "chats") { onTabSelected("chats") }
                BottomNavItem(Icons.Default.Person, "Profile", activeTab == "profile") { onTabSelected("profile") }
            }
        }

        // Custom Jiji central FAB (+ Sell)
        Box(
            modifier = Modifier
                .padding(bottom = 20.dp)
                .size(54.dp)
                .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(16.dp))
                .border(3.dp, MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                .clickable(onClick = onPostAdClick)
                .testTag("post_ad_fab"),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Add, contentDescription = "Post Ad", tint = Color.White, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val activeColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
    val activeWeight = if (selected) FontWeight.Bold else FontWeight.Medium

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(icon, contentDescription = label, tint = activeColor, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = activeWeight,
            color = activeColor
        )
    }
}

// --- Clickable Fields Row for drill-down options selection ---
@Composable
fun SelectFieldRow(
    label: String,
    selectedValue: String,
    placeholder: String = "Select option",
    onClick: () -> Unit
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedValue.ifEmpty { placeholder },
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (selectedValue.isNotEmpty()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// --- Sell Screen Dialog slide sheet code ---
@Composable
fun PostAdDialog(
    onDismiss: () -> Unit,
    onPublish: (title: String, price: String, categoryId: String, location: String, condition: String, description: String, imageUrl: String, isPromoted: Boolean) -> Unit
) {
    val context = LocalContext.current
    // 7-Step Funnel:
    // Step 1: Title Screen & Smart Prediction
    // Step 2: Category & Subcategory Selection
    // Step 3: Attribute Engine & Live SEO Title Suggester
    // Step 4: Price, Negotiability, Media & Description
    // Step 5: Location & Communication Toggles
    // Step 6: Boost Promotion Screen
    // Step 7: Automated Moderation & Review Animation

    var wizardStep by rememberSaveable { mutableStateOf(1) }
    var categoryId by rememberSaveable { mutableStateOf("2") } // Defaults to Phones
    var subcategory by rememberSaveable { mutableStateOf("Mobile Phones") }

    // Navigation and Selector views inside the wizard steps
    var currentSubView by rememberSaveable { mutableStateOf("main") } // "main", "select_category", "select_subcategory", "selector"
    var selectedCityPostAd by remember { mutableStateOf<CityLocation?>(null) }
    var selectorTitle by rememberSaveable { mutableStateOf("") }
    var selectorOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectorSelectedValue by rememberSaveable { mutableStateOf("") }
    var selectorTargetProperty by rememberSaveable { mutableStateOf("") }

    // Dynamic Specifications States
    // Vehicles (ID "1")
    var vehicleBrand by rememberSaveable { mutableStateOf("") }
    var vehicleModel by rememberSaveable { mutableStateOf("") }
    var vehicleYear by rememberSaveable { mutableStateOf("") }
    var vehicleTransmission by rememberSaveable { mutableStateOf("Automatic") }
    var vehicleFuel by rememberSaveable { mutableStateOf("Petrol") }

    // Phones (ID "2")
    var phoneBrand by rememberSaveable { mutableStateOf("") }
    var phoneStorage by rememberSaveable { mutableStateOf("") }
    var phoneRam by rememberSaveable { mutableStateOf("") }
    var phoneColor by rememberSaveable { mutableStateOf("") }

    // Property (ID "3")
    var propertyBedrooms by rememberSaveable { mutableStateOf("") }
    var propertyBathrooms by rememberSaveable { mutableStateOf("") }
    var propertyFurnishing by rememberSaveable { mutableStateOf("Unfurnished") }

    // Fashion (ID "4")
    var fashionGender by rememberSaveable { mutableStateOf("Unisex") }
    var fashionSize by rememberSaveable { mutableStateOf("") }
    var fashionBrand by rememberSaveable { mutableStateOf("") }

    // Furniture (ID "5")
    var furnitureMaterial by rememberSaveable { mutableStateOf("") }
    var furnitureBrand by rememberSaveable { mutableStateOf("") }

    // Electronics (ID "6")
    var elecBrand by rememberSaveable { mutableStateOf("") }
    var elecType by rememberSaveable { mutableStateOf("") }

    // Health & Beauty (ID "7")
    var beautyBrand by rememberSaveable { mutableStateOf("") }
    var beautyType by rememberSaveable { mutableStateOf("") }

    // Services (ID "8")
    var serviceType by rememberSaveable { mutableStateOf("") }
    var serviceExperience by rememberSaveable { mutableStateOf("") }

    // Jobs (ID "9")
    var jobType by rememberSaveable { mutableStateOf("") }
    var jobExperience by rememberSaveable { mutableStateOf("") }

    // Animals & Pets (ID "10")
    var petType by rememberSaveable { mutableStateOf("") }
    var petAge by rememberSaveable { mutableStateOf("") }

    // Agriculture & Food (ID "11")
    var agriType by rememberSaveable { mutableStateOf("") }
    var agriUnit by rememberSaveable { mutableStateOf("") }

    // Core attributes
    var title by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var isNegotiable by rememberSaveable { mutableStateOf(true) }
    var condition by rememberSaveable { mutableStateOf("New") }
    var location by rememberSaveable { mutableStateOf("Addis Ababa, Bole") }
    var imageUrl by rememberSaveable { mutableStateOf("") }
    var description by rememberSaveable { mutableStateOf("") }

    // Communication settings (Step 5)
    var showPhoneNumber by rememberSaveable { mutableStateOf(true) }
    var allowVibroChats by rememberSaveable { mutableStateOf(true) }
    var allowWhatsApp by rememberSaveable { mutableStateOf(true) }

    // Premium Boost Tier: "free", "silver", "diamond"
    var selectedPremiumTier by rememberSaveable { mutableStateOf("silver") }

    // Constants Lists
    val vehicleSubs = listOf(
        "Cars", "Motorcycles & Scooters", "Auto Parts & Acc.", "Trucks", 
        "Buses & Microbuses", "Heavy Equipment", "Boats & Watercraft", 
        "Tractors & Agricultural Vehicles", "Car Rental Services", "Bicycles", 
        "Tires & Rims", "Other Vehicles"
    )
    val phoneSubs = listOf(
        "Mobile Phones", "Tablets", "Smartwatches", "Mobile Phone Accessories", 
        "Tablets Accessories", "Smartwatch Accessories", "Memory Cards", 
        "Power Banks & Chargers", "Cases & Screen Protectors", "Smart Home Devices", 
        "Sim Cards & Credits", "Repair Parts"
    )
    val propertySubs = listOf(
        "Apartments for Rent", "Houses for Sale", "Land & Plots", "Commercial Property for Sale", 
        "Commercial Property for Rent", "Apartments for Sale", "Houses for Rent", 
        "Offices for Rent", "Shops & Retail for Rent", "Short-Let & Guest Houses", 
        "Real Estate Agent Services", "Warehouses"
    )
    val fashionSubs = listOf(
        "Clothing", "Shoes", "Watches & Jewelry", "Bags & Acc.", 
        "Underwear & Sleepwear", "Wedding Wear", "Traditional Clothing", 
        "Kids' Fashion & Wear", "Sunglasses & Eyewear", "Perfumes & Colognes", 
        "Beauty & Cosmetics Accessories", "Activewear"
    )
    val furnitureSubs = listOf(
        "Living Room Furniture", "Kitchen & Dining", "Office Furniture", "Home Appliances", 
        "Bedroom Furniture", "Bathroom Furniture", "Outdoor & Garden", "Lighting & Fixtures", 
        "Rugs & Carpets", "Curtains & Blinds", "Decor & Home Accents", "Storage & Shelving"
    )
    val electronicSubs = listOf(
        "TVs & Home Theatre", "Laptops & Computers", "Audio & Speakers", "Video Games & Consoles", 
        "Cameras", "Computer Hardware & RAM", "Printers & Scanners", "Photocopiers", 
        "Projectors & Screens", "Networking Products", "Software & Licenses", "Electronic Security & CCTV"
    )
    val healthBeautySubs = listOf(
        "Cosmetics", "Fragrances", "Hair Care", "Skin Care", "Makeup", 
        "Vitamins & Supplements", "Sexual Wellness", "Oral Care", "Bath & Body", 
        "Medical Supplies & Equipment", "Tools & Accessories", "Salon & Massage Services"
    )
    val serviceSubs = listOf(
        "Repair Services", "Cleaning Services", "Transport & Delivery", "Event Planning & Catering", 
        "Tuition & Classes", "Computer & IT Support", "Legal & Business Advisory", "Construction & Handyman", 
        "Translation & Writing", "Marketing & SEO Services", "Photography & Video", "Healthcare & Gym Personal Trainer"
    )
    val jobSubs = listOf(
        "Full-time Jobs", "Part-time Jobs", "Remote Jobs", "Internships", 
        "Contract Jobs", "Freelance Work", "Commission-based Sales Jobs", "Part-time Tutoring Jobs", 
        "Temporary / Event Jobs", "Apprenticeships", "Volunteering Roles", "Executive Positions"
    )
    val animalPetSubs = listOf(
        "Dogs", "Cats", "Birds", "Pet Food & Acc.", 
        "Aquarium & Fish", "Reptiles", "Rabbits & Small Rodents", "Poultry & Farm Animals", 
        "Vet Services & Care", "Pet Grooming", "Pet Boarding & Sitting", "Horses & Equestrian"
    )
    val agricultureFoodSubs = listOf(
        "Farm Products", "Farm Machinery", "Grains & Seeds", "Fresh Food & Groceries", 
        "Fertilizers & Soil Care", "Livestock & Poultry Feed", "Beekeeping Supplies", "Irrigation & Watering", 
        "Horticulture & Ornamental Plants", "Organic Foods", "Butchery & Meat Products", "Spices & Condiments"
    )

    val carBrands = listOf("Toyota", "Hyundai", "Suzuki", "Mercedes-Benz", "BYD", "Lifan", "Nissan", "Ford", "Honda", "BMW", "Audi", "Chevrolet", "Kia", "Mitsubishi", "Peugeot", "Volkswagen", "Isuzu")
    val phoneBrands = listOf("Apple", "Samsung", "Xiaomi", "Tecno", "Infinix", "Google", "Huawei", "Oppo", "Vivo", "OnePlus", "Realme", "Nokia", "Motorola", "Itel")
    val storageOptions = listOf("16GB", "32GB", "64GB", "128GB", "256GB", "512GB", "1TB")
    val ramOptions = listOf("2GB", "3GB", "4GB", "6GB", "8GB", "12GB", "16GB", "24GB")
    val bedroomOptions = listOf("Studio", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10+")
    val bathroomOptions = listOf("1", "2", "3", "4", "5", "6+")
    val furnishingOptions = listOf("Furnished", "Unfurnished", "Semi-Furnished")
    val fashionGenders = listOf("Men's", "Women's", "Unisex", "Kids Boy", "Kids Girl")
    val furnitureMaterials = listOf("Wood", "Leather", "Fabric", "Metal", "Glass", "Plastic", "Bamboo", "Marble")

    val electronicBrands = listOf("HP", "Dell", "Lenovo", "Apple", "Sony", "Samsung", "Canon", "LG", "Xiaomi", "Asus", "Acer", "Toshiba", "Nikon", "Panasonic", "JBL", "Bose")
    val electronicTypes = listOf("Laptop", "Television", "Smartphone", "Camera", "Audio & Speaker", "Video Games & Consoles", "Desktop PC", "Smartwatch", "Tablet", "Projector", "Headphone")
    val beautyBrands = listOf("Nivea", "CeraVe", "Dior", "Chanel", "MAC", "Fenty Beauty", "The Ordinary", "Colgate", "Gillette", "L'Oreal", "Vaseline", "Estee Lauder")
    val beautyTypes = listOf("Skincare", "Makeup", "Fragrance", "Haircare", "Personal Care", "Cosmetics", "Nutritional Supplements", "Salon Equipment")
    val serviceTypesList = listOf("Home Appliance Repair", "House Cleaning", "Cargo & Moving", "Home Tutor", "Beauty Salon Service", "Car Rent & Transport", "Web Development", "Plumbing & Electrical", "Legal Consulting", "Painting & Renovation")
    val serviceExperiences = listOf("Under 1 Year", "1-3 Years", "3-5 Years", "5-10 Years", "10+ Years")
    val jobTypesList = listOf("Full-time", "Part-time", "Contract / Project", "Internship", "Freelance", "Commission")
    val jobExperiences = listOf("Entry Level / No Experience", "1-2 Years", "3-5 Years", "5-10 Years", "10+ Years")
    val petTypesList = listOf("Dogs", "Cats", "Birds", "Fish", "Pet Accessories", "Rabbits", "Poultry", "Horses", "Veterinary Services")
    val petAges = listOf("Baby / Puppy / Kitten", "Young Adult", "Mature Adult", "Senior")
    val agriTypesList = listOf("Crop Seeds & Grains", "Fertilizer & Soil", "Farm Tractor & Tools", "Livestock & Poultry", "Pesticides & Chemicals", "Animal Feed", "Irrigation Systems")
    val agriUnits = listOf("Per Kilogram", "Per Quintal / Bag", "Per Litre", "Per Item / Head", "Per Ton", "Per Acre", "Per Package")

    val locations = listOf("Addis Ababa, Bole", "Addis Ababa, Kazanchis", "Addis Ababa, Piazza", "Addis Ababa, Sarbet", "Hawassa, Piassa")
    val conditions = listOf("New", "Used - Like New", "Used - Good", "Used - Fair")

    // Real-time local offline category prediction based on Title and Description
    val predictedCategory = remember(title, description) {
        if (title.isEmpty()) {
            null
        } else {
            val catId = CategoryMapper.mapTitleToCategoryId(title, description)
            mockCategories.find { it.id == catId }
        }
    }

    // Silent background automatic pre-fill assignment
    LaunchedEffect(predictedCategory) {
        predictedCategory?.let { category ->
            categoryId = category.id
            subcategory = when (category.id) {
                "1" -> "Cars"
                "2" -> "Mobile Phones"
                "3" -> "Apartments for Rent"
                "4" -> "Clothing"
                "5" -> "Living Room Furniture"
                "6" -> "TVs & Home Theatre"
                "7" -> "Cosmetics"
                "8" -> "Repair Services"
                "9" -> "Full-time Jobs"
                "10" -> "Dogs"
                "11" -> "Farm Products"
                else -> ""
            }
        }
    }

    // Live Offline Spam / Policy Banned Words Checker
    val spamWarningDetected = remember(title, description) {
        val combined = "$title $description".lowercase()
        combined.contains("scam") || combined.contains("illegal") || combined.contains("free money") || 
        combined.contains("lottery prize") || combined.contains("hacking tool") || combined.contains("make cash quick")
    }

    // Price rating warning
    val priceParse = price.toDoubleOrNull() ?: 0.0
    val pricingSanityWarningDetected = remember(price, categoryId) {
        price.isNotEmpty() && priceParse > 0.0 && priceParse < 100.0
    }

    // Dynamic Search Engine preview text compilation
    val seoStructuredTitle = remember(
        categoryId, subcategory, vehicleBrand, vehicleModel, vehicleYear,
        phoneBrand, phoneStorage, propertyBedrooms, propertyFurnishing,
        fashionGender, fashionSize, furnitureMaterial
    ) {
        when (categoryId) {
            "1" -> {
                val brand = vehicleBrand.ifEmpty { "Toyota" }
                val modelStr = if (vehicleModel.isNotEmpty()) " $vehicleModel" else "Vitz"
                val yearStr = if (vehicleYear.isNotEmpty()) " ($vehicleYear)" else ""
                "$brand$modelStr$yearStr"
            }
            "2" -> {
                val brand = phoneBrand.ifEmpty { "Apple" }
                val sub = if (subcategory.isNotEmpty() && subcategory != "Mobile Phones") subcategory else "iPhone"
                val storageStr = if (phoneStorage.isNotEmpty()) " $phoneStorage" else ""
                "$brand $sub$storageStr"
            }
            "3" -> {
                val bedroomsStr = if (propertyBedrooms.isNotEmpty()) "$propertyBedrooms Bed " else "2 Bed "
                val sub = if (subcategory.isNotEmpty()) subcategory else "Apartment"
                val furnishingStr = if (propertyFurnishing.isNotEmpty()) " ($propertyFurnishing)" else ""
                "$bedroomsStr$sub$furnishingStr"
            }
            "4" -> {
                val sub = if (subcategory.isNotEmpty()) subcategory else "Apparel"
                val genderStr = "$fashionGender "
                val sizeStr = if (fashionSize.isNotEmpty()) " size $fashionSize" else ""
                "$genderStr$sub$sizeStr"
            }
            "5" -> {
                val sub = if (subcategory.isNotEmpty()) subcategory else "Furniture Piece"
                val materialStr = if (furnitureMaterial.isNotEmpty()) " in $furnitureMaterial" else ""
                "$sub$materialStr"
            }
            else -> ""
        }
    }

    // Hardware Back gestures inside the Single Screen Form
    BackHandler(enabled = true) {
        if (currentSubView != "main") {
            currentSubView = "main"
        } else {
            onDismiss()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.98f)
                .padding(top = 10.dp)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Panel
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Publish New Listing",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Complete the details to post your ad across Ethiopia",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider()

                Spacer(modifier = Modifier.height(14.dp))

                // --- Render Sub-selection Screens if users clicked on selector fields ---
                if (currentSubView == "select_category") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Browse Main Classification Systems",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            mockCategories.forEach { cat ->
                                val isSel = categoryId == cat.id
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            categoryId = cat.id
                                            subcategory = when (cat.id) {
                                                "1" -> "Cars"
                                                "2" -> "Mobile Phones"
                                                "3" -> "Apartments for Rent"
                                                "4" -> "Clothing"
                                                "5" -> "Living Room Furniture"
                                                "6" -> "TVs & Home Theatre"
                                                "7" -> "Cosmetics"
                                                "8" -> "Repair Services"
                                                "9" -> "Full-time Jobs"
                                                "10" -> "Dogs"
                                                "11" -> "Farm Products"
                                                else -> ""
                                            }
                                            currentSubView = "main"
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) cat.iconColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(
                                        width = 1.dp,
                                        color = if (isSel) cat.iconColor else Color.Transparent
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier.size(40.dp).clip(CircleShape).background(cat.bgColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(cat.icon, contentDescription = null, tint = cat.iconColor, modifier = Modifier.size(20.dp))
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = cat.name,
                                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                                            )
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                } else if (currentSubView == "select_subcategory") {
                    val activeSubs = when (categoryId) {
                        "1" -> vehicleSubs
                        "2" -> phoneSubs
                        "3" -> propertySubs
                        "4" -> fashionSubs
                        "5" -> furnitureSubs
                        "6" -> electronicSubs
                        "7" -> healthBeautySubs
                        "8" -> serviceSubs
                        "9" -> jobSubs
                        "10" -> animalPetSubs
                        "11" -> agricultureFoodSubs
                        else -> listOf()
                    }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Browse Specific Subcategory Filters",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            activeSubs.forEach { sub ->
                                val isSel = subcategory == sub
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            subcategory = sub
                                            currentSubView = "main"
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = sub, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                } else if (currentSubView == "select_city") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Select City",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ethiopianCities.forEach { city ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCityPostAd = city
                                            currentSubView = "select_subcity"
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = city.name, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                } else if (currentSubView == "select_subcity") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = "Select Area in ${selectedCityPostAd?.name}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            selectedCityPostAd?.subLocations?.forEach { subcity ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            location = "${selectedCityPostAd?.name}, $subcity"
                                            currentSubView = "main"
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = subcity, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                } else if (currentSubView == "selector") {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Text(
                            text = selectorTitle,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                        Column(
                            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            selectorOptions.forEach { opt ->
                                val isSel = selectorSelectedValue == opt
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            when (selectorTargetProperty) {
                                                "vehicleBrand" -> vehicleBrand = opt
                                                "vehicleTransmission" -> vehicleTransmission = opt
                                                "vehicleFuel" -> vehicleFuel = opt
                                                "phoneBrand" -> phoneBrand = opt
                                                "phoneStorage" -> phoneStorage = opt
                                                "phoneRam" -> phoneRam = opt
                                                "propertyBedrooms" -> propertyBedrooms = opt
                                                "propertyBathrooms" -> propertyBathrooms = opt
                                                "propertyFurnishing" -> propertyFurnishing = opt
                                                "fashionGender" -> fashionGender = opt
                                                "furnitureMaterial" -> furnitureMaterial = opt
                                                "elecBrand" -> elecBrand = opt
                                                "elecType" -> elecType = opt
                                                "beautyBrand" -> beautyBrand = opt
                                                "beautyType" -> beautyType = opt
                                                "serviceType" -> serviceType = opt
                                                "serviceExperience" -> serviceExperience = opt
                                                "jobType" -> jobType = opt
                                                "jobExperience" -> jobExperience = opt
                                                "petType" -> petType = opt
                                                "petAge" -> petAge = opt
                                                "agriType" -> agriType = opt
                                                "agriUnit" -> agriUnit = opt
                                                "location" -> location = opt
                                                "condition" -> condition = opt
                                            }
                                            currentSubView = "main"
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                    border = BorderStroke(1.dp, if (isSel) MaterialTheme.colorScheme.primary else Color.Transparent)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(opt, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
                                        if (isSel) {
                                            Icon(Icons.Default.Check, contentDescription = "Active", tint = MaterialTheme.colorScheme.primary)
                                        } else {
                                            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // --- Standard Main View (Form fully integrated and scrollable) ---
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                            // --- Form Section 1: Basic Information ---
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "1. Basic Details",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("Ad Title (e.g. Toyota Vitz 2018 or iPhone 15 Pro)") },
                                    modifier = Modifier.fillMaxWidth().testTag("funnel_title_input"),
                                    singleLine = true,
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = MaterialTheme.colorScheme.primary
                                    )
                                )

                                val activeCat = mockCategories.find { it.id == categoryId }
                                val catLabel = activeCat?.name ?: "Tap to choose"

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        SelectFieldRow(
                                            label = "Category Theme",
                                            selectedValue = catLabel,
                                            placeholder = "Select Category",
                                            onClick = { currentSubView = "select_category" }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        SelectFieldRow(
                                            label = "Subcategory",
                                            selectedValue = subcategory,
                                            placeholder = "Select Subcategory",
                                            onClick = { currentSubView = "select_subcategory" }
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // --- Form Section 2: Attributes Engine ---
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "2. Specific Attributes",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Dynamic Form fields based on category
                                when (categoryId) {
                                    "1" -> { // Vehicles
                                        SelectFieldRow(
                                            label = "Vehicle Brand",
                                            selectedValue = vehicleBrand,
                                            placeholder = "Select Brand",
                                            onClick = {
                                                selectorTitle = "Select Vehicle Brand"
                                                selectorOptions = carBrands
                                                selectorSelectedValue = vehicleBrand
                                                selectorTargetProperty = "vehicleBrand"
                                                currentSubView = "selector"
                                            }
                                        )

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = vehicleModel,
                                                onValueChange = { vehicleModel = it },
                                                label = { Text("Model e.g. Vitz / Hilux") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = vehicleYear,
                                                onValueChange = { vehicleYear = it },
                                                label = { Text("Year e.g. 2021") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true,
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                            )
                                        }

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                SelectFieldRow(
                                                    label = "Transmission",
                                                    selectedValue = vehicleTransmission,
                                                    placeholder = "Select",
                                                    onClick = {
                                                        selectorTitle = "Select Transmission"
                                                        selectorOptions = listOf("Automatic", "Manual")
                                                        selectorSelectedValue = vehicleTransmission
                                                        selectorTargetProperty = "vehicleTransmission"
                                                        currentSubView = "selector"
                                                    }
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                SelectFieldRow(
                                                    label = "Fuel Type",
                                                    selectedValue = vehicleFuel,
                                                    placeholder = "Select",
                                                    onClick = {
                                                        selectorTitle = "Select Fuel Type"
                                                        selectorOptions = listOf("Petrol", "Diesel", "Electric", "Hybrid")
                                                        selectorSelectedValue = vehicleFuel
                                                        selectorTargetProperty = "vehicleFuel"
                                                        currentSubView = "selector"
                                                    }
                                                )
                                            }
                                        }
                                    }
                                    "2" -> { // Phones
                                        SelectFieldRow(
                                            label = "Phone Brand",
                                            selectedValue = phoneBrand,
                                            placeholder = "Select Brand",
                                            onClick = {
                                                selectorTitle = "Select Phone Brand"
                                                selectorOptions = phoneBrands
                                                selectorSelectedValue = phoneBrand
                                                selectorTargetProperty = "phoneBrand"
                                                currentSubView = "selector"
                                            }
                                        )

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                SelectFieldRow(
                                                    label = "Storage Capacity",
                                                    selectedValue = phoneStorage,
                                                    placeholder = "Select Storage",
                                                    onClick = {
                                                        selectorTitle = "Select Storage Capacity"
                                                        selectorOptions = storageOptions
                                                        selectorSelectedValue = phoneStorage
                                                        selectorTargetProperty = "phoneStorage"
                                                        currentSubView = "selector"
                                                    }
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                SelectFieldRow(
                                                    label = "RAM Size",
                                                    selectedValue = phoneRam,
                                                    placeholder = "Select RAM",
                                                    onClick = {
                                                        selectorTitle = "Select RAM Size"
                                                        selectorOptions = ramOptions
                                                        selectorSelectedValue = phoneRam
                                                        selectorTargetProperty = "phoneRam"
                                                        currentSubView = "selector"
                                                    }
                                                )
                                            }
                                        }

                                        OutlinedTextField(
                                            value = phoneColor,
                                            onValueChange = { phoneColor = it },
                                            label = { Text("Color e.g. Space Gray") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                    "3" -> { // Property
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Box(modifier = Modifier.weight(1f)) {
                                                SelectFieldRow(
                                                    label = "Rooms & Bed",
                                                    selectedValue = propertyBedrooms,
                                                    placeholder = "Select Bed",
                                                    onClick = {
                                                        selectorTitle = "Select Rooms & Bed"
                                                        selectorOptions = bedroomOptions
                                                        selectorSelectedValue = propertyBedrooms
                                                        selectorTargetProperty = "propertyBedrooms"
                                                        currentSubView = "selector"
                                                    }
                                                )
                                            }
                                            Box(modifier = Modifier.weight(1f)) {
                                                SelectFieldRow(
                                                    label = "Bathrooms",
                                                    selectedValue = propertyBathrooms,
                                                    placeholder = "Select Bath",
                                                    onClick = {
                                                        selectorTitle = "Select Bathrooms"
                                                        selectorOptions = bathroomOptions
                                                        selectorSelectedValue = propertyBathrooms
                                                        selectorTargetProperty = "propertyBathrooms"
                                                        currentSubView = "selector"
                                                    }
                                                )
                                            }
                                        }

                                        SelectFieldRow(
                                            label = "Furnishing Status",
                                            selectedValue = propertyFurnishing,
                                            placeholder = "Select Furnishing",
                                            onClick = {
                                                selectorTitle = "Select Furnishing Condition"
                                                selectorOptions = furnishingOptions
                                                selectorSelectedValue = propertyFurnishing
                                                selectorTargetProperty = "propertyFurnishing"
                                                currentSubView = "selector"
                                            }
                                        )
                                    }
                                    "4" -> { // Fashion
                                        SelectFieldRow(
                                            label = "Gender Segment",
                                            selectedValue = fashionGender,
                                            placeholder = "Select Gender",
                                            onClick = {
                                                selectorTitle = "Select Gender Group"
                                                selectorOptions = fashionGenders
                                                selectorSelectedValue = fashionGender
                                                selectorTargetProperty = "fashionGender"
                                                currentSubView = "selector"
                                            }
                                        )

                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            OutlinedTextField(
                                                value = fashionSize,
                                                onValueChange = { fashionSize = it },
                                                label = { Text("Size e.g. XL, 42") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                            OutlinedTextField(
                                                value = fashionBrand,
                                                onValueChange = { fashionBrand = it },
                                                label = { Text("Brand e.g. Nike") },
                                                modifier = Modifier.weight(1f),
                                                singleLine = true
                                            )
                                        }
                                    }
                                    "5" -> { // Furniture
                                        SelectFieldRow(
                                            label = "Material Type",
                                            selectedValue = furnitureMaterial,
                                            placeholder = "Select Material",
                                            onClick = {
                                                selectorTitle = "Select Material"
                                                selectorOptions = furnitureMaterials
                                                selectorSelectedValue = furnitureMaterial
                                                selectorTargetProperty = "furnitureMaterial"
                                                currentSubView = "selector"
                                            }
                                        )

                                        OutlinedTextField(
                                            value = furnitureBrand,
                                            onValueChange = { furnitureBrand = it },
                                            label = { Text("Manufacturer Brand") },
                                            modifier = Modifier.fillMaxWidth(),
                                            singleLine = true
                                        )
                                    }
                                    "6" -> { // Electronics
                                        SelectFieldRow(
                                            label = "Electronic Device Type",
                                            selectedValue = elecType,
                                            placeholder = "Select Type",
                                            onClick = {
                                                selectorTitle = "Select Electronic Device Type"
                                                selectorOptions = electronicTypes
                                                selectorSelectedValue = elecType
                                                selectorTargetProperty = "elecType"
                                                currentSubView = "selector"
                                            }
                                        )
                                        SelectFieldRow(
                                            label = "Electronic Brand",
                                            selectedValue = elecBrand,
                                            placeholder = "Select Brand",
                                            onClick = {
                                                selectorTitle = "Select Electronic Brand"
                                                selectorOptions = electronicBrands
                                                selectorSelectedValue = elecBrand
                                                selectorTargetProperty = "elecBrand"
                                                currentSubView = "selector"
                                            }
                                        )
                                    }
                                    "7" -> { // Health & Beauty
                                        SelectFieldRow(
                                            label = "Beauty Product Type",
                                            selectedValue = beautyType,
                                            placeholder = "Select Type",
                                            onClick = {
                                                selectorTitle = "Select Product Type"
                                                selectorOptions = beautyTypes
                                                selectorSelectedValue = beautyType
                                                selectorTargetProperty = "beautyType"
                                                currentSubView = "selector"
                                            }
                                        )
                                        SelectFieldRow(
                                            label = "Brand / Manufacturer",
                                            selectedValue = beautyBrand,
                                            placeholder = "Select Brand",
                                            onClick = {
                                                selectorTitle = "Select Brand"
                                                selectorOptions = beautyBrands
                                                selectorSelectedValue = beautyBrand
                                                selectorTargetProperty = "beautyBrand"
                                                currentSubView = "selector"
                                            }
                                        )
                                    }
                                    "8" -> { // Services
                                        SelectFieldRow(
                                            label = "Service Type",
                                            selectedValue = serviceType,
                                            placeholder = "Select Service Type",
                                            onClick = {
                                                selectorTitle = "Select Service Type"
                                                selectorOptions = serviceTypesList
                                                selectorSelectedValue = serviceType
                                                selectorTargetProperty = "serviceType"
                                                currentSubView = "selector"
                                            }
                                        )
                                        SelectFieldRow(
                                            label = "Service Experience Level",
                                            selectedValue = serviceExperience,
                                            placeholder = "Select Experience",
                                            onClick = {
                                                selectorTitle = "Select Experience Level"
                                                selectorOptions = serviceExperiences
                                                selectorSelectedValue = serviceExperience
                                                selectorTargetProperty = "serviceExperience"
                                                currentSubView = "selector"
                                            }
                                        )
                                    }
                                    "9" -> { // Jobs
                                        SelectFieldRow(
                                            label = "Job Type",
                                            selectedValue = jobType,
                                            placeholder = "Select Job Type",
                                            onClick = {
                                                selectorTitle = "Select Job Type"
                                                selectorOptions = jobTypesList
                                                selectorSelectedValue = jobType
                                                selectorTargetProperty = "jobType"
                                                currentSubView = "selector"
                                            }
                                        )
                                        SelectFieldRow(
                                            label = "Required Experience",
                                            selectedValue = jobExperience,
                                            placeholder = "Select Experience",
                                            onClick = {
                                                selectorTitle = "Select Required Experience"
                                                selectorOptions = jobExperiences
                                                selectorSelectedValue = jobExperience
                                                selectorTargetProperty = "jobExperience"
                                                currentSubView = "selector"
                                            }
                                        )
                                    }
                                    "10" -> { // Animals & Pets
                                        SelectFieldRow(
                                            label = "Pet Category Type",
                                            selectedValue = petType,
                                            placeholder = "Select Type",
                                            onClick = {
                                                selectorTitle = "Select Pet Type"
                                                selectorOptions = petTypesList
                                                selectorSelectedValue = petType
                                                selectorTargetProperty = "petType"
                                                currentSubView = "selector"
                                            }
                                        )
                                        SelectFieldRow(
                                            label = "Pet Age Group",
                                            selectedValue = petAge,
                                            placeholder = "Select Age Group",
                                            onClick = {
                                                selectorTitle = "Select Age Group"
                                                selectorOptions = petAges
                                                selectorSelectedValue = petAge
                                                selectorTargetProperty = "petAge"
                                                currentSubView = "selector"
                                            }
                                        )
                                    }
                                    "11" -> { // Agriculture & Food
                                        SelectFieldRow(
                                            label = "Agriculture Category Type",
                                            selectedValue = agriType,
                                            placeholder = "Select Type",
                                            onClick = {
                                                selectorTitle = "Select Agriculture Type"
                                                selectorOptions = agriTypesList
                                                selectorSelectedValue = agriType
                                                selectorTargetProperty = "agriType"
                                                currentSubView = "selector"
                                            }
                                        )
                                        SelectFieldRow(
                                            label = "Agri Pricing / Quantity Unit",
                                            selectedValue = agriUnit,
                                            placeholder = "Select Unit",
                                            onClick = {
                                                selectorTitle = "Select Pricing Unit"
                                                selectorOptions = agriUnits
                                                selectorSelectedValue = agriUnit
                                                selectorTargetProperty = "agriUnit"
                                                currentSubView = "selector"
                                            }
                                        )
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // --- Form Section 3: Media & Pricing details ---
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "3. Pricing, Photos & Description",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                        OutlinedTextField(
                                            value = price,
                                            onValueChange = { price = it },
                                            label = { Text("Price in Birr (Br)") },
                                            placeholder = { Text("Enter price (e.g. 150000)") },
                                            modifier = Modifier.fillMaxWidth().testTag("funnel_price_input"),
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            singleLine = true
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Is Price Negotiable?", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                Text("Let buyers counter-offer on chat rooms", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Switch(checked = isNegotiable, onCheckedChange = { isNegotiable = it })
                                        }
                                    }
                                }

                                // Suspicious Price alert warnings
                                if (pricingSanityWarningDetected) {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                                        border = BorderStroke(1.dp, Color(0xFFD97706)),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                            Icon(Icons.Default.Warning, contentDescription = "Low Price", tint = Color(0xFFD87706), modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "⚠️ Low Price Protection: Vibro automatically flags transactions below 100 Br to screen out potential decoy deposits. Increase value or verify status.",
                                                fontSize = 11.sp,
                                                color = Color.DarkGray
                                            )
                                        }
                                    }
                                }







                                // Image selection and upload systems removed as requested

                                // Description box
                                OutlinedTextField(
                                    value = description,
                                    onValueChange = { description = it },
                                    label = { Text("Product Description") },
                                    placeholder = { Text("Optional (If left blank, the product title will be used)") },
                                    modifier = Modifier.fillMaxWidth().height(100.dp)
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // --- Form Section 4: Location & Communication settings ---
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "4. Location, Condition & Availability",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Box(modifier = Modifier.weight(1f)) {
                                        SelectFieldRow(
                                            label = "Official Area Location",
                                            selectedValue = location,
                                            placeholder = "Select area",
                                            onClick = {
                                                currentSubView = "select_city"
                                            }
                                        )
                                    }
                                    Box(modifier = Modifier.weight(1f)) {
                                        SelectFieldRow(
                                            label = "Condition Standard",
                                            selectedValue = condition,
                                            placeholder = "Select quality status",
                                            onClick = {
                                                selectorTitle = "Specify Listing Wear & Tear"
                                                selectorOptions = conditions
                                                selectorSelectedValue = condition
                                                selectorTargetProperty = "condition"
                                                currentSubView = "selector"
                                            }
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Card(
                                    shape = RoundedCornerShape(12.dp),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                                ) {
                                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Show Phone Number", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Allows direct dial from buyers", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Switch(checked = showPhoneNumber, onCheckedChange = { showPhoneNumber = it })
                                        }

                                        HorizontalDivider()

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Allow App Chats", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("Enables secure inbox messenger conversations", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Switch(checked = allowVibroChats, onCheckedChange = { allowVibroChats = it })
                                        }

                                        HorizontalDivider()

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column {
                                                Text("Allow WhatsApp Inquiries", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                                Text("One-click redirect to WhatsApp mobile chats", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            }
                                            Switch(checked = allowWhatsApp, onCheckedChange = { allowWhatsApp = it })
                                        }
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // --- Form Section 5: Premium Boost Visibility ---
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    text = "5. Premium Boost Plans",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                // Free Card
                                val isFree = selectedPremiumTier == "free"
                                OutlinedCard(
                                    onClick = { selectedPremiumTier = "free" },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if (isFree) 2.dp else 1.dp, if (isFree) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    colors = CardDefaults.outlinedCardColors(containerColor = if (isFree) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = isFree, onClick = { selectedPremiumTier = "free" })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text("Free Plan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            Text("Standard listings, live for 30 days.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("0 Br", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                                    }
                                }

                                // Silver Card
                                val isSilver = selectedPremiumTier == "silver"
                                OutlinedCard(
                                    onClick = { selectedPremiumTier = "silver" },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if (isSilver) 2.dp else 1.dp, if (isSilver) Color(0xFF10B981) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    colors = CardDefaults.outlinedCardColors(containerColor = if (isSilver) Color(0xFFE6F4EA).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = isSilver, onClick = { selectedPremiumTier = "silver" })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Silver Booster 🚀", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF10B981))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(modifier = Modifier.background(Color(0xFFE6F4EA), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                    Text("5X TRAFFIC", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                                                }
                                            }
                                            Text("Special highlights in categories for 7 days.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("499 Br", fontWeight = FontWeight.ExtraBold, color = Color(0xFF10B981))
                                    }
                                }

                                // Diamond Card
                                val isDiamond = selectedPremiumTier == "diamond"
                                OutlinedCard(
                                    onClick = { selectedPremiumTier = "diamond" },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(if (isDiamond) 2.dp else 1.dp, if (isDiamond) Color(0xFFD97706) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                                    colors = CardDefaults.outlinedCardColors(containerColor = if (isDiamond) Color(0xFFFEF3C7).copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                                ) {
                                    Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                        RadioButton(selected = isDiamond, onClick = { selectedPremiumTier = "diamond" })
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text("Diamond Ultra VIP 💎", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFFD97706))
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(modifier = Modifier.background(Color(0xFFFEF3C7), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp, vertical = 2.dp)) {
                                                    Text("15X POWER", fontSize = 8.sp, fontWeight = FontWeight.Black, color = Color(0xFFD97706))
                                                }
                                            }
                                            Text("Pinned at home page topmost section for 15 days.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        Text("1499 Br", fontWeight = FontWeight.ExtraBold, color = Color(0xFFD97706))
                                    }
                                }
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                            // --- Form Section 6: Real-time Compliance Checks & Health Status ---
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                Text(
                                    text = "6. Real-time Ad Compliance Monitoring",
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(14.dp),
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        // Policy/Spam word check status
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            Icon(
                                                imageVector = if (!spamWarningDetected) Icons.Default.CheckCircle else Icons.Default.Cancel,
                                                contentDescription = null,
                                                tint = if (!spamWarningDetected) Color(0xFF16A34A) else Color.Red,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = if (!spamWarningDetected) "Automated Policy Scan Passed" else "Automated Policy Warning: Red-flagged words!",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (!spamWarningDetected) Color(0xFF16A34A) else Color.Red
                                            )
                                        }

                                        // Pricing sanity status
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            Icon(
                                                imageVector = if (!pricingSanityWarningDetected) Icons.Default.CheckCircle else Icons.Default.Warning,
                                                contentDescription = null,
                                                tint = if (!pricingSanityWarningDetected) Color(0xFF16A34A) else Color(0xFFD97706),
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = if (!pricingSanityWarningDetected) "Product Price Sanity Audited (Safe)" else "Product Price Warning: Potential Deposit Decoy",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (!pricingSanityWarningDetected) Color(0xFF16A34A) else Color(0xFFD97706)
                                            )
                                        }

                                        // Image format verification status
                                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                            val hasImage = imageUrl.trim().isNotEmpty()
                                            Icon(
                                                imageVector = if (hasImage) Icons.Default.CheckCircle else Icons.Default.Info,
                                                contentDescription = null,
                                                tint = if (hasImage) Color(0xFF16A34A) else Color.Gray,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(
                                                text = if (hasImage) "Listing Image Configured" else "Listing Image Missing (Standard fallback applies)",
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (hasImage) Color(0xFF16A34A) else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Real-time compliance check warnings blocking pub if any
                            if (spamWarningDetected) {
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEE2E2)),
                                    border = BorderStroke(1.dp, Color.Red),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = "Blocked", tint = Color.Red, modifier = Modifier.size(24.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text("⚠️ Cannot Publish Ad", fontWeight = FontWeight.Bold, color = Color.Red, fontSize = 13.sp)
                                            Text(
                                                text = "Forbidden policy keywords are found in your inputs. Please check your title or description.",
                                                color = Color.DarkGray,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }

                            // --- Publish / Submit Ad Button ---
                            val isFormValid = title.trim().isNotEmpty() && price.trim().isNotEmpty() && location.trim().isNotEmpty() && !spamWarningDetected
                            
                            if (!isFormValid && !spamWarningDetected) {
                                Text(
                                    text = "Please fill in all required fields (Title, Price, Location) to publish your ad.",
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            
                            Button(
                                onClick = {
                                    val trimTitle = title.trim()
                                    val trimPrice = price.trim()
                                    val specsBuilder = StringBuilder()

                                    when (categoryId) {
                                        "1" -> {
                                            specsBuilder.append("🌐 Category: Vehicles (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (vehicleBrand.isNotEmpty()) specsBuilder.append("🚘 Brand: $vehicleBrand\n")
                                            if (vehicleModel.isNotEmpty()) specsBuilder.append("📋 Model: $vehicleModel\n")
                                            if (vehicleYear.isNotEmpty()) specsBuilder.append("📅 Year: $vehicleYear\n")
                                            specsBuilder.append("⚙️ Transmission: $vehicleTransmission\n")
                                            specsBuilder.append("⛽ Fuel: $vehicleFuel\n")
                                        }
                                        "2" -> {
                                            specsBuilder.append("🌐 Category: Electronics (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (phoneBrand.isNotEmpty()) specsBuilder.append("📱 Brand: $phoneBrand\n")
                                            if (phoneStorage.isNotEmpty()) specsBuilder.append("💾 Storage: $phoneStorage\n")
                                            if (phoneRam.isNotEmpty()) specsBuilder.append("⚡ RAM: $phoneRam\n")
                                            if (phoneColor.isNotEmpty()) specsBuilder.append("🎨 Color: $phoneColor\n")
                                        }
                                        "3" -> {
                                            specsBuilder.append("🌐 Category: Real Estate (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (propertyBedrooms.isNotEmpty()) specsBuilder.append("🛏️ Bedrooms: $propertyBedrooms\n")
                                            if (propertyBathrooms.isNotEmpty()) specsBuilder.append("🚿 Bathrooms: $propertyBathrooms\n")
                                            specsBuilder.append("🛋️ Furnishing: $propertyFurnishing\n")
                                        }
                                        "4" -> {
                                            specsBuilder.append("🌐 Category: Fashion/Apparel (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            specsBuilder.append("🚻 Gender: $fashionGender\n")
                                            if (fashionSize.isNotEmpty()) specsBuilder.append("📏 Size: $fashionSize\n")
                                            if (fashionBrand.isNotEmpty()) specsBuilder.append("🏷️ Apparel Brand: $fashionBrand\n")
                                        }
                                        "5" -> {
                                            specsBuilder.append("🌐 Category: Furnishing (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (furnitureMaterial.isNotEmpty()) specsBuilder.append("🪵 Material: $furnitureMaterial\n")
                                            if (furnitureBrand.isNotEmpty()) specsBuilder.append("🏷️ Brand: $furnitureBrand\n")
                                        }
                                        "6" -> {
                                            specsBuilder.append("🌐 Category: Electronics (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (elecType.isNotEmpty()) specsBuilder.append("💻 Device Type: $elecType\n")
                                            if (elecBrand.isNotEmpty()) specsBuilder.append("🏷️ Brand: $elecBrand\n")
                                        }
                                        "7" -> {
                                            specsBuilder.append("🌐 Category: Health & Beauty (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (beautyType.isNotEmpty()) specsBuilder.append("💅 Product Type: $beautyType\n")
                                            if (beautyBrand.isNotEmpty()) specsBuilder.append("🌸 Brand/Line: $beautyBrand\n")
                                        }
                                        "8" -> {
                                            specsBuilder.append("🌐 Category: Services (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (serviceType.isNotEmpty()) specsBuilder.append("🛠️ Service Offered: $serviceType\n")
                                            if (serviceExperience.isNotEmpty()) specsBuilder.append("💼 Experience: $serviceExperience\n")
                                        }
                                        "9" -> {
                                            specsBuilder.append("🌐 Category: Jobs (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (jobType.isNotEmpty()) specsBuilder.append("👔 Job Type: $jobType\n")
                                            if (jobExperience.isNotEmpty()) specsBuilder.append("🎓 Experience Required: $jobExperience\n")
                                        }
                                        "10" -> {
                                            specsBuilder.append("🌐 Category: Animals & Pets (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (petType.isNotEmpty()) specsBuilder.append("🐶 Pet Type: $petType\n")
                                            if (petAge.isNotEmpty()) specsBuilder.append("📅 Age Level: $petAge\n")
                                        }
                                        "11" -> {
                                            specsBuilder.append("🌐 Category: Agriculture & Food (")
                                            specsBuilder.append(subcategory)
                                            specsBuilder.append(")\n")
                                            if (agriType.isNotEmpty()) specsBuilder.append("🚜 Agriculture Type: $agriType\n")
                                            if (agriUnit.isNotEmpty()) specsBuilder.append("📦 Measure Unit: $agriUnit\n")
                                        }
                                    }

                                    val finalDescription = if (specsBuilder.isNotEmpty()) {
                                        val descPart = if (description.trim().isNotEmpty()) "\n📝 AD DESCRIPTION:\n" + description else ""
                                        "📌 SPECIFICATIONS:\n" + specsBuilder.toString() + descPart
                                    } else {
                                        description
                                    }

                                    onPublish(
                                        trimTitle,
                                        trimPrice,
                                        categoryId,
                                        location,
                                        condition,
                                        finalDescription,
                                        imageUrl,
                                        selectedPremiumTier != "free"
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp)
                                    .testTag("submit_ad_button"),
                                enabled = isFormValid,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Publish Ad Live Now", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                }
            }
        }
    }
}

@Composable
fun VerificationStatusRow(label: String, checked: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(if (checked) Color(0xFFF0FDF4) else Color.Transparent)
            .padding(10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (checked) Color(0xFF16A34A) else Color.Gray.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (checked) Icons.Default.Check else Icons.Default.Circle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(label, style = MaterialTheme.typography.bodyMedium, color = if (checked) Color(0xFF16A34A) else Color.Gray)
        }
        if (checked) {
            Text("PASSED", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
        } else {
            Text("SCANNING...", fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = Color.Gray)
        }
    }
}

// --- Welcome Splash Screen ---
@Composable
fun SplashScreen(onNavigationNext: () -> Unit) {
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200)
        onNavigationNext()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF1E293B), // Slate 800
                        Color(0xFF0F172A)  // Slate 900
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Elegant pulsing icon container
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.3f), RoundedCornerShape(24.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Campaign,
                    contentDescription = "Vibro Logo",
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(54.dp)
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Vibro",
                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Black),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Ethiopia's Dynamic Marketplace",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(64.dp))
            CircularProgressIndicator(
                color = Color(0xFF3B82F6),
                modifier = Modifier.size(28.dp),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading amazing deals...",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.5f)
            )
        }
    }
}

// --- Welcome / Onboarding Screen ---
@Composable
fun WelcomeOnboardingScreen(
    onGetStarted: () -> Unit,
    onSkip: () -> Unit
) {
    var currentPage by remember { mutableStateOf(0) }
    
    val onboardingData = listOf(
        Triple(
            Icons.Default.Storefront,
            "Buy & Sell Direct",
            "Instantly post your vehicles, smartphones, properties, or fashion. Connect directly with direct local buyers in Addis Ababa, Hawassa, and beyond."
        ),
        Triple(
            Icons.Default.FlashOn,
            "Premium Booster Tiers",
            "Get up to 10x more exposure! Upgrade your listings to Silver or Diamond booster tiers for high visibility on the search feed."
        ),
        Triple(
            Icons.Default.Forum,
            "Direct Secure Chats",
            "Negotiate prices and secure details safely with the built-in real-time seller and buyer messaging. No shared phone number needed."
        )
    )

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onSkip) {
                    Text(
                        text = "Skip",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val (icon, title, desc) = onboardingData[currentPage]

            // Top visual graphic panel
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(54.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = desc,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
            }

            // Indicator dots and Action button at bottom
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Indicator dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    for (i in 0..2) {
                        val isActive = i == currentPage
                        val width = if (isActive) 24.dp else 8.dp
                        val color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        Box(
                            modifier = Modifier
                                .size(height = 8.dp, width = width)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }

                // Action buttons
                Button(
                    onClick = {
                        if (currentPage < 2) {
                            currentPage++
                        } else {
                            onGetStarted()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (currentPage == 2) "Get Started" else "Next",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// --- Sign-In / Login Screen ---
@Composable
fun LoginScreen(
    onLoginSuccess: (email: String, password: String, name: String, city: String) -> Unit,
    onSkip: () -> Unit
) {
    var isSignUpMode by remember { mutableStateOf(false) }
    
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("Addis Ababa, Bole") }
    
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.statusBars)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onSkip) {
                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                }
                TextButton(onClick = onSkip) {
                    Text("Skip to Guest", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Main Core Fields
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
            ) {
                // Styled Tag/Logo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Campaign,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "VIBRO MARKETPLACE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = if (isSignUpMode) "Create an Account" else "Welcome Back",
                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Black),
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = if (isSignUpMode) 
                        "Join Vibro to post ads, manage offers, and chat securely." 
                        else "Sign in to list items, sync chats, and boost exposure.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 28.dp)
                )

                // Input fields inside card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (isSignUpMode) {
                            OutlinedTextField(
                                value = name,
                                onValueChange = { name = it },
                                label = { Text("Your Full Name") },
                                leadingIcon = { Icon(Icons.Default.Person, null) },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                        }

                        OutlinedTextField(
                            value = email,
                            onValueChange = { email = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Password (Min 6 Characters)") },
                            leadingIcon = { Icon(Icons.Default.Lock, null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        if (isSignUpMode) {
                            var showCityDropdown by remember { mutableStateOf(false) }
                            val cities = listOf("Addis Ababa", "Hawassa", "Adama", "Bahir Dar", "Dire Dawa")
                            
                            Box(modifier = Modifier.fillMaxWidth()) {
                                OutlinedTextField(
                                    value = city,
                                    onValueChange = { city = it },
                                    label = { Text("Your Location / City") },
                                    leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                                    trailingIcon = {
                                        IconButton(onClick = { showCityDropdown = true }) {
                                            Icon(Icons.Default.ArrowDropDown, null)
                                        }
                                    },
                                    readOnly = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                DropdownMenu(
                                    expanded = showCityDropdown,
                                    onDismissRequest = { showCityDropdown = false }
                                ) {
                                    cities.forEach { choice ->
                                        DropdownMenuItem(
                                            text = { Text(choice) },
                                            onClick = {
                                                city = choice
                                                showCityDropdown = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Login Button
                Button(
                    onClick = {
                        if (email.trim().isEmpty() || password.trim().isEmpty()) {
                            Toast.makeText(context, "Please fill in all core fields", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (password.length < 6) {
                            Toast.makeText(context, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isSignUpMode && name.trim().isEmpty()) {
                            Toast.makeText(context, "Please enter your name", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        coroutineScope.launch {
                            isLoading = true
                            try {
                                val fallbackPrefs = context.getSharedPreferences("vibro_auth_fallback", Context.MODE_PRIVATE)
                                // Pre-seed fallback with default credential for nathanaeleshetu8901@gmail.com
                                if (!fallbackPrefs.contains("user_pass_nathanaeleshetu8901@gmail.com")) {
                                    fallbackPrefs.edit()
                                        .putString("user_pass_nathanaeleshetu8901@gmail.com", "123456")
                                        .putString("user_name_nathanaeleshetu8901@gmail.com", "Nathanael Eshetu")
                                        .putString("user_city_nathanaeleshetu8901@gmail.com", "Addis Ababa")
                                        .apply()
                                }

                                if (NetworkManager.isSupabaseConfigured) {
                                    if (isSignUpMode) {
                                        try {
                                            // Supabase Sign Up
                                            val response = NetworkManager.signUpWithEmail(email, password, name, city)
                                            // Store locally for offline/fallback baseline
                                            fallbackPrefs.edit()
                                                .putString("user_pass_$email", password)
                                                .putString("user_name_$email", name)
                                                .putString("user_city_$email", city)
                                                .apply()
                                            
                                            isLoading = false
                                            onLoginSuccess(email, password, name, city)
                                            Toast.makeText(context, "Welcome to Vibro, $name! Account created.", Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            val isConnError = e is java.io.IOException || e.message?.contains("Unable to resolve host", ignoreCase = true) == true || e.message?.contains("connect", ignoreCase = true) == true || e.message?.contains("unavailable", ignoreCase = true) == true
                                            if (isConnError) {
                                                // Fallback to offline registration if not registered yet
                                                if (fallbackPrefs.contains("user_pass_$email")) {
                                                    throw Exception("Account already exists locally. Offline mode cannot overwrite. Please connect to the internet.")
                                                }
                                                fallbackPrefs.edit()
                                                    .putString("user_pass_$email", password)
                                                    .putString("user_name_$email", name)
                                                    .putString("user_city_$email", city)
                                                    .apply()
                                                isLoading = false
                                                onLoginSuccess(email, password, name, city)
                                                Toast.makeText(context, "Account created locally (offline fallback)! Welcome, $name.", Toast.LENGTH_LONG).show()
                                            } else {
                                                // Real authentication error (e.g. email already exists in Supabase)
                                                throw e
                                            }
                                        }
                                    } else {
                                        try {
                                            // Supabase Sign In
                                            val response = NetworkManager.signInWithEmail(email, password)
                                            val metaName = response.user?.userMetadata?.name ?: email.substringBefore("@")
                                            val metaCity = response.user?.userMetadata?.city ?: "Addis Ababa"
                                            
                                            // Sync locally
                                            fallbackPrefs.edit()
                                                .putString("user_pass_$email", password)
                                                .putString("user_name_$email", metaName)
                                                .putString("user_city_$email", metaCity)
                                                .apply()
                                            
                                            isLoading = false
                                            onLoginSuccess(email, password, metaName, metaCity)
                                            Toast.makeText(context, "Welcome back, $metaName!", Toast.LENGTH_LONG).show()
                                        } catch (e: Exception) {
                                            val isConnError = e is java.io.IOException || e.message?.contains("Unable to resolve host", ignoreCase = true) == true || e.message?.contains("connect", ignoreCase = true) == true || e.message?.contains("unavailable", ignoreCase = true) == true
                                            if (isConnError && fallbackPrefs.contains("user_pass_$email")) {
                                                // Offline local password check
                                                val savedPass = fallbackPrefs.getString("user_pass_$email", "")
                                                if (savedPass != password) {
                                                    throw Exception("Incorrect password (offline verification).")
                                                }
                                                val storedName = fallbackPrefs.getString("user_name_$email", "Guest") ?: "Guest"
                                                val storedCity = fallbackPrefs.getString("user_city_$email", "Addis Ababa") ?: "Addis Ababa"
                                                isLoading = false
                                                onLoginSuccess(email, password, storedName, storedCity)
                                                Toast.makeText(context, "Logged in via offline mode. Welcome back, $storedName!", Toast.LENGTH_LONG).show()
                                            } else {
                                                // Strict error propagation for credentials rejection
                                                throw e
                                            }
                                        }
                                    }
                                } else {
                                    // Local Offline Fallback Mode
                                    if (isSignUpMode) {
                                        if (fallbackPrefs.contains("user_pass_$email")) {
                                            isLoading = false
                                            Toast.makeText(context, "Email is already registered locally.", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                        fallbackPrefs.edit()
                                            .putString("user_pass_$email", password)
                                            .putString("user_name_$email", name)
                                            .putString("user_city_$email", city)
                                            .apply()
                                        
                                        isLoading = false
                                        onLoginSuccess(email, password, name, city)
                                        Toast.makeText(context, "Account created locally! Welcome, $name.", Toast.LENGTH_LONG).show()
                                    } else {
                                        if (!fallbackPrefs.contains("user_pass_$email")) {
                                            isLoading = false
                                            Toast.makeText(context, "No account found with this email. Please sign up register.", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                        val savedPass = fallbackPrefs.getString("user_pass_$email", "")
                                        if (savedPass != password) {
                                            isLoading = false
                                            Toast.makeText(context, "Incorrect password. Please try again.", Toast.LENGTH_LONG).show()
                                            return@launch
                                        }
                                        val storedName = fallbackPrefs.getString("user_name_$email", "Guest") ?: "Guest"
                                        val storedCity = fallbackPrefs.getString("user_city_$email", "Addis Ababa") ?: "Addis Ababa"
                                        
                                        isLoading = false
                                        onLoginSuccess(email, password, storedName, storedCity)
                                        Toast.makeText(context, "Successfully logged in! Welcome back, $storedName.", Toast.LENGTH_LONG).show()
                                    }
                                }
                            } catch (e: Exception) {
                                isLoading = false
                                val displayError = e.message ?: "Authentication failed"
                                android.util.Log.e("LoginScreen", "Auth error: $displayError", e)
                                Toast.makeText(context, displayError, Toast.LENGTH_LONG).show()
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            text = if (isSignUpMode) "Register & Start Deals" else "Sign In Now",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            // Footer Switch Mode Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(
                    onClick = { isSignUpMode = !isSignUpMode }
                ) {
                    Text(
                        text = if (isSignUpMode) 
                            "Already have an account? Sign In" 
                            else "Don't have an account? Create one now",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

fun compressImageUri(context: Context, uri: android.net.Uri): ByteArray? {
    return try {
        val contentResolver = context.contentResolver
        val rawBytes = contentResolver.openInputStream(uri)?.use { it.readBytes() } ?: return null
        
        // Decode bounds to get original dimensions safely
        val options = android.graphics.BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        android.graphics.BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, options)

        var width = options.outWidth
        var height = options.outHeight
        if (width <= 0 || height <= 0) {
            return rawBytes
        }

        var inSampleSize = 1
        val maxDimension = 1024
        while (width / 2 >= maxDimension || height / 2 >= maxDimension) {
            width /= 2
            height /= 2
            inSampleSize *= 2
        }

        val optionsForBitmap = android.graphics.BitmapFactory.Options().apply {
            this.inSampleSize = inSampleSize
        }
        val bitmap = android.graphics.BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, optionsForBitmap)

        if (bitmap != null) {
            val outputStream = java.io.ByteArrayOutputStream()
            bitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 75, outputStream)
            val bytes = outputStream.toByteArray()
            bitmap.recycle()
            bytes
        } else {
            rawBytes
        }
    } catch (e: Throwable) {
        android.util.Log.e("MarketplaceApp", "Error compressing image bytes safely", e)
        null
    }
}

fun getProductImages(product: Product): List<String> {
    val list = mutableListOf(product.imageUrl)
    val categoryId = product.categoryId
    val alt1 = when (categoryId) {
        "1" -> "https://images.unsplash.com/photo-1549399542-7e3f8b79c341?auto=format&fit=crop&w=600&q=80"
        "2" -> "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=600&q=80"
        "3" -> "https://images.unsplash.com/photo-1512917774080-9991f1c4c750?auto=format&fit=crop&w=600&q=80"
        "4" -> "https://images.unsplash.com/photo-1483985988355-763728e1935b?auto=format&fit=crop&w=600&q=80"
        "5" -> "https://images.unsplash.com/photo-1524758631624-e2822e304c36?auto=format&fit=crop&w=600&q=80"
        else -> "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80"
    }
    val alt2 = when (categoryId) {
        "1" -> "https://images.unsplash.com/photo-1503376780353-7e6692767b70?auto=format&fit=crop&w=600&q=80"
        "2" -> "https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=600&q=80"
        "3" -> "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?auto=format&fit=crop&w=600&q=80"
        "4" -> "https://images.unsplash.com/photo-1490481651871-ab68de25d43d?auto=format&fit=crop&w=600&q=80"
        "5" -> "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=600&q=80"
        else -> "https://images.unsplash.com/photo-1583394838336-acd977736f90?auto=format&fit=crop&w=600&q=80"
    }
    list.add(alt1)
    list.add(alt2)
    return list
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FullScreenImageGallery(
    images: List<String>,
    initialIndex: Int,
    onDismiss: () -> Unit
) {
    val pagerState = rememberPagerState(
        initialPage = initialIndex,
        pageCount = { images.size }
    )
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = images[page],
                            contentDescription = "Zoomed Product Image ${page + 1}",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Surface(
                        color = Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1} / ${images.size}",
                            color = Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(48.dp))
                }

                BackHandler {
                    onDismiss()
                }
            }
        }
    }
}

// --- Quick Chat Dialog ---
@Composable
fun QuickChatDialog(
    sellerName: String,
    productTitle: String,
    onDismiss: () -> Unit,
    onSend: (message: String) -> Unit
) {
    var messageText by remember { mutableStateOf("Is this available?") }
    val templates = listOf(
        "Is this available?",
        "What's the last price?",
        "Can I inspect this in person?",
        "I want to buy this, can we talk?"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
                .clickable(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            Card(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = {} // Consume click to prevent dismiss when clicking inside the card
                    )
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 24.dp, start = 20.dp, end = 20.dp)
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Bottom Sheet Handle Bar
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                            .align(Alignment.CenterHorizontally)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Header
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Chat with $sellerName",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = productTitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Section title
                    Text(
                        text = "Quick templates:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Scrollable row of templates
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        templates.forEach { template ->
                            val isSelected = messageText == template
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                ),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable {
                                    messageText = template
                                }
                            ) {
                                Text(
                                    text = template,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom message input
                    Text(
                        text = "Customize message:",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("Type your message here...") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 4,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (messageText.isNotBlank()) {
                                    onSend(messageText.trim())
                                }
                            },
                            enabled = messageText.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Send", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// --- Product Persistence Helper ---
object ProductPersistence {
    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val listType = com.squareup.moshi.Types.newParameterizedType(List::class.java, Product::class.java)
    private val adapter = moshi.adapter<List<Product>>(listType)

    fun saveProducts(context: Context, products: List<Product>) {
        try {
            val json = adapter.toJson(products)
            val prefs = context.getSharedPreferences("marketplace_products_prefs", Context.MODE_PRIVATE)
            prefs.edit().putString("saved_products_list", json).apply()
            android.util.Log.i("ProductPersistence", "Saved ${products.size} products to local storage.")
        } catch (e: Exception) {
            android.util.Log.e("ProductPersistence", "Failed to save products: ${e.message}")
        }
    }

    fun loadProducts(context: Context): List<Product>? {
        try {
            val prefs = context.getSharedPreferences("marketplace_products_prefs", Context.MODE_PRIVATE)
            val json = prefs.getString("saved_products_list", null) ?: return null
            val products = adapter.fromJson(json)
            android.util.Log.i("ProductPersistence", "Loaded ${products?.size ?: 0} products from local storage.")
            return products
        } catch (e: Exception) {
            android.util.Log.e("ProductPersistence", "Failed to load products: ${e.message}")
            return null
        }
    }
}

// --- Update Dialog ---
@Composable
fun UpdateDialog(
    updateInfo: GitHubUpdateManager.UpdateInfo,
    isDownloading: Boolean,
    downloadProgress: Float,
    onDismiss: () -> Unit,
    onUpdateClick: () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "New Update Available!",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Version: ${updateInfo.latestVersion}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                if (!isDownloading) {
                    // Release Notes
                    Text(
                        text = "What's New:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 150.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        Text(
                            text = updateInfo.releaseNotes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    if (updateInfo.apkSize > 0) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Download size: ${String.format("%.2f", updateInfo.apkSize.toFloat() / (1024 * 1024))} MB",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Later")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onUpdateClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Download & Install", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    // Downloading State
                    Text(
                        text = "Downloading update package...",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LinearProgressIndicator(
                        progress = { downloadProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${(downloadProgress * 100).toInt()}% downloaded",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        if (updateInfo.apkSize > 0) {
                            val downloadedMb = (downloadProgress * updateInfo.apkSize.toFloat()) / (1024 * 1024)
                            val totalMb = updateInfo.apkSize.toFloat() / (1024 * 1024)
                            Text(
                                text = "${String.format("%.1f", downloadedMb)} / ${String.format("%.1f", totalMb)} MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Please do not close the app while the download is in progress.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}


