package com.nate.app.util

import android.util.LruCache
import com.nate.app.data.model.Media
import com.nate.app.data.repository.ScrapedLink
import java.util.Collections

object LruCacheManager {
    
    // In-memory cache for TMDB details (maximum 100 media records)
    private val mediaDetailsCache = LruCache<String, Media>(100)
    
    // In-memory cache for scraped stream links (maximum 50 content keys, e.g., "movie_1234" or "tv_567_2_3")
    private val streamLinksCache = LruCache<String, List<ScrapedLink>>(50)
    
    // Thread-safe lock objects
    private val detailsLock = Any()
    private val streamsLock = Any()

    fun putMediaDetails(key: String, media: Media) {
        synchronized(detailsLock) {
            mediaDetailsCache.put(key, media)
        }
    }

    fun getMediaDetails(key: String): Media? {
        return synchronized(detailsLock) {
            mediaDetailsCache.get(key)
        }
    }

    fun putStreamLinks(key: String, links: List<ScrapedLink>) {
        synchronized(streamsLock) {
            streamLinksCache.put(key, links)
        }
    }

    fun getStreamLinks(key: String): List<ScrapedLink>? {
        return synchronized(streamsLock) {
            streamLinksCache.get(key)
        }
    }

    fun clearAll() {
        synchronized(detailsLock) {
            mediaDetailsCache.evictAll()
        }
        synchronized(streamsLock) {
            streamLinksCache.evictAll()
        }
    }
}
