package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

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

fun formatDisplayPrice(price: String): String {
    if (price.isBlank()) return "ETB 0"
    if (price.startsWith("ETB") || price.startsWith("$")) return price
    val num = price.replace("[^\\d.]".toRegex(), "").toDoubleOrNull()
    return if (num != null) {
        if (num % 1.0 == 0.0) {
            String.format("ETB %,d", num.toLong())
        } else {
            String.format("ETB %,.2f", num)
        }
    } else {
        "ETB $price"
    }
}

data class Category(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val bgColor: Color,
    val iconColor: Color
)

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
