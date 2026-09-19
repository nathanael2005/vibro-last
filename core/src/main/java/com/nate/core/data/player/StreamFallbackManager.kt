package com.nate.core.data.player

import com.nate.core.domain.model.StreamLink

class StreamFallbackManager(val streams: List<StreamLink>) {
    private var currentIndex = 0

    fun getCurrentStream(): StreamLink? {
        if (streams.isEmpty() || currentIndex >= streams.size) return null
        return streams[currentIndex]
    }

    fun moveToNextStream(): StreamLink? {
        currentIndex++
        return getCurrentStream()
    }

    fun moveToStream(serverId: String): StreamLink? {
        val index = streams.indexOfFirst { it.serverId == serverId }
        if (index != -1) {
            currentIndex = index
            return streams[currentIndex]
        }
        return null
    }

    fun hasMoreStreams(): Boolean {
        return currentIndex + 1 < streams.size
    }

    fun getCurrentIndex(): Int = currentIndex
}
