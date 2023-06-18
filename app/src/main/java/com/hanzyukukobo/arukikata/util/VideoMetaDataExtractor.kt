package com.hanzyukukobo.arukikata.util

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.util.Log
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.FFprobeKit
import com.hanzyukukobo.arukikata.data.VideoInfo
import java.io.File

class VideoMetaDataExtractor {
    companion object {
        fun extract(context: Context, uri: Uri): VideoInfo {
            // FFprobを使って動画の長さを取得する
            val ffmpegPath = FFmpegKitConfig.getSafParameterForRead(context, uri)
            val mediaInformation = FFprobeKit.getMediaInformation(ffmpegPath)
            val information = mediaInformation.mediaInformation

            // 小数点ありの秒単位のString型で取得できるのでマイクロ秒で整数化する
            val duration = (information.duration.toDouble() * 1000000).toInt()

            val retriever = MediaMetadataRetriever().apply {
                setDataSource(context, uri)
            }

            val rotation = Integer.parseInt(
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)!!)
            val width = Integer.parseInt(
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)!!)
            val height = Integer.parseInt(
                retriever.extractMetadata(
                    MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)!!)

            val frameCount = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Integer.parseInt(
                    retriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)!!)
            } else {
                // -1ではじかれるようにする
                -1
            }
            retriever.release()

            // 各所でresultFileを生成していると間違える可能性があるから
            // 使用する動画の情報を生成する段階で、出力先を指定して参照しやすくする
            val file = File(context.filesDir, "resultVideo.mp4")

            // 元のUriを保持しておくことで様々な形式への変換に対応できるようにする
            return VideoInfo(uri, file, width, height, rotation, duration, frameCount)
        }
    }
}