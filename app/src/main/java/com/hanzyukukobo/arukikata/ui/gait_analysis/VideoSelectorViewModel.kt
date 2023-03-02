package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.ViewModel
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.databinding.FragmentVideoSelectorBinding
import com.hanzyukukobo.arukikata.domain.BuildVideoPlayerUseCase
import com.hanzyukukobo.arukikata.util.VideoMetaDataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class VideoSelectorViewModel @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository,
    private val buildVideoPlayerUseCase: BuildVideoPlayerUseCase
) : ViewModel() {

    private var player: ExoPlayer? = null

    lateinit var onCompleteListener: OnCompleteListener

    fun pickVideoFromGallery(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "video/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("video/*"))
        }
        launcher.launch(Intent.createChooser(intent, "Select Video"))
    }

    @UnstableApi
    fun pickedVideoFromGallery(
        context: Context, uri: Uri, binding: FragmentVideoSelectorBinding) {
        extractVideoMetaData(context, uri)
        /* こうしたいけど縦画面だとUIが崩れてしまう。これに対応できたら実装する
        if (videoInfo.rotation == 0 || videoInfo.rotation == 180) {
            binding.aspectRationFrameLayout.setAspectRatio(
                (videoInfo.width.toFloat()/videoInfo.height.toFloat()))
        } else {
            binding.aspectRationFrameLayout.setAspectRatio(
                (videoInfo.height.toFloat()/videoInfo.width.toFloat()))
        }
        */
        val videoInfo = gaitAnalysisRepository.getVideoInfo()
        binding.aspectRationFrameLayout.setAspectRatio(
            (videoInfo!!.width.toFloat()/videoInfo.height.toFloat()))
        buildPlayer(context, binding.videoView)
    }

    private fun extractVideoMetaData(context: Context, uri: Uri) {
        val videoInfo = VideoMetaDataExtractor.extract(context, uri)
        gaitAnalysisRepository.setVideoInfo(videoInfo)
    }

    fun buildPlayer(context: Context, playerView: PlayerView) {
        player = buildVideoPlayerUseCase(context, playerView, false)
        if (player != null) {
            onCompleteListener.onComplete(GaitAnalysisFragments.VideoSelector)
        }
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }
}