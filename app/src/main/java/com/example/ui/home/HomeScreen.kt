package com.example.ui.home

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import kotlinx.coroutines.launch
import com.example.NetworkManager
import com.example.model.Product
import com.example.model.ethiopianCities
import com.example.model.mockCategories
import com.example.ui.components.CategoryChip
import com.example.ui.components.ProductGridCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    products: List<Product>,
    savedProductIds: List<String>,
    onProductClick: (String) -> Unit,
    onRefreshFeed: () -> Unit,
    onSearchClick: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedCategoryId by remember { mutableStateOf<String?>(null) }
    var showConnectionDialog by remember { mutableStateOf(false) }
    var showCitySelectorHome by remember { mutableStateOf(false) }
    var selectedCityHomeFilter by remember { mutableStateOf("Ethiopia (All)") }
    var isGridView by remember { mutableStateOf(true) }
    var citySearchQuery by remember { mutableStateOf("") }

    val filteredProducts = remember(products, selectedCategoryId, selectedCityHomeFilter) {
        products.filter { product ->
            val matchesCategory = selectedCategoryId == null || product.categoryId == selectedCategoryId
            val matchesCity = if (selectedCityHomeFilter == "Ethiopia (All)") true else product.location.contains(selectedCityHomeFilter, ignoreCase = true)
            matchesCategory && matchesCity
        }
    }

    val filteredCities = remember(citySearchQuery) {
        if (citySearchQuery.isBlank()) ethiopianCities
        else ethiopianCities.filter { it.name.contains(citySearchQuery, ignoreCase = true) }
    }

    val bannerPagerState = rememberPagerState(pageCount = { 2 })

    var isTopBarVisible by remember { mutableStateOf(true) }
    val gridState = rememberLazyStaggeredGridState()
    val listState = rememberLazyListState()

    val isAtTop by remember {
        derivedStateOf {
            if (isGridView) {
                gridState.firstVisibleItemIndex == 0 && gridState.firstVisibleItemScrollOffset == 0
            } else {
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            }
        }
    }

    LaunchedEffect(isAtTop) {
        if (isAtTop) {
            isTopBarVisible = true
        }
    }

    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val delta = available.y
                if (delta < -12f && isTopBarVisible) {
                    isTopBarVisible = false
                }
                return Offset.Zero
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .nestedScroll(nestedScrollConnection)
        ) {
        // --- Collapsible Top Header (Top App Bar, Search Bar, Banner, Category Icons) ---
        AnimatedVisibility(
            visible = isTopBarVisible,
            enter = slideInVertically(initialOffsetY = { -it }) + expandVertically(),
            exit = slideOutVertically(targetOffsetY = { -it }) + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                    shadowElevation = 4.dp
                ) {
                    Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 12.dp)) {
                        // Header Row: App Title + Location Selector + Action Badges
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Logo / App Brand
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Storefront,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Vibro",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Location Selector Pill
                            Surface(
                                onClick = { showCitySelectorHome = true },
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = selectedCityHomeFilter,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.width(2.dp))
                                    Icon(
                                        Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }

                            // Top Action Icons (Cloud Sync & Alerts)
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                IconButton(
                                    onClick = { showConnectionDialog = true },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                                ) {
                                    Icon(
                                        Icons.Default.CloudSync,
                                        contentDescription = "Sync",
                                        tint = if (NetworkManager.isSupabaseConfigured) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                Box {
                                    IconButton(
                                        onClick = { /* Alerts */ },
                                        modifier = Modifier
                                            .size(36.dp)
                                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), CircleShape)
                                    ) {
                                        Icon(
                                            Icons.Outlined.Notifications,
                                            contentDescription = "Alerts",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(Color.Red, CircleShape)
                                            .align(Alignment.TopEnd)
                                            .offset(x = (-2).dp, y = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Elevated Search Field Bar
                        Surface(
                            onClick = onSearchClick,
                            shape = RoundedCornerShape(14.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("search_input")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Search phones, cars, electronics, fashion...",
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            Icons.Default.Tune,
                                            contentDescription = "Filters",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Filter",
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

                // Promotional Banner Carousel
                Box(modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)) {
                    HorizontalPager(
                        state = bannerPagerState,
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 12.dp,
                        modifier = Modifier.fillMaxWidth().height(115.dp)
                    ) { page ->
                        if (page == 0) {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "QUICK SELL",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Turn items into cash today",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                        Text(
                                            "Post an ad for free in under 60 seconds",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        Icons.Default.Sell,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        } else {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.secondary,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                "VERIFIED DEALS",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Trusted Local Sellers",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer
                                        )
                                        Text(
                                            "Chat directly & negotiate best prices",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                                        )
                                    }
                                    Icon(
                                        Icons.Default.VerifiedUser,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(44.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Categories Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (selectedCategoryId != null) {
                        Text(
                            text = "Clear Category",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { selectedCategoryId = null }
                        )
                    }
                }

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
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
            }
        }

        // --- Main Feed Scrollable Area ---
        Column(modifier = Modifier.weight(1f)) {
            // Section Header: Feed Title + Item Count + Layout Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (selectedCategoryId != null) "Search Results" else "Trending Feed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = "${filteredProducts.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onRefreshFeed,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(if (isGridView) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { isGridView = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.GridView,
                                    contentDescription = "Grid View",
                                    tint = if (isGridView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .background(if (!isGridView) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(6.dp))
                                    .clickable { isGridView = false },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.ViewList,
                                    contentDescription = "List View",
                                    tint = if (!isGridView) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Feed Content Grid / List
            if (filteredProducts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "No products found",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try clearing your category or city filter to see more items.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                selectedCategoryId = null
                                selectedCityHomeFilter = "Ethiopia (All)"
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Reset All Filters")
                        }
                    }
                }
            } else if (isGridView) {
                LazyVerticalStaggeredGrid(
                    state = gridState,
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 80.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { item ->
                        val isSaved = savedProductIds.contains(item.id)
                        ProductGridCard(
                            product = item,
                            onClick = { onProductClick(item.id) },
                            isSaved = isSaved,
                            onSaveToggle = {
                                if (isSaved) {
                                    (savedProductIds as? MutableList)?.remove(item.id)
                                } else {
                                    (savedProductIds as? MutableList)?.add(item.id)
                                }
                            }
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { item ->
                        val isSaved = savedProductIds.contains(item.id)
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onProductClick(item.id) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(90.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    coil.compose.AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.title,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        placeholder = coil.compose.rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(item.categoryId, item.title)),
                                        error = coil.compose.rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(item.categoryId, item.title))
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.title,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = com.example.model.formatDisplayPrice(item.price),
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.LocationOn,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = item.location,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = item.condition,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

        // Elegant Scroll-To-Top Floating Action Button
        AnimatedVisibility(
            visible = !isAtTop,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 80.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        if (isGridView) {
                            gridState.animateScrollToItem(0)
                        } else {
                            listState.animateScrollToItem(0)
                        }
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "Scroll to Top",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    // --- Searchable City Selector Modal Sheet ---
    if (showCitySelectorHome) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showCitySelectorHome = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { showCitySelectorHome = false }
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
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Select Location",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        // Search Input for Cities
                        OutlinedTextField(
                            value = citySearchQuery,
                            onValueChange = { citySearchQuery = it },
                            placeholder = { Text("Search city (Addis Ababa, Hawassa...)") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedCityHomeFilter = "Ethiopia (All)"
                                    showCitySelectorHome = false
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Public,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    "Ethiopia (All)",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                        LazyColumn(modifier = Modifier.weight(1f)) {
                            items(filteredCities) { city ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            selectedCityHomeFilter = city.name
                                            showCitySelectorHome = false
                                        }
                                        .padding(vertical = 12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                Icons.Default.LocationOn,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Text(city.name, style = MaterialTheme.typography.bodyMedium)
                                        }
                                        if (selectedCityHomeFilter == city.name) {
                                            Icon(
                                                Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = { showCitySelectorHome = false },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    // --- Supabase / Connection Settings Dialog ---
    if (showConnectionDialog) {
        var inputUrl by remember { mutableStateOf(NetworkManager.customSupabaseUrl.ifEmpty { NetworkManager.supabaseUrl }) }
        var inputKey by remember { mutableStateOf(NetworkManager.customSupabaseAnonKey.ifEmpty { NetworkManager.supabaseAnonKey }) }
        val context = LocalContext.current

        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showConnectionDialog = false },
            properties = androidx.compose.ui.window.DialogProperties(
                usePlatformDefaultWidth = false,
                decorFitsSystemWindows = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
                    .clickable(
                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                        indication = null,
                        onClick = { showConnectionDialog = false }
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
                            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                            indication = null,
                            onClick = {}
                        )
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .navigationBarsPadding()
                            .verticalScroll(rememberScrollState())
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                                .align(Alignment.CenterHorizontally)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Backend Sync Settings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Manage Supabase remote connection or sandbox sync below.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = inputUrl,
                            onValueChange = { inputUrl = it },
                            label = { Text("Supabase URL") },
                            placeholder = { Text("https://your-project.supabase.co") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = inputKey,
                            onValueChange = { inputKey = it },
                            label = { Text("Supabase Anon Key") },
                            placeholder = { Text("eyJhbGciOiJIUzI1Ni...") },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3
                        )
                        Spacer(modifier = Modifier.height(12.dp))

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
                                    if (NetworkManager.syncErrorMessage == null) "Status: Connected (${NetworkManager.syncItemCount} remote items)" else "Status: Sync Error"
                                } else "Status: Local Sandbox Mode",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = if (NetworkManager.isSupabaseConfigured && NetworkManager.syncErrorMessage == null) Color(0xFF137333) else Color(0xFFB06000)
                            )
                        }

                        if (NetworkManager.syncErrorMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Error: ${NetworkManager.syncErrorMessage}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
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
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Save & Sync")
                            }

                            OutlinedButton(
                                onClick = { showConnectionDialog = false },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Cancel")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        TextButton(
                            onClick = {
                                NetworkManager.clearConfig(context)
                                inputUrl = ""
                                inputKey = ""
                                onRefreshFeed()
                                showConnectionDialog = false
                                Toast.makeText(context, "Reset to environment variables defaults", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Reset to Env Defaults")
                        }
                    }
                }
            }
        }
    }
}
