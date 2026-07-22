package com.example.ui.product

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.NetworkManager
import com.example.model.Product
import com.example.model.Review
import com.example.model.VibroArticle
import com.example.ui.components.FullScreenImageGallery
import com.example.ui.sell.PostAdDialog
import com.example.ui.components.QuickChatDialog
import com.example.ui.components.SellerReviewsDialog
import com.example.util.getProductImages
import kotlinx.coroutines.launch

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
    onDeleteProduct: (Product) -> Unit = {},
    onSellerClick: (String) -> Unit = {}
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
        Dialog(
            onDismissRequest = { activeReadingArticle = null },
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
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { activeReadingArticle = null }
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Surface(
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight(0.75f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .navigationBarsPadding()
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
                                .weight(1f)
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
            ) {
                val images = getProductImages(product)
                val pagerState = rememberPagerState(pageCount = { images.size })

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    AsyncImage(
                        model = images[page],
                        contentDescription = "${product.title} Image ${page + 1}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize().clickable { showFullScreenPhotoViewer = true },
                        error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(product.categoryId, product.title)),
                        placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(product.categoryId, product.title))
                    )
                }

                if (images.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        repeat(images.size) { i ->
                            Box(
                                modifier = Modifier
                                    .size(if (pagerState.currentPage == i) 8.dp else 6.dp)
                                    .clip(CircleShape)
                                    .background(if (pagerState.currentPage == i) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f))
                            )
                        }
                    }
                }

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
                Text(com.example.model.formatDisplayPrice(product.price), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)

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
                        .clickable { onSellerClick(product.sellerName) }
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
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
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
            PostAdDialog(
                editingProduct = currentProductState,
                onDismiss = { showEditDialog = false },
                onPublish = { title, price, categoryId, location, condition, description, imageUrl, isPromoted ->
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
                        imageUrl = finalImgUrl,
                        isPromoted = isPromoted
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
