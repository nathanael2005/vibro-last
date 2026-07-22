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
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.foundation.isSystemInDarkTheme
import com.example.ui.theme.MyApplicationTheme
import com.example.model.*
import com.example.util.*
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.auth.*
import com.example.ui.home.*
import com.example.ui.search.*
import com.example.ui.seller.*
import com.example.ui.saved.*
import com.example.ui.chat.*
import com.example.ui.product.*
import com.example.ui.sell.*
import com.example.ui.profile.*

// --- Navigation Routes ---
const val SPLASH_ROUTE = "splash"
const val WELCOME_ROUTE = "welcome"
const val LOGIN_ROUTE = "login"
const val MAIN_SHELL_ROUTE = "main"
const val PRODUCT_DETAIL_ROUTE = "product/{productId}"
const val SELLER_PROFILE_ROUTE = "seller_profile/{sellerName}"
fun createProductDetailRoute(id: String) = "product/$id"
fun createSellerProfileRoute(sellerName: String) = "seller_profile/$sellerName"

// --- Preset Data ---
val mockCategories = listOf(
    Category("1", "Vehicles", Icons.Default.DirectionsCar, Color(0xFFFEF3C7), Color(0xFFD97706)),
    Category("2", "Phones & Tablets", Icons.Default.Smartphone, Color(0xFFDBEAFE), Color(0xFF2563EB)),
    Category("3", "Property", Icons.Default.Apartment, Color(0xFFF3E8FF), Color(0xFF9333EA)),
    Category("4", "Fashion", Icons.Default.ShoppingBag, Color(0xFFFFE4E6), Color(0xFFE11D48)),
    Category("5", "Furniture & Appliances", Icons.Default.Chair, Color(0xFFD1FAE5), Color(0xFF059669)),
    Category("6", "Electronics", Icons.Default.Devices, Color(0xFFE0F2FE), Color(0xFF0369A1)),
    Category("7", "Health & Beauty", Icons.Default.AutoAwesome, Color(0xFFFCE7F3), Color(0xFFBE185D)),
    Category("8", "Services", Icons.Default.Handyman, Color(0xFFE0E7FF), Color(0xFF4F46E5)),
    Category("9", "Jobs", Icons.Default.BusinessCenter, Color(0xFFFFEDD5), Color(0xFFEA580C)),
    Category("10", "Animals & Pets", Icons.Default.Pets, Color(0xFFCCFBF1), Color(0xFF0D9488)),
    Category("11", "Agriculture & Food", Icons.Default.Agriculture, Color(0xFFECFCCB), Color(0xFF65A30D))
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
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("vibro_prefs", Context.MODE_PRIVATE) }
    var themeMode by remember { mutableStateOf(sharedPrefs.getString("app_theme_mode", "system") ?: "system") }

    val systemInDark = isSystemInDarkTheme()
    val useDarkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> systemInDark
    }

    MyApplicationTheme(darkTheme = useDarkTheme) {
        Surface(modifier = Modifier.fillMaxSize()) {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()

            // Unified Robust Initialization
            val myPostedAdIds = remember { ProductPersistence.getPostedAdIds(context) }
            val products = remember {
                val loaded = ProductPersistence.loadProducts(context)
                val initialList = if (!loaded.isNullOrEmpty()) {
                    mockProducts.clear()
                    mockProducts.addAll(loaded)
                    loaded
                } else {
                    mockProducts
                }
                
                mutableStateListOf<Product>().apply {
                    addAll(initialList.map { p ->
                        if (myPostedAdIds.contains(p.id)) p.copy(sellerName = "You") else p
                    })
                }
            }

            LaunchedEffect(Unit) {
                NetworkManager.initialize(context.applicationContext)
                AdDraftStore.loadDraft(context.applicationContext)
            }

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
                    
                    val myPostedAdIds = ProductPersistence.getPostedAdIds(context)
                    val remoteMapped = allRemoteMaps.map { map ->
                        val p = NetworkManager.mapMapToProduct(map)
                        val finalImage = if (p.imageUrl.isBlank() || p.imageUrl.equals("null", ignoreCase = true)) {
                            NetworkManager.getFallbackImageUrl(p.categoryId, p.title)
                        } else {
                            p.imageUrl
                        }
                        val finalSeller = if (myPostedAdIds.contains(p.id) || p.sellerName == "You") "You" else p.sellerName
                        p.copy(imageUrl = finalImage, sellerName = finalSeller)
                    }.distinctBy { it.id }

                    // Keep unique mock products to preserve "remaining ads"
                    val remoteIds = remoteMapped.map { it.id }.toSet()
                    val remainingMock = mockProducts.map { p ->
                        val finalSeller = if (myPostedAdIds.contains(p.id) || p.sellerName == "You") "You" else p.sellerName
                        p.copy(sellerName = finalSeller)
                    }.filter { it.id !in remoteIds }

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
                onManualCheckForUpdates = onManualCheckForUpdates,
                currentThemeMode = themeMode,
                onThemeModeChange = { mode ->
                    themeMode = mode
                    sharedPrefs.edit().putString("app_theme_mode", mode).apply()
                }
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
                    },
                    onSellerClick = { sellerName ->
                        navController.navigate(createSellerProfileRoute(sellerName))
                    }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Product not found")
                }
            }
        }
        
        composable(
            route = SELLER_PROFILE_ROUTE,
            arguments = listOf(navArgument("sellerName") { type = NavType.StringType })
        ) { backStackEntry ->
            val sellerName = backStackEntry.arguments?.getString("sellerName") ?: ""
            SellerProfileScreen(
                sellerName = sellerName,
                products = products,
                sellerReviews = sellerReviews,
                onBackClick = { navController.popBackStack() },
                onProductClick = { id ->
                    navController.navigate(createProductDetailRoute(id))
                }
            )
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
    onManualCheckForUpdates: () -> Unit = {},
    currentThemeMode: String = "system",
    onThemeModeChange: (String) -> Unit = {}
) {
    var activeTab by remember { mutableStateOf("home") }
    var showPostAdDialog by remember { mutableStateOf(false) }
    var showLoginRequiredDialog by remember { mutableStateOf(false) }
    var showAdvancedSearch by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    if (showAdvancedSearch) {
        AdvancedSearchScreen(
            products = products,
            onProductClick = onProductClick,
            onClose = { showAdvancedSearch = false }
        )
    } else {
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
                        onRefreshFeed = onRefreshFeed,
                        onSearchClick = { showAdvancedSearch = true }
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
                    onManualCheckForUpdates = onManualCheckForUpdates,
                    currentThemeMode = currentThemeMode,
                    onThemeModeChange = onThemeModeChange
                )
            }

            if (showLoginRequiredDialog) {
                Dialog(
                    onDismissRequest = { showLoginRequiredDialog = false },
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
                                onClick = { showLoginRequiredDialog = false }
                            ),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Surface(
                            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                    indication = null,
                                    onClick = {}
                                )
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(24.dp)
                                    .navigationBarsPadding()
                                    .fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(40.dp)
                                        .height(4.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                        .align(Alignment.CenterHorizontally)
                                )
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(end = 8.dp)
                                    )
                                    Text("Sign In Required", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("To list your products and post ads on Vibro Marketplace, you need to sign in first. It only takes a minute!", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = {
                                        showLoginRequiredDialog = false
                                        onSignInClick()
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Sign In Now")
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedButton(
                                    onClick = { showLoginRequiredDialog = false },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Cancel")
                                }
                            }
                        }
                    }
                }
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
                        ProductPersistence.addPostedAdId(context, newAdId)
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
                                    val response = NetworkManager.supabaseApi?.insertAd(payload)
                                    if (response != null && response.isNotEmpty()) {
                                        val returnedProd = NetworkManager.mapMapToProduct(response[0])
                                        ProductPersistence.addPostedAdId(context, returnedProd.id)
                                        val idxProducts = products.indexOfFirst { it.id == newProd.id }
                                        if (idxProducts >= 0) {
                                            products[idxProducts] = returnedProd.copy(sellerName = "You")
                                        }
                                        val idxMock = mockProducts.indexOfFirst { it.id == newProd.id }
                                        if (idxMock >= 0) {
                                            mockProducts[idxMock] = returnedProd.copy(sellerName = "You")
                                        }
                                        ProductPersistence.saveProducts(context, mockProducts)
                                    }
                                    android.util.Log.i("MarketplaceApp", "Synced newly published ad to Supabase (ads table).")
                                    NetworkManager.syncErrorMessage = null
                                } catch (e: Exception) {
                                    android.util.Log.w("MarketplaceApp", "Could not sync new ad to Supabase (ads table): ${NetworkManager.getErrorMessage(e)}. Trying 'products' table...")
                                    try {
                                        val payload = NetworkManager.buildInsertPayload(newProd, NetworkManager.productTableKeys)
                                        android.util.Log.d("MarketplaceApp", "Prepared payload for 'products' table: $payload")
                                        val response = NetworkManager.supabaseApi?.insertProduct(payload)
                                        if (response != null && response.isNotEmpty()) {
                                            val returnedProd = NetworkManager.mapMapToProduct(response[0])
                                            ProductPersistence.addPostedAdId(context, returnedProd.id)
                                            val idxProducts = products.indexOfFirst { it.id == newProd.id }
                                            if (idxProducts >= 0) {
                                                products[idxProducts] = returnedProd.copy(sellerName = "You")
                                            }
                                            val idxMock = mockProducts.indexOfFirst { it.id == newProd.id }
                                            if (idxMock >= 0) {
                                                mockProducts[idxMock] = returnedProd.copy(sellerName = "You")
                                            }
                                            ProductPersistence.saveProducts(context, mockProducts)
                                        }
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
                        AdDraftStore.clear()
                        AdDraftStore.saveDraft(context)
                        showPostAdDialog = false
                        activeTab = "home"
                    }
                )
            }
        }
    }
}
}
