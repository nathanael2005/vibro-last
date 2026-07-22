package com.example.data.local

import android.content.Context
import com.example.model.Product
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory

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

    fun getPostedAdIds(context: Context): Set<String> {
        val prefs = context.getSharedPreferences("marketplace_products_prefs", Context.MODE_PRIVATE)
        return prefs.getStringSet("posted_ad_ids", emptySet()) ?: emptySet()
    }

    fun addPostedAdId(context: Context, adId: String) {
        val prefs = context.getSharedPreferences("marketplace_products_prefs", Context.MODE_PRIVATE)
        val current = prefs.getStringSet("posted_ad_ids", emptySet()) ?: emptySet()
        val updated = current.toMutableSet().apply { add(adId) }
        prefs.edit().putStringSet("posted_ad_ids", updated).apply()
    }
}
