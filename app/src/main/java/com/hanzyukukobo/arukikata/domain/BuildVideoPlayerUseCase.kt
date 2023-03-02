package com.hanzyukukobo.arukikata.domain

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import javax.inject.Inject

class BuildVideoPlayerUseCase @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository
) {
    operator fun invoke(context: Context, playerView: PlayerView, isResult: Boolean): ExoPlayer? {
        val videoInfo = gaitAnalysisRepository.getVideoInfo() ?: return null

        return ExoPlayer.Builder(context)
            .build()
            .also {
                playerView.player = it

                val mediaItem = if (isResult) {
                    MediaItem.fromUri(videoInfo.resultVideoFile.path)
                } else {
                    MediaItem.fromUri(videoInfo.uri)
                }
                it.setMediaItem(mediaItem)
                it.prepare()
            }
    }
}