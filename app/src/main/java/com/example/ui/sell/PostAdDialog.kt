package com.example.ui.sell

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.AdDraftStore
import com.example.util.compressImageUri
import com.example.NetworkManager
import com.example.model.CityLocation
import com.example.model.Product
import com.example.model.ethiopianCities
import com.example.model.mockCategories
import com.example.ui.components.SelectFieldRow

val carBrands = listOf("Toyota", "Suzuki", "Hyundai", "Nissan", "Isuzu", "Honda", "Volkswagen", "Ford", "BYD", "Lifan", "Other Brand")
val phoneBrands = listOf("Apple / iPhone", "Samsung", "Xiaomi / Redmi", "Tecno", "Infinix", "Huawei", "Google Pixel", "Itel", "Realme", "Other Brand")
val conditions = listOf("Brand New (Unopened)", "Used - Like New", "Used - Good / Refurbished", "Used - Fair / Working", "For Parts / Faulty")

@Composable
fun PostAdDialog(
    editingProduct: Product? = null,
    onDismiss: () -> Unit,
    onPublish: (title: String, price: String, categoryId: String, location: String, condition: String, description: String, imageUrl: String, isPromoted: Boolean) -> Unit
) {
    val context = LocalContext.current

    var wizardStep by rememberSaveable { mutableStateOf(1) }
    var categoryId by rememberSaveable { mutableStateOf(editingProduct?.categoryId ?: AdDraftStore.getString("categoryId", "2")) }
    var subcategory by rememberSaveable {
        mutableStateOf(
            editingProduct?.description?.let { desc ->
                Regex("📂 (.*?)\n").find(desc)?.groupValues?.get(1)?.trim()
                    ?: Regex("🌐 Category: .* \\((.*)\\)").find(desc)?.groupValues?.get(1)?.trim()
            } ?: AdDraftStore.getString("subcategory", "Mobile Phones")
        )
    }

    var currentSubView by rememberSaveable { mutableStateOf("main") }
    var selectedCityPostAd by remember { mutableStateOf<CityLocation?>(null) }
    var selectorTitle by rememberSaveable { mutableStateOf("") }
    var selectorOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selectorSelectedValue by rememberSaveable { mutableStateOf("") }
    var selectorTargetProperty by rememberSaveable { mutableStateOf("") }

    var vehicleBrand by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🚘 Brand: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("vehicleBrand", "")) }
    var vehicleModel by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("📋 Model: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("vehicleModel", "")) }
    var vehicleYear by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("📅 Year: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("vehicleYear", "")) }
    var vehicleTransmission by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("⚙️ Transmission: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("vehicleTransmission", "Automatic")) }
    var vehicleFuel by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("⛽ Fuel: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("vehicleFuel", "Petrol")) }

    var phoneBrand by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("📱 Brand: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("phoneBrand", "")) }
    var phoneStorage by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("💾 Storage: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("phoneStorage", "")) }
    var phoneRam by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("⚡ RAM: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("phoneRam", "")) }
    var phoneColor by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🎨 Color: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("phoneColor", "")) }

    var propertyBedrooms by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🛏️ Bedrooms: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("propertyBedrooms", "")) }
    var propertyBathrooms by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🚿 Bathrooms: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("propertyBathrooms", "")) }
    var propertyFurnishing by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🛋️ Furnishing: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("propertyFurnishing", "Unfurnished")) }

    var fashionGender by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🚻 Gender: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("fashionGender", "Unisex")) }
    var fashionSize by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("📏 Size: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("fashionSize", "")) }
    var fashionBrand by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🏷️ Apparel Brand: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("fashionBrand", "")) }

    var furnitureMaterial by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🪵 Material: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("furnitureMaterial", "")) }
    var furnitureBrand by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🏷️ Brand: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("furnitureBrand", "")) }

    var elecBrand by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🏷️ Brand: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("elecBrand", "")) }
    var elecType by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("💻 Device Type: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("elecType", "")) }

    var beautyBrand by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🌸 Brand/Line: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("beautyBrand", "")) }
    var beautyType by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("💅 Product Type: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("beautyType", "")) }

    var serviceType by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🛠️ Service Offered: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("serviceType", "")) }
    var serviceExperience by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("💼 Experience: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("serviceExperience", "")) }

    var jobType by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("👔 Job Type: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("jobType", "")) }
    var jobExperience by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🎓 Experience Required: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("jobExperience", "")) }

    var petType by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🐶 Pet Type: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("petType", "")) }
    var petAge by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("📅 Age Level: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("petAge", "")) }

    var agriType by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🚜 Agriculture Type: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("agriType", "")) }
    var agriUnit by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("📦 Measure Unit: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("agriUnit", "")) }

    var extraSpecificBrand by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("🏷️ Specific Brand: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("extraSpecificBrand", "")) }
    var extraSpecificPartName by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("⚙️ Specific Part/Sub-item: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("extraSpecificPartName", "")) }
    var extraSpecificModel by rememberSaveable { mutableStateOf(editingProduct?.description?.let { Regex("📋 Model/Version: (.*)").find(it)?.groupValues?.get(1)?.trim() } ?: AdDraftStore.getString("extraSpecificModel", "")) }

    var title by rememberSaveable { mutableStateOf(editingProduct?.title ?: AdDraftStore.getString("title", "")) }
    var price by rememberSaveable { mutableStateOf(editingProduct?.price?.replace("[^\\d]".toRegex(), "") ?: AdDraftStore.getString("price", "")) }
    var isPriceNegotiable by rememberSaveable { mutableStateOf(AdDraftStore.getBoolean("isPriceNegotiable", true)) }
    var location by rememberSaveable { mutableStateOf(editingProduct?.location ?: AdDraftStore.getString("location", "Addis Ababa, Bole")) }
    var condition by rememberSaveable { mutableStateOf(editingProduct?.condition ?: AdDraftStore.getString("condition", "Brand New (Unopened)")) }
    var description by rememberSaveable {
        mutableStateOf(
            editingProduct?.description?.let { desc ->
                if (desc.contains("📝 AD DESCRIPTION:\n")) {
                    desc.substringAfter("📝 AD DESCRIPTION:\n")
                } else if (!desc.contains("📌 SPECIFICATIONS:\n")) {
                    desc
                } else ""
            } ?: AdDraftStore.getString("description", "")
        )
    }
    var imageUrl by rememberSaveable { mutableStateOf(editingProduct?.imageUrl ?: AdDraftStore.getString("imageUrl", "")) }
    var isUploadingMedia by remember { mutableStateOf(false) }

    var showPhoneNumber by rememberSaveable { mutableStateOf(AdDraftStore.getBoolean("showPhoneNumber", true)) }
    var allowVibroChats by rememberSaveable { mutableStateOf(AdDraftStore.getBoolean("allowVibroChats", true)) }
    var allowWhatsApp by rememberSaveable { mutableStateOf(AdDraftStore.getBoolean("allowWhatsApp", true)) }

    var selectedPremiumTier by rememberSaveable { mutableStateOf(if (editingProduct?.isPromoted == true) "boost" else "free") }
    var showPaymentScreen by remember { mutableStateOf(false) }

    val forbiddenSpamKeywords = remember { listOf("cheat", "hack", "scam", "deposit first", "send money first", "lottery") }
    val spamWarningDetected = remember(title, description) {
        val combinedText = (title + " " + description).lowercase()
        forbiddenSpamKeywords.any { combinedText.contains(it) }
    }

    val pricingSanityWarningDetected = remember(price) {
        val pNum = price.toLongOrNull() ?: 0L
        pNum in 1..49
    }

    fun executePublishAd(tier: String) {
        val finalTitle = title.trim()
        val rawPrice = price.trim()
        val cleanPriceNum = rawPrice.replace("[^\\d]".toRegex(), "")
        val formattedPrice = if (cleanPriceNum.isNotEmpty()) {
            String.format("ETB %,d", cleanPriceNum.toLong())
        } else {
            "ETB 0"
        } + if (isPriceNegotiable) " (Negotiable)" else ""

        val finalImgUrl = if (imageUrl.trim().isNotEmpty()) imageUrl else {
            NetworkManager.getFallbackImageUrl(categoryId, finalTitle)
        }

        val specsBuilder = StringBuilder()
        specsBuilder.append("📂 $subcategory\n")
        val finalDescription = if (specsBuilder.isNotEmpty()) {
            val descPart = if (description.trim().isNotEmpty()) "\n\n📝 AD DESCRIPTION:\n" + description else ""
            "📌 SPECIFICATIONS:\n" + specsBuilder.toString() + descPart
        } else {
            description
        }

        onPublish(
            finalTitle,
            formattedPrice,
            categoryId,
            location,
            condition,
            finalDescription,
            finalImgUrl,
            tier != "free"
        )
        AdDraftStore.clear()
        onDismiss()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(vertical = 16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (editingProduct != null) "Edit Listing" else "Post New Ad",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Ad Title") },
                    modifier = Modifier.fillMaxWidth().testTag("ad_title_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = price,
                    onValueChange = { price = it },
                    label = { Text("Price (ETB)") },
                    modifier = Modifier.fillMaxWidth().testTag("ad_price_input"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth().testTag("ad_location_input"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().height(100.dp).testTag("ad_description_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        executePublishAd("free")
                    },
                    modifier = Modifier.fillMaxWidth().testTag("submit_ad_button"),
                    enabled = title.trim().isNotEmpty() && price.trim().isNotEmpty()
                ) {
                    Text("Publish Ad Live Now", fontWeight = FontWeight.Bold)
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
