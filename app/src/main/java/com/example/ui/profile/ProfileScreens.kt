package com.example.ui.profile

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.NetworkManager
import com.example.model.Product
import com.example.model.Review
import com.example.ui.components.ProductGridCard

@Composable
fun ProfileOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun KeyfacedCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun ProfileListingsPageContent(
    products: List<Product>,
    onProductClick: (String) -> Unit
) {
    val myProducts = remember(products) { products.filter { it.sellerName == "You" || it.sellerName == "Abebe Kebede" } }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("My Active Listings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${myProducts.size} live ads published on Vibro", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (myProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No active listings found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(myProducts) { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onProductClick(prod.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = prod.imageUrl,
                                    contentDescription = prod.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title)),
                                    placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(prod.price, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier.background(Color(0xFF22C55E).copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("ACTIVE", color = Color(0xFF16A34A), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(prod.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileBookmarksPageContent(
    products: List<Product>,
    savedProductIds: List<String>,
    onProductClick: (String) -> Unit
) {
    val bookmarkedProducts = remember(products, savedProductIds) {
        products.filter { savedProductIds.contains(it.id) }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Saved Favorites", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("${bookmarkedProducts.size} items bookmarked for later", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        Spacer(modifier = Modifier.height(16.dp))

        if (bookmarkedProducts.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No saved bookmarks yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(bookmarkedProducts) { prod ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onProductClick(prod.id) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(70.dp).clip(RoundedCornerShape(8.dp))) {
                                AsyncImage(
                                    model = prod.imageUrl,
                                    contentDescription = prod.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title)),
                                    placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(prod.categoryId, prod.title))
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(prod.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text(prod.price, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(prod.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
    var reviewerNameInput by remember { mutableStateOf("") }
    var reviewCommentInput by remember { mutableStateOf("") }
    var selectedRatingInput by remember { mutableStateOf(5) }

    val filteredReviews = remember(sellerReviews, sellerName) {
        sellerReviews.filter { it.sellerName == sellerName || sellerName == "You" }
    }

    val avgRating = if (filteredReviews.isEmpty()) 5.0f else filteredReviews.map { it.rating }.average().toFloat()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(sellerName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
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
                Spacer(modifier = Modifier.height(4.dp))
                Text(String.format("%.1f out of 5.0 (%d reviews)", avgRating, filteredReviews.size), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text("Write a Review", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = reviewerNameInput,
            onValueChange = { reviewerNameInput = it },
            label = { Text("Your Name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Rating: ", fontWeight = FontWeight.Bold)
            repeat(5) { idx ->
                val ratingVal = idx + 1
                IconButton(onClick = { selectedRatingInput = ratingVal }) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = if (ratingVal <= selectedRatingInput) Color(0xFFFBBF24) else Color.LightGray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = reviewCommentInput,
            onValueChange = { reviewCommentInput = it },
            label = { Text("Your Feedback") },
            modifier = Modifier.fillMaxWidth().height(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (reviewerNameInput.trim().isNotEmpty() && reviewCommentInput.trim().isNotEmpty()) {
                    onAddReview(reviewerNameInput.trim(), selectedRatingInput, reviewCommentInput.trim())
                    reviewerNameInput = ""
                    reviewCommentInput = ""
                    Toast.makeText(context, "Review submitted!", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = reviewerNameInput.trim().isNotEmpty() && reviewCommentInput.trim().isNotEmpty()
        ) {
            Text("Submit Review", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text("Customer Feedback (${filteredReviews.size})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))

        filteredReviews.forEach { rev ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(rev.reviewerName, fontWeight = FontWeight.Bold)
                        Row {
                            repeat(rev.rating) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFBBF24), modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(rev.comment, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(rev.timeAgo, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

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
    products: List<Product>,
    savedProductIds: List<String>,
    onProductClick: (String) -> Unit,
    sellerReviews: List<Review>,
    onAddReview: (reviewerName: String, rating: Int, comment: String) -> Unit,
    onPostAdClick: () -> Unit,
    onManualCheckForUpdates: () -> Unit = {},
    currentThemeMode: String = "system",
    onThemeModeChange: (String) -> Unit = {}
) {
    var activeSubPage by remember { mutableStateOf<String?>(null) }

    when (activeSubPage) {
        "listings" -> {
            Column(modifier = Modifier.fillMaxSize()) {
                IconButton(onClick = { activeSubPage = null }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                ProfileListingsPageContent(products = products, onProductClick = onProductClick)
            }
        }
        "bookmarks" -> {
            Column(modifier = Modifier.fillMaxSize()) {
                IconButton(onClick = { activeSubPage = null }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                ProfileBookmarksPageContent(products = products, savedProductIds = savedProductIds, onProductClick = onProductClick)
            }
        }
        "reviews" -> {
            Column(modifier = Modifier.fillMaxSize()) {
                IconButton(onClick = { activeSubPage = null }, modifier = Modifier.padding(8.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                SellerReviewsPageContent(sellerName = userName.ifEmpty { "You" }, sellerReviews = sellerReviews, onAddReview = onAddReview)
            }
        }
        else -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Profile Header
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (isLoggedIn && userName.isNotEmpty()) userName.take(1).uppercase() else "G",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (isLoggedIn) userName else "Guest User",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isLoggedIn) userEmail else "Sign in to post ads and chat with sellers",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (isLoggedIn && userCity.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(userCity, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Stats Cards Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSubPage = "listings" },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$myAdsCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("My Listings", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { activeSubPage = "bookmarks" },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$savedCount", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("Saved Items", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Menu options
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column {
                        ProfileOptionRow(
                            icon = Icons.Default.ListAlt,
                            title = "My Active Listings",
                            subtitle = "Manage your published ads",
                            onClick = { activeSubPage = "listings" }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ProfileOptionRow(
                            icon = Icons.Default.BookmarkBorder,
                            title = "Saved Favorites",
                            subtitle = "Items you've bookmarked",
                            onClick = { activeSubPage = "bookmarks" }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ProfileOptionRow(
                            icon = Icons.Default.Star,
                            title = "Seller Reviews",
                            subtitle = "View ratings and feedback",
                            onClick = { activeSubPage = "reviews" }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        ProfileOptionRow(
                            icon = Icons.Default.SystemUpdate,
                            title = "Check for Updates",
                            subtitle = "Vibro Marketplace v1.0.6",
                            onClick = onManualCheckForUpdates
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Theme Mode Selector
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("App Theme", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("system" to "System", "light" to "Light", "dark" to "Dark").forEach { (modeKey, label) ->
                                val isSelected = currentThemeMode == modeKey
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onThemeModeChange(modeKey) },
                                    label = { Text(label) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Sign in / Sign out button
                if (isLoggedIn) {
                    OutlinedButton(
                        onClick = onSignOutClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign Out", fontWeight = FontWeight.Bold)
                    }
                } else {
                    Button(
                        onClick = onSignInClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Login, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Sign In or Register", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

