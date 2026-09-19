package com.nate.app.util

import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.nate.app.data.model.Media
import com.nate.app.ui.screens.PlayerActivity

object PlayerNavigator {
    fun launchMovie(
        context: Context,
        media: Media,
        episodeCount: Int = 0,
        resumePositionMs: Long = 0L,
    ) {
        launch(
            context = context,
            mediaId = media.id,
            mediaType = "movie",
            mediaTitle = media.displayTitle,
            season = 1,
            episode = 1,
            episodeCount = episodeCount,
            resumePositionMs = resumePositionMs,
        )
    }

    fun launchEpisode(
        context: Context,
        media: Media,
        season: Int,
        episode: Int,
        episodeCount: Int,
        resumePositionMs: Long = 0L,
    ) {
        launch(
            context = context,
            mediaId = media.id,
            mediaType = "tv",
            mediaTitle = media.displayTitle,
            season = season,
            episode = episode,
            episodeCount = episodeCount,
            resumePositionMs = resumePositionMs,
        )
    }

    private fun launch(
        context: Context,
        mediaId: Int,
        mediaType: String,
        mediaTitle: String,
        season: Int,
        episode: Int,
        episodeCount: Int,
        resumePositionMs: Long,
    ) {
        try {
            val intent = Intent(context, PlayerActivity::class.java).apply {
                putExtra(PlayerActivity.EXTRA_MEDIA_ID, mediaId)
                putExtra(PlayerActivity.EXTRA_MEDIA_TYPE, mediaType)
                putExtra(PlayerActivity.EXTRA_MEDIA_TITLE, mediaTitle)
                putExtra(PlayerActivity.EXTRA_SEASON, season)
                putExtra(PlayerActivity.EXTRA_EPISODE, episode)
                putExtra(PlayerActivity.EXTRA_EPISODE_COUNT, episodeCount)
                putExtra(PlayerActivity.EXTRA_RESUME_POSITION_MS, resumePositionMs)
            }
            context.startActivity(intent)
        } catch (t: Throwable) {
            Toast.makeText(
                context,
                "Unable to open player: ${t.message ?: "unknown error"}",
                Toast.LENGTH_LONG,
            ).show()
        }
    }
}
