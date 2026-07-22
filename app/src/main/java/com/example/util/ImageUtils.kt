package com.example.util

import android.content.Context
import com.example.model.Product

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
        android.util.Log.e("ImageUtils", "Error compressing image bytes safely", e)
        null
    }
}

fun getProductImages(product: Product): List<String> {
    fun isValidUrl(url: String): Boolean {
        val trimmed = url.trim()
        return trimmed.isNotBlank() &&
                !trimmed.startsWith("placeholder") &&
                !trimmed.startsWith("android.resource") &&
                (trimmed.startsWith("http://") || trimmed.startsWith("https://") || trimmed.startsWith("content://") || trimmed.startsWith("file://"))
    }

    val fallback = com.example.NetworkManager.getFallbackImageUrl(product.categoryId, product.title)

    val rawList = if (product.imageUrl.contains("||")) {
        product.imageUrl.split("||").map { it.trim() }.filter { it.isNotBlank() }
    } else {
        listOf(product.imageUrl)
    }

    val sanitizedList = rawList.map { url ->
        if (isValidUrl(url)) url else fallback
    }.toMutableList()

    if (sanitizedList.isEmpty()) {
        sanitizedList.add(fallback)
    }

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

    if (!sanitizedList.contains(alt1)) sanitizedList.add(alt1)
    if (!sanitizedList.contains(alt2)) sanitizedList.add(alt2)

    return sanitizedList
}
