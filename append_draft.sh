cat << 'INNER_EOF' >> app/src/main/java/com/example/MarketplaceApp.kt

object AdDraftStore {
    val data = mutableMapOf<String, Any>()
    
    fun getString(key: String, default: String): String = data[key] as? String ?: default
    fun getInt(key: String, default: Int): Int = data[key] as? Int ?: default
    fun getBoolean(key: String, default: Boolean): Boolean = data[key] as? Boolean ?: default
    
    fun clear() {
        data.clear()
    }
}
INNER_EOF
