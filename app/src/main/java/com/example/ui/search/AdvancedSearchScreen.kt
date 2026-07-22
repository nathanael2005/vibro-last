package com.example.ui.search

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.example.NetworkManager
import com.example.model.Product
import com.example.model.ethiopianCities
import com.example.model.mockCategories
import com.example.ui.components.ProductGridCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedSearchScreen(
    products: List<Product>,
    onProductClick: (String) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var submittedQuery by rememberSaveable { mutableStateOf("") }
    var isSubmitted by rememberSaveable { mutableStateOf(false) }

    var selectedCategoryName by rememberSaveable { mutableStateOf("All Categories") }
    var selectedCityName by rememberSaveable { mutableStateOf("All Cities") }
    var selectedCondition by rememberSaveable { mutableStateOf("All") }
    var selectedSort by rememberSaveable { mutableStateOf("Recommended") }
    var minPriceText by rememberSaveable { mutableStateOf("") }
    var maxPriceText by rememberSaveable { mutableStateOf("") }

    var isGridView by rememberSaveable { mutableStateOf(true) }
    var showFilterSheet by rememberSaveable { mutableStateOf(false) }

    val popularSearchTags = remember {
        listOf("Toyota Vitz", "iPhone 15 Pro", "Apartment Bole", "Sony PS5", "Nike Shoes", "MacBook Pro", "Washing Machine")
    }

    val filteredProducts = remember(products, submittedQuery, isSubmitted, selectedCategoryName, selectedCityName, selectedCondition, selectedSort, minPriceText, maxPriceText) {
        val activeSearch = if (isSubmitted) submittedQuery else searchQuery
        
        var list = products.filter { prod ->
            val matchesQuery = activeSearch.isBlank() ||
                    prod.title.contains(activeSearch, ignoreCase = true) ||
                    prod.description.contains(activeSearch, ignoreCase = true)

            val matchesCategory = selectedCategoryName == "All Categories" ||
                    mockCategories.find { it.id == prod.categoryId }?.name == selectedCategoryName

            val matchesCity = selectedCityName == "All Cities" ||
                    prod.location.contains(selectedCityName, ignoreCase = true)

            val matchesCondition = selectedCondition == "All" ||
                    prod.condition.equals(selectedCondition, ignoreCase = true)

            val rawPrice = prod.price.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            val minP = minPriceText.toDoubleOrNull() ?: 0.0
            val maxP = maxPriceText.toDoubleOrNull() ?: Double.MAX_VALUE
            val matchesPrice = rawPrice >= minP && rawPrice <= maxP

            matchesQuery && matchesCategory && matchesCity && matchesCondition && matchesPrice
        }

        list = when (selectedSort) {
            "Price: Low to High" -> list.sortedBy { it.price.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0 }
            "Price: High to Low" -> list.sortedByDescending { it.price.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0 }
            "Newest" -> list.sortedByDescending { it.id.toIntOrNull() ?: 0 }
            else -> list
        }
        list
    }

    val activeFilterCount = remember(selectedCategoryName, selectedCityName, selectedCondition, minPriceText, maxPriceText) {
        var count = 0
        if (selectedCategoryName != "All Categories") count++
        if (selectedCityName != "All Cities") count++
        if (selectedCondition != "All") count++
        if (minPriceText.isNotEmpty() || maxPriceText.isNotEmpty()) count++
        count
    }

    androidx.activity.compose.BackHandler {
        if (showFilterSheet) {
            showFilterSheet = false
        } else {
            onClose()
        }
    }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 2.dp,
                shadowElevation = 3.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(start = 12.dp, end = 12.dp, top = 8.dp, bottom = 8.dp)
                ) {
                    // Header Search Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Modern Input Field
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = {
                                searchQuery = it
                                isSubmitted = false
                            },
                            placeholder = { Text("Search products, brands, locations...") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        submittedQuery = ""
                                        isSubmitted = false
                                    }) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Clear",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("search_input_field")
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        // Filter Button
                        Surface(
                            onClick = { showFilterSheet = true },
                            shape = RoundedCornerShape(14.dp),
                            color = if (activeFilterCount > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            border = BorderStroke(
                                1.dp,
                                if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier.size(50.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Tune,
                                    contentDescription = "Filter Options",
                                    tint = if (activeFilterCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                if (activeFilterCount > 0) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.primary,
                                        shape = CircleShape,
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(16.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "$activeFilterCount",
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Active Filters / Sort Indicator Pills
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        contentPadding = PaddingValues(horizontal = 4.dp)
                    ) {
                        item {
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Sort, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Sort: $selectedSort", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        if (selectedCategoryName != "All Categories") {
                            item {
                                ActiveFilterPill(label = selectedCategoryName) { selectedCategoryName = "All Categories" }
                            }
                        }

                        if (selectedCityName != "All Cities") {
                            item {
                                ActiveFilterPill(label = selectedCityName) { selectedCityName = "All Cities" }
                            }
                        }

                        if (selectedCondition != "All") {
                            item {
                                ActiveFilterPill(label = selectedCondition) { selectedCondition = "All" }
                            }
                        }

                        if (activeFilterCount > 0) {
                            item {
                                Text(
                                    text = "Reset All",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clickable {
                                            selectedCategoryName = "All Categories"
                                            selectedCityName = "All Cities"
                                            selectedCondition = "All"
                                            minPriceText = ""
                                            maxPriceText = ""
                                            selectedSort = "Recommended"
                                        }
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    // Dynamic Search Auto-complete Suggestions Dropdown
                    val showSuggestions = searchQuery.isNotEmpty() && !isSubmitted
                    val matchingSuggestions = remember(searchQuery, products) {
                        if (searchQuery.isBlank()) emptyList()
                        else {
                            val queryLower = searchQuery.lowercase()
                            // Find matching product titles, category names, or conditions
                            val titles = products.map { it.title }.filter { it.lowercase().contains(queryLower) }
                            val categories = mockCategories.map { it.name }.filter { it.lowercase().contains(queryLower) }
                            val conditions = products.map { it.condition }.filter { it.lowercase().contains(queryLower) }
                            (titles + categories + conditions).distinct().take(4)
                        }
                    }

                    if (showSuggestions && matchingSuggestions.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                matchingSuggestions.forEach { suggestion ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                searchQuery = suggestion
                                                submittedQuery = suggestion
                                                isSubmitted = true
                                            }
                                            .padding(horizontal = 16.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = suggestion,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Result Count & View Toggle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredProducts.size} results",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

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

            // Quick Search Chips if search query is empty
            if (searchQuery.isBlank()) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Text("Trending Searches", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(popularSearchTags) { tag ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)),
                                modifier = Modifier.clickable {
                                    searchQuery = tag
                                    submittedQuery = tag
                                    isSubmitted = true
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.TrendingUp,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(tag, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Product List Grid / List
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
                        Text("No matching items found", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Try searching with different keywords or clearing your active filters.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (isGridView) {
                LazyVerticalStaggeredGrid(
                    columns = StaggeredGridCells.Fixed(2),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalItemSpacing = 10.dp,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { item ->
                        ProductGridCard(
                            product = item,
                            onClick = { onProductClick(item.id) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 4.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filteredProducts) { item ->
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
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                ) {
                                    AsyncImage(
                                        model = item.imageUrl,
                                        contentDescription = item.title,
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        placeholder = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(item.categoryId, item.title)),
                                        error = rememberAsyncImagePainter(model = NetworkManager.getFallbackImageUrl(item.categoryId, item.title))
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
                                        Text(text = item.location, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Refine Filters Modal Sheet
    if (showFilterSheet) {
        ModalBottomSheet(
            onDismissRequest = { showFilterSheet = false },
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Refine Search", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.ExtraBold)
                    IconButton(onClick = { showFilterSheet = false }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sort Options
                Text("Sort By", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val sortOptions = listOf("Recommended", "Newest", "Price: Low to High", "Price: High to Low")
                    items(sortOptions) { option ->
                        FilterChip(
                            selected = selectedSort == option,
                            onClick = { selectedSort = option },
                            label = { Text(option) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Category Selection
                Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCategoryName == "All Categories",
                            onClick = { selectedCategoryName = "All Categories" },
                            label = { Text("All Categories") }
                        )
                    }
                    items(mockCategories) { cat ->
                        FilterChip(
                            selected = selectedCategoryName == cat.name,
                            onClick = { selectedCategoryName = cat.name },
                            label = { Text(cat.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // City Selection
                Text("City / Location", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedCityName == "All Cities",
                            onClick = { selectedCityName = "All Cities" },
                            label = { Text("All Cities") }
                        )
                    }
                    items(ethiopianCities) { city ->
                        FilterChip(
                            selected = selectedCityName == city.name,
                            onClick = { selectedCityName = city.name },
                            label = { Text(city.name) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Price Inputs
                Text("Price Range (ETB)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = minPriceText,
                        onValueChange = { minPriceText = it },
                        label = { Text("Min Price") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxPriceText,
                        onValueChange = { maxPriceText = it },
                        label = { Text("Max Price") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Condition
                Text("Condition", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("All", "Brand New", "Used", "Refurbished").forEach { cond ->
                        FilterChip(
                            selected = selectedCondition == cond,
                            onClick = { selectedCondition = cond },
                            label = { Text(cond) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            selectedCategoryName = "All Categories"
                            selectedCityName = "All Cities"
                            selectedCondition = "All"
                            minPriceText = ""
                            maxPriceText = ""
                            selectedSort = "Recommended"
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Reset")
                    }

                    Button(
                        onClick = { showFilterSheet = false },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Apply Filters")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun ActiveFilterPill(label: String, onRemove: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                Icons.Default.Close,
                contentDescription = "Remove",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(14.dp)
                    .clickable(onClick = onRemove)
            )
        }
    }
}
