package com.nate.core.data.player

import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.nate.core.domain.model.SubtitleLink

object SubtitleParser {

    @OptIn(UnstableApi::class)
    fun toSubtitleConfiguration(subtitle: SubtitleLink): MediaItem.SubtitleConfiguration? {
        if (subtitle.url.isBlank() || !subtitle.url.startsWith("http")) {
            return null
        }

        val mimeType = when (subtitle.format.uppercase()) {
            "VTT" -> MimeTypes.TEXT_VTT
            "SRT" -> MimeTypes.APPLICATION_SUBRIP
            else -> MimeTypes.TEXT_VTT
        }

        return try {
            MediaItem.SubtitleConfiguration.Builder(Uri.parse(subtitle.url))
                .setMimeType(mimeType)
                .setLanguage(subtitle.lang)
                .setLabel(subtitle.label)
                .setSelectionFlags(C.SELECTION_FLAG_DEFAULT)
                .build()
        } catch (_: Exception) {
            null
        }
    }
}
